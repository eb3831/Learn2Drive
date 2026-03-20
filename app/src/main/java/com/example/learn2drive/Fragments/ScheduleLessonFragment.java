package com.example.learn2drive.Fragments;

import static com.example.learn2drive.Helpers.FBRef.refTeachersTimeTable;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.learn2drive.Adapters.TimeSlotAdapter;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.TimeSlot;
import com.example.learn2drive.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Fragment responsible for allowing a student to schedule a lesson with their teacher.
 */
public class ScheduleLessonFragment extends Fragment implements TimeSlotAdapter.OnTimeSlotStatusChangeListener
{
    private static final String ARG_TEACHER_UID = "teacher_uid";
    private String teacherUid;

    private ImageButton btnBack;
    private LinearLayout containerDatePicker;
    private TextView tvSelectedDate;
    private TextView tvHoursTitle;
    private RecyclerView rvTimeSlots;
    private LinearLayout layoutPlaceholder;
    private ProgressBar progressBar;

    private Calendar calendar;
    private String selectedDateStr;
    private TimeSlotAdapter adapter;
    private List<TimeSlot> timeSlotList;

    private DatabaseReference timetableRef;
    private ValueEventListener timetableListener;

    public ScheduleLessonFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @param teacherUid The UID of the teacher to fetch available hours from.
     * @return A new instance of fragment ScheduleLessonFragment.
     */
    public static ScheduleLessonFragment newInstance(String teacherUid)
    {
        ScheduleLessonFragment fragment = new ScheduleLessonFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEACHER_UID, teacherUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            teacherUid = getArguments().getString(ARG_TEACHER_UID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_schedule_lesson, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        setupListeners();
    }

    /**
     * Initializes UI components and basic variables.
     *
     * @param view The root view of the fragment.
     */
    private void initViews(View view)
    {
        btnBack = view.findViewById(R.id.btnBack);
        containerDatePicker = view.findViewById(R.id.containerDatePicker);
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate);
        tvHoursTitle = view.findViewById(R.id.tvHoursTitle);
        rvTimeSlots = view.findViewById(R.id.rvTimeSlots);
        layoutPlaceholder = view.findViewById(R.id.layoutPlaceholder);
        progressBar = view.findViewById(R.id.progressBar);

        calendar = Calendar.getInstance();
        timeSlotList = new ArrayList<>();

        layoutPlaceholder.setVisibility(View.VISIBLE);
        rvTimeSlots.setVisibility(View.GONE);
    }

    /**
     * Configures the RecyclerView and its adapter.
     */
    private void setupRecyclerView()
    {
        rvTimeSlots.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TimeSlotAdapter(getContext(), timeSlotList, true, this);
        rvTimeSlots.setAdapter(adapter);
    }

    /**
     * Sets up click listeners for the interactive UI elements.
     */
    private void setupListeners()
    {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null)
            {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        containerDatePicker.setOnClickListener(v -> openDatePickerDialog());
    }

    /**
     * Opens a DatePickerDialog, preventing the selection of past dates.
     */
    private void openDatePickerDialog()
    {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) ->
        {
            calendar.set(selectedYear, selectedMonth, selectedDay);

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            selectedDateStr = sdf.format(calendar.getTime());
            tvSelectedDate.setText(selectedDateStr);

            fetchAvailableHours(selectedDateStr);

        }, year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    /**
     * Fetches the available time slots for the specified date from Firebase.
     * Filters the list to only include slots with STATUS_AVAILABLE.
     *
     * @param dateStr The selected date in "dd-MM-yyyy" format.
     */
    private void fetchAvailableHours(String dateStr)
    {
        progressBar.setVisibility(View.VISIBLE);
        rvTimeSlots.setVisibility(View.GONE);
        layoutPlaceholder.setVisibility(View.GONE);
        tvHoursTitle.setVisibility(View.GONE);

        if (timetableRef != null && timetableListener != null)
        {
            timetableRef.removeEventListener(timetableListener);
        }

        timetableRef = refTeachersTimeTable.child(teacherUid).child(dateStr);

        timetableListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                timeSlotList.clear();

                for (DataSnapshot timeSnapshot : snapshot.getChildren())
                {
                    TimeSlot slot = timeSnapshot.getValue(TimeSlot.class);
                    if (slot != null && slot.isAvailable())
                    {
                        timeSlotList.add(slot);
                    }
                }

                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);

                if (timeSlotList.isEmpty())
                {
                    layoutPlaceholder.setVisibility(View.VISIBLE);
                    TextView tvMessage = layoutPlaceholder.findViewById(R.id.tvPlaceholderMessage);
                    tvMessage.setText("No available hours for this date.");
                    rvTimeSlots.setVisibility(View.GONE);
                    tvHoursTitle.setVisibility(View.GONE);
                }
                else
                {
                    layoutPlaceholder.setVisibility(View.GONE);
                    rvTimeSlots.setVisibility(View.VISIBLE);
                    tvHoursTitle.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load hours: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        timetableRef.addValueEventListener(timetableListener);
    }

    @Override
    public void onStatusChanged(TimeSlot timeSlot, String newStatus, int position)
    {
        // Not used in student mode
    }

    /**
     * Triggered when a student clicks on a specific time slot to request a lesson.
     * Displays a confirmation dialog before sending the request.
     *
     * @param timeSlot The selected time slot.
     */
    @Override
    public void onStudentTimeSlotClicked(TimeSlot timeSlot)
    {
        if (FBRef.uid == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Request Lesson")
                .setMessage("Do you want to request a lesson at " + timeSlot.getStartTime() + "?")
                .setPositiveButton("Yes, Request", (dialog, which) -> sendLessonRequest(timeSlot))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Updates the time slot in Firebase to indicate a lesson request.
     * Changes the status to REQUESTED and assigns the student's UID.
     *
     * @param timeSlot The selected time slot to be updated.
     */
    private void sendLessonRequest(TimeSlot timeSlot)
    {
        progressBar.setVisibility(View.VISIBLE);

        DatabaseReference slotRef = refTeachersTimeTable
                .child(teacherUid)
                .child(selectedDateStr)
                .child(timeSlot.getStartTime());

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", TimeSlot.STATUS_REQUESTED);
        updates.put("studentUid", FBRef.uid);

        slotRef.updateChildren(updates).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);

            if (task.isSuccessful())
            {
                Toast.makeText(getContext(), "Request sent successfully!", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(getContext(), "Failed to send request.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        if (timetableRef != null && timetableListener != null)
        {
            timetableRef.removeEventListener(timetableListener);
        }
    }
}