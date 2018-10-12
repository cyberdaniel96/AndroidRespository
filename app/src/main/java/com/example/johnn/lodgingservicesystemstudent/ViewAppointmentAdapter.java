package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.RecursiveAction;

import domain.Appointment;
import domain.Message;
import domain.PrivateChat;

public class ViewAppointmentAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Appointment> list;

    TextView ownerID;
    TextView status;
    TextView date;
    TextView time;

    private static MyOnClick clickListener;
    public ViewAppointmentAdapter(Context context, List<Appointment> list){
        this.context = context;
        this.list = list;

    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ViewHolder(View itemView){
            super(itemView);

            ownerID = (TextView)itemView.findViewById(R.id.txtOwnerID);
            status  = (TextView)itemView.findViewById(R.id.txtStatus);
            date = (TextView)itemView.findViewById(R.id.txtDate);
            time = (TextView)itemView.findViewById(R.id.txtTime);
        }

        void bind(Appointment appointment){
            ownerID.setText(""+appointment.getOwnerID());
            status.setText(""+appointment.getStatus());
            String[] dateTime = appointment.getDateTime().split("AND");
            date.setText(""+dateTime[0]);
            time.setText(""+dateTime[1]);
        }

        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);


        }
    }

    public interface MyOnClick{
        void onItemClick(int position, View v);
    }

    public void setOnItemClickListener(MyOnClick myOnClick){
        ViewAppointmentAdapter.clickListener = myOnClick;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_appointment_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Appointment appointment = (Appointment) list.get(position);
        ((ViewHolder) holder).bind(appointment);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
