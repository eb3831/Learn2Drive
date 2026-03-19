package com.example.learn2drive.Fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Adapters.TimeSlotAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.TimeSlot;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragment responsible for allowing teachers to manage their working hours.
 * Generates time slots based on selected date, start/end hours, and duration.
 */
public class HoursManagerFragment extends Fragment implements TimeSlotAdapter.OnTimeSlotStatusChangeListener
{

    private ImageView btnBack;
    private TextView etDate, etStartHour, etEndHour;
    private Switch switchDuration;
    private TextView tvAvailableSlotsLabel;
    private LinearLayout btnGenerateSlots, hoursManagerEmptyLayout;
    private RecyclerView rvTimeSlots;
    private FrameLayout pbHoursManagerOverlay;

    private TimeSlotAdapter timeSlotAdapter;
    private List<TimeSlot> timeSlotList;

    private int selectedDuration = 60; // Default duration
    private String selectedDate = ""; // Format: dd-MM-yyyy

    private DatabaseReference currentDayRef;
    private ValueEventListener timeSlotsListener;

    public HoursManagerFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_hours_manager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupDefaults();
        setupClickListeners();
    }

    public static HoursManagerFragment newInstance()
    {
        return new HoursManagerFragment();
    }

    /**
     * Initializes all UI components and sets up the RecyclerView.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        btnBack = view.findViewById(R.id.btnBack);
        etDate = view.findViewById(R.id.etDate);
        etStartHour = view.findViewById(R.id.etStartHour);
        etEndHour = view.findViewById(R.id.etEndHour);
        switchDuration = view.findViewById(R.id.switchDuration);
        btnGenerateSlots = view.findViewById(R.id.btnGenerateSlots);
        tvAvailableSlotsLabel = view.findViewById(R.id.tvAvailableSlotsLabel);
        hoursManagerEmptyLayout = view.findViewById(R.id.hoursManagerEmptyLayout);
        rvTimeSlots = view.findViewById(R.id.rvTimeSlots);
        pbHoursManagerOverlay = view.findViewById(R.id.pbHoursManagerOverlay);

        timeSlotList = new ArrayList<>();
        timeSlotAdapter = new TimeSlotAdapter(requireContext(), timeSlotList, false, this);
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTimeSlots.setAdapter(timeSlotAdapter);
    }

    /**
     * Sets the default values when the fragment is first opened.
     * Sets today's date and triggers the initial data load from Firebase.
     */
    private void setupDefaults()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        etDate.setText(selectedDate);

        // Match the default state of the switch in XML (checked = true -> 60 min)
        selectedDuration = switchDuration.isChecked() ? 60 : 45;

        loadTimeSlotsFromFirebase(selectedDate);
    }

    /**
     * Sets up click listeners for all interactive UI elements.
     */
    private void setupClickListeners()
    {
        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        etDate.setOnClickListener(v -> showDatePicker());

        etStartHour.setOnClickListener(v -> showTimePicker(etStartHour));

        etEndHour.setOnClickListener(v -> showTimePicker(etEndHour));

        switchDuration.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            selectedDuration = isChecked ? 60 : 45;
        });

        btnGenerateSlots.setOnClickListener(v -> handleGenerateClick());
    }

    /**
     * Displays a DatePickerDialog to select a working date.
     * Opens with the currently selected date and prevents the selection of past dates.
     */
    private void showDatePicker()
    {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());

        try
        {
            if (!selectedDate.isEmpty())
            {
                Date date = sdf.parse(selectedDate);
                if (date != null)
                {
                    calendar.setTime(date);
                }
            }
        } catch (ParseException e)
        {
            e.printStackTrace();
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, selectedYear, selectedMonth, selectedDay) ->
        {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(selectedYear, selectedMonth, selectedDay);

            selectedDate = sdf.format(selectedCalendar.getTime());
            etDate.setText(selectedDate);

            loadTimeSlotsFromFirebase(selectedDate);

        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /**
     * Displays a TimePickerDialog to select start or end hours.
     *
     * @param targetTextView The TextView to update with the selected time.
     */
    private void showTimePicker(TextView targetTextView)
    {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, selectedHour, selectedMinute) ->
        {
            String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
            targetTextView.setText(time);
        }, hour, minute, true); // true for 24-hour format

        timePickerDialog.show();
    }

    /**
     * Handles the click event for the generate slots button.
     * Validates input and shows a confirmation dialog before generating.
     */
    private void handleGenerateClick()
    {
        String startText = etStartHour.getText().toString();
        String endText = etEndHour.getText().toString();

        if (startText.isEmpty() || endText.isEmpty())
        {
            Toast.makeText(requireContext(), "Please select start and end hours", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Generate Time Slots")
                .setMessage("This action will generate new time slots and overwrite any existing slots for " + selectedDate + ".\nAre you sure you want to proceed?")
                .setPositiveButton("Generate", (dialog, which) -> generateSlotsAlgorithm(startText, endText))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Generates time slots based on the selected start and end hours.
     *
     * @param startText The selected start time in HH:mm format.
     * @param endText   The selected end time in HH:mm format.
     */
    private void generateSlotsAlgorithm(String startText, String endText)
    {
        try
        {
            String[] startParts = startText.split(":");
            String[] endParts = endText.split(":");

            int startMins = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMins = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);

            if (startMins >= endMins)
            {
                Toast.makeText(requireContext(), "Start hour must be before end hour", Toast.LENGTH_SHORT).show();
                return;
            }

            timeSlotList.clear();
            int currentMins = startMins;

            while (currentMins + selectedDuration <= endMins)
            {
                int startH = currentMins / 60;
                int startM = currentMins % 60;
                int nextMins = currentMins + selectedDuration;
                int endH = nextMins / 60;
                int endM = nextMins % 60;

                String slotStartTime = String.format(Locale.getDefault(), "%02d:%02d", startH, startM);
                String slotEndTime = String.format(Locale.getDefault(), "%02d:%02d", endH, endM);

                TimeSlot newSlot = new TimeSlot(selectedDuration, slotStartTime, slotEndTime);

                timeSlotList.add(newSlot);
                currentMins += selectedDuration;
            }

            timeSlotAdapter.notifyDataSetChanged();
            updateEmptyState();
            saveTimeSlotsToFirebase(); // Save the new list directly to DB

        } catch (Exception e)
        {
            Toast.makeText(requireContext(), "Error parsing time", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Updates the UI based on whether the timeSlotList is empty or not.
     */
    private void updateEmptyState()
    {
        if (timeSlotList.isEmpty())
        {
            hoursManagerEmptyLayout.setVisibility(View.VISIBLE);
            rvTimeSlots.setVisibility(View.GONE);
            tvAvailableSlotsLabel.setVisibility(View.GONE);
        } else
        {
            hoursManagerEmptyLayout.setVisibility(View.GONE);
            rvTimeSlots.setVisibility(View.VISIBLE);
            tvAvailableSlotsLabel.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Saves the newly generated time slots to Firebase atomically.
     */
    private void saveTimeSlotsToFirebase()
    {
        if (FBRef.uid == null) return;

        DatabaseReference dayRef = FBRef.FB_DB.getReference("Teachers Timetable")
                .child(FBRef.uid)
                .child("Available Hours")
                .child(selectedDate);

        pbHoursManagerOverlay.setVisibility(View.VISIBLE);

        if (timeSlotsListener != null)
        {
            dayRef.removeEventListener(timeSlotsListener);
        }

        java.util.Map<String, Object> slotsMap = new java.util.HashMap<>();
        for (TimeSlot slot : timeSlotList)
        {
            slotsMap.put(slot.getStartTime(), slot);
        }

        dayRef.setValue(slotsMap).addOnCompleteListener(task ->
        {
            pbHoursManagerOverlay.setVisibility(View.GONE);

            if (task.isSuccessful())
            {
                android.widget.Toast.makeText(requireContext(), "Slots updated successfully", android.widget.Toast.LENGTH_SHORT).show();
            }

            else
            {
                android.widget.Toast.makeText(requireContext(), "Failed to save slots", android.widget.Toast.LENGTH_SHORT).show();
            }

            if (timeSlotsListener != null)
            {
                dayRef.addValueEventListener(timeSlotsListener);
            }
        });
    }

    /**
     * Loads time slots from Firebase for the selected date.
     * Attaches a ValueEventListener to keep data synced.
     *
     * @param date The selected date in dd-MM-yyyy format.
     */
    private void loadTimeSlotsFromFirebase(String date)
    {
        if (FBRef.uid == null) return;

        // Remove listener from previous date if it exists to prevent memory leaks/duplicate calls
        if (currentDayRef != null && timeSlotsListener != null)
        {
            currentDayRef.removeEventListener(timeSlotsListener);
        }

        currentDayRef = FBRef.FB_DB.getReference("Teachers Timetable")
                .child(FBRef.uid)
                .child("Available Hours")
                .child(date);

        pbHoursManagerOverlay.setVisibility(View.VISIBLE);

        timeSlotsListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                timeSlotList.clear();
                if (snapshot.exists())
                {
                    for (DataSnapshot timeSnapshot : snapshot.getChildren())
                    {
                        TimeSlot slot = timeSnapshot.getValue(TimeSlot.class);
                        if (slot != null)
                        {
                            timeSlotList.add(slot);
                        }
                    }
                }
                timeSlotAdapter.notifyDataSetChanged();
                updateEmptyState();
                pbHoursManagerOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                pbHoursManagerOverlay.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Failed to load schedule", Toast.LENGTH_SHORT).show();
            }
        };

        currentDayRef.addValueEventListener(timeSlotsListener);
    }

    @Override
    public void onStatusChanged(TimeSlot timeSlot, String newStatus, int position)
    {
        if (FBRef.uid == null) return;

        DatabaseReference slotStatusRef = FBRef.FB_DB.getReference("Teachers Timetable")
                .child(FBRef.uid)
                .child("Available Hours")
                .child(selectedDate)
                .child(timeSlot.getStartTime())
                .child("status");

        slotStatusRef.setValue(newStatus);
    }

    @Override
    public void onStudentTimeSlotClicked(TimeSlot timeSlot)
    {
        // Intentionally left blank for this fragment
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if (currentDayRef != null && timeSlotsListener != null)
        {
            currentDayRef.removeEventListener(timeSlotsListener);
        }
    }
}