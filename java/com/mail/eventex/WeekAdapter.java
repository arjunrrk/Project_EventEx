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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Random;

public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.MyViewHolder> {

    private Context context;
    private ArrayList subject_array;
    private ArrayList day_array;
    private OnWeekEventListener mOnEventListener;


    WeekAdapter (Context context, ArrayList subject, ArrayList day, OnWeekEventListener onWeekEventListener){
        this.context =  context;
        subject_array = subject;
        day_array = day;
        this.mOnEventListener = onWeekEventListener;
    }

    @NonNull
    @Override
    public WeekAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from((parent.getContext()));
        View view = inflater.inflate(R.layout.activity_week_adapter, parent, false);
        return new WeekAdapter.MyViewHolder(view, mOnEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WeekAdapter.MyViewHolder holder, int position) {

        holder.itemView.setTag(subject_array.get(position));
        holder.subject.setText(String.valueOf(subject_array.get(position)));

        int[] androidColors = holder.itemView.getResources().getIntArray(R.array.radnom_colors);
        int color = androidColors[new Random().nextInt(androidColors.length)];
        holder.week_icon.setBackgroundColor(color);
        holder.day.setText(String.valueOf(day_array.get(position)));


    }

    @Override
    public int getItemCount() {
        return subject_array.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView subject;
        TextView day;
        ImageView week_icon;

        OnWeekEventListener onWeekEventListener;

        public MyViewHolder(@NonNull View itemView, OnWeekEventListener onWeekEventListener) {
            super(itemView);
            subject = itemView.findViewById(R.id.mail_subject);
            week_icon = itemView.findViewById(R.id.week_icon);
            day = itemView.findViewById(R.id.day);
            this.onWeekEventListener = onWeekEventListener;

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            onWeekEventListener.onWeekEventClick(getAdapterPosition());
        }
    }
    public interface OnWeekEventListener{
        void onWeekEventClick(int position);
    }
}
