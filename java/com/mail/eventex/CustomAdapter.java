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

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder>{

    private Context context;
    private ArrayList subject_array;
    private OnEventListener mOnEventListener;
//    itemClicked activty;


    CustomAdapter(Context context, ArrayList subject, OnEventListener onEventListener){
        this.context =  context;
        subject_array = subject;
        this.mOnEventListener = onEventListener;
    }

    @NonNull
    @Override
    public CustomAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from((parent.getContext()));
        View view = inflater.inflate(R.layout.activity_custom_adapter, parent, false);
        return new MyViewHolder(view, mOnEventListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomAdapter.MyViewHolder holder, int position) {

        holder.itemView.setTag(subject_array.get(position));
        holder.subject.setText(String.valueOf(subject_array.get(position)));


        int[] androidColors = holder.itemView.getResources().getIntArray(R.array.radnom_colors);
        int color = androidColors[new Random().nextInt(androidColors.length)];
        holder.icon_mail.setBackgroundColor(color);


    }

    @Override
    public int getItemCount() {
        return subject_array.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView subject;
        ImageView icon_mail;

        OnEventListener onEventListener;

        public MyViewHolder(@NonNull View itemView,OnEventListener onEventListener) {
            super(itemView);
            subject = itemView.findViewById(R.id.mail_subject);
            icon_mail = itemView.findViewById(R.id.mail_icon);
            this.onEventListener = onEventListener;

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            onEventListener.onEventClick(getAdapterPosition());
        }
    }
    public interface OnEventListener{
        void onEventClick(int position);
    }

}
