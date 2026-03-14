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

public class StudentRequestsAdapter extends RecyclerView.Adapter<StudentRequestsAdapter.RequestViewHolder>
{

    private Context context;
    private List<Student> studentsList;
    private OnRequestClickListener listener;

    public interface OnRequestClickListener
    {
        void onAcceptClick(Student student, int position);
        void onRejectClick(Student student, int position);
    }

    public StudentRequestsAdapter(Context context, List<Student> studentsList, OnRequestClickListener listener) {
        this.context = context;
        this.studentsList = studentsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_request, parent, false);
        return new RequestViewHolder(view);
    }

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

    @Override
    public int getItemCount()
    {
        return studentsList.size();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvStudentName, tvStudentId;
        Button btnAccept, btnReject;

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