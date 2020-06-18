package com.mail.eventex;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

public class StarredAdapter extends RecyclerView.Adapter<StarredAdapter.MyViewHolder> {
    private Context context;
    private ArrayList subject_array;
    private OnStarredEventListener mOnEventListener;


    StarredAdapter (Context context, ArrayList subject, OnStarredEventListener onStarredEventListener){
        this.context =  context;
        subject_array = subject;
        this.mOnEventListener = onStarredEventListener;
    }

    @NonNull
    @Override
    public StarredAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from((parent.getContext()));
        View view = inflater.inflate(R.layout.activity_starred_adapter, parent, false);
        return new StarredAdapter.MyViewHolder(view, mOnEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StarredAdapter.MyViewHolder holder, int position) {

        holder.itemView.setTag(subject_array.get(position));
        holder.subject.setText(String.valueOf(subject_array.get(position)));

        int[] androidColors = holder.itemView.getResources().getIntArray(R.array.radnom_colors);
        int color = androidColors[new Random().nextInt(androidColors.length)];
        holder.starred_icon.setBackgroundColor(color);


    }

    @Override
    public int getItemCount() {
        return subject_array.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView subject;
        ImageView starred_icon;

        OnStarredEventListener onStarredEventListener;

        public MyViewHolder(@NonNull View itemView, OnStarredEventListener onStarredEventListener) {
            super(itemView);
            subject = itemView.findViewById(R.id.mail_subject);
            starred_icon = itemView.findViewById(R.id.starred_icon);
            this.onStarredEventListener = onStarredEventListener;

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            onStarredEventListener.onStarredEventClick(getAdapterPosition());
        }
    }
    public interface OnStarredEventListener{
        void onStarredEventClick(int position);
    }
}
