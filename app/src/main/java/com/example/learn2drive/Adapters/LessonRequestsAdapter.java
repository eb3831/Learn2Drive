package com.example.learn2drive.Adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.learn2drive.Helpers.FBRef;
import com.example.learn2drive.Objects.LessonRequestModel;
import com.example.learn2drive.R;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * Adapter class for displaying a list of lesson requests in a RecyclerView.
 */
public class LessonRequestsAdapter extends RecyclerView.Adapter<LessonRequestsAdapter.RequestViewHolder>
{

    /**
     * Interface to handle click events on the request action buttons (Accept/Decline).
     */
    public interface OnRequestClickListener
    {
        void onAcceptClick(LessonRequestModel request);

        void onDeclineClick(LessonRequestModel request);
    }

    private Context context;
    private List<LessonRequestModel> requestsList;
    private OnRequestClickListener listener;

    /**
     * Constructor for the adapter.
     *
     * @param context      The context of the calling fragment/activity.
     * @param requestsList The data set of lesson requests.
     * @param listener     The callback interface for button clicks.
     */
    public LessonRequestsAdapter(Context context, List<LessonRequestModel> requestsList, OnRequestClickListener listener)
    {
        this.context = context;
        this.requestsList = requestsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position)
    {
        LessonRequestModel currentRequest = requestsList.get(position);

        holder.tvRequestDate.setText(currentRequest.getDate());

        String timeText = currentRequest.getTimeSlot().getStartTime() + " - " + currentRequest.getTimeSlot().getEndTime();
        holder.tvRequestTime.setText(timeText);

        if (currentRequest.getStudent() != null)
        {
            holder.tvStudentName.setText(currentRequest.getStudent().getFullName());
            holder.tvStudentId.setText("ID: " + currentRequest.getStudent().getIdNumber());

            // Fetch and load profile picture using Glide and Firebase Storage
            FBRef.refProfilePics.child(currentRequest.getStudent().getUid()).child("profile.jpg").getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>()
                    {
                        @Override
                        public void onSuccess(Uri uri)
                        {
                            holder.ivStudentProfile.setImageTintList(null);
                            holder.ivStudentProfile.setPadding(0, 0, 0, 0);

                            Glide.with(context)
                                    .load(uri)
                                    .placeholder(R.drawable.profile) // Shows this while loading
                                    .circleCrop()
                                    .into(holder.ivStudentProfile);
                        }
                    })
                    .addOnFailureListener(e ->
                    {
                        holder.ivStudentProfile.setImageResource(R.drawable.profile);
                    });
        }

        // Set click listeners for the buttons
        holder.btnAccept.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onAcceptClick(currentRequest);
            }
        });

        holder.btnDecline.setOnClickListener(v ->
        {
            if (listener != null)
            {
                listener.onDeclineClick(currentRequest);
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return requestsList.size();
    }

    /**
     * ViewHolder class holding the UI elements for a single request item.
     */
    public static class RequestViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvRequestDate, tvRequestTime, tvStudentName, tvStudentId;
        ImageView ivStudentProfile;
        AppCompatButton btnAccept, btnDecline;

        public RequestViewHolder(@NonNull View itemView)
        {
            super(itemView);
            tvRequestDate = itemView.findViewById(R.id.tvRequestDate);
            tvRequestTime = itemView.findViewById(R.id.tvRequestTime);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentId = itemView.findViewById(R.id.tvStudentId);
            ivStudentProfile = itemView.findViewById(R.id.ivStudentProfile);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }
    }
}