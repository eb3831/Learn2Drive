package com.example.learn2drive.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.Student;
import com.example.learn2drive.R;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * Adapter class for displaying a list of students in a RecyclerView.
 */
public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private Context context;
    private List<Student> studentList;
    private OnItemClickListener listener;

    /**
     * Interface to handle click events on individual student items in the list.
     */
    public interface OnItemClickListener {
        /**
         * Called when a student item is clicked.
         *
         * @param student The student object that was clicked.
         */
        void onItemClick(Student student);
    }

    /**
     * Sets the listener for item click events.
     *
     * @param listener The listener to set.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Constructor for the StudentAdapter.
     *
     * @param context     The context of the calling fragment/activity.
     * @param studentList The list of Student objects to display.
     */
    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
    }

    /**
     * Called when RecyclerView needs a new {@link StudentViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new StudentViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student, parent, false);
        return new StudentViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link StudentViewHolder} to reflect the student at the given position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        holder.tvStudentName.setText(student.getFullName());
        holder.tvStudentId.setText("ID: " + student.getIdNumber());
        holder.tvLessonsCount.setText(student.getLessonsCompleted() + " lessons completed");

        FBRef.refProfilePics.child(student.getUid()).child("profile.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        holder.ivStudentProfile.setImageTintList(null);
                        holder.ivStudentProfile.setPadding(0, 0, 0, 0);

                        Glide.with(context)
                                .load(uri)
                                .placeholder(R.drawable.ic_user)
                                .error(R.drawable.ic_user)
                                .circleCrop()
                                .into(holder.ivStudentProfile);
                    }
                });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(student);
                }
            }
        });
    }

    /**
     * Returns the total number of students in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount()
    {
        return studentList.size();
    }

    /**
     * ViewHolder class holding the UI elements for a single student item.
     */
    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentId, tvLessonsCount;
        ImageView ivStudentProfile;

        /**
         * Constructor for the StudentViewHolder.
         * Initializes the UI components by finding them within the provided item view.
         *
         * @param itemView The view containing the layout for a single student item.
         */
        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            tvLessonsCount = itemView.findViewById(R.id.tvLessonsCount);
            ivStudentProfile = itemView.findViewById(R.id.ivStudentProfile);
        }
    }
}