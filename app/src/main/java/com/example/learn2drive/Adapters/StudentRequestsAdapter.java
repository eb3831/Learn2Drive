package com.example.learn2drive.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.R;

import java.util.List;

/**
 * Adapter class for displaying a list of pending student registration requests in a RecyclerView.
 * Allows a teacher to accept or reject new students.
 */
public class StudentRequestsAdapter extends RecyclerView.Adapter<StudentRequestsAdapter.RequestViewHolder>
{

    private Context context;
    private List<Student> studentsList;
    private OnRequestClickListener listener;

    /**
     * Interface to handle click events on the request action buttons (Accept/Reject).
     */
    public interface OnRequestClickListener
    {
        /**
         * Called when the accept button is clicked for a specific student request.
         *
         * @param student  The student whose request is being accepted.
         * @param position The position of the item in the adapter's data set.
         */
        void onAcceptClick(Student student, int position);

        /**
         * Called when the reject button is clicked for a specific student request.
         *
         * @param student  The student whose request is being rejected.
         * @param position The position of the item in the adapter's data set.
         */
        void onRejectClick(Student student, int position);
    }

    /**
     * Constructor for the StudentRequestsAdapter.
     *
     * @param context      The context of the calling fragment/activity.
     * @param studentsList The list of pending student requests to display.
     * @param listener     The callback interface for button clicks.
     */
    public StudentRequestsAdapter(Context context, List<Student> studentsList, OnRequestClickListener listener) {
        this.context = context;
        this.studentsList = studentsList;
        this.listener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link RequestViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new RequestViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_request, parent, false);
        return new RequestViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link RequestViewHolder} to reflect the student request at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        Student currentStudent = studentsList.get(position);

        holder.tvStudentName.setText(currentStudent.getFullName());
        holder.tvStudentId.setText("ID: " + currentStudent.getIdNumber());

        holder.btnAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listener != null)
                {
                    listener.onAcceptClick(currentStudent, position);
                }
            }
        });

        holder.btnReject.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (listener != null)
                {
                    listener.onRejectClick(currentStudent, position);
                }
            }
        });
    }

    /**
     * Returns the total number of student requests in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount()
    {
        return studentsList.size();
    }

    /**
     * ViewHolder class holding the UI elements for a single student request item.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvStudentName, tvStudentId;
        Button btnAccept, btnReject;

        /**
         * Constructor for the RequestViewHolder.
         * Initializes the UI components by finding them within the provided item view.
         *
         * @param itemView The view containing the layout for a single student request item.
         */
        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}