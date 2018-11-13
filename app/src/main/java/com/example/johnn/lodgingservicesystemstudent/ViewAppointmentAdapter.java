package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import domain.Appointment;

public class ViewAppointmentAdapter extends RecyclerView.Adapter {

    private Context context;
    private List<Appointment> list;

    TextView ownerID;
    TextView status;
    TextView date;
    TextView time;
    ImageView img;
    private static MyOnClick clickListener;
    private static MyOnLongClick longClickListener;

    public ViewAppointmentAdapter(Context context, List<Appointment> list){
        this.context = context;
        this.list = list;

    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        ViewHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            ownerID = (TextView)itemView.findViewById(R.id.txtOwnerID);
            status  = (TextView)itemView.findViewById(R.id.txtStatus);
            date = (TextView)itemView.findViewById(R.id.txtIssueDate);
            time = (TextView)itemView.findViewById(R.id.txtTime);
            img = (ImageView)itemView.findViewById(R.id.image_appointment_user);

        }

        void bind(Appointment appointment){
            ownerID.setText(appointment.getOwnerID());

            if(appointment.getStatus().equals("pending")){
                status.setText(appointment.getStatus());
                status.setTextColor(Color.rgb(255,215,0));
            }
            if(appointment.getStatus().equals("accepted")){
                status.setText(appointment.getStatus());
                status.setTextColor(Color.rgb(124,252,0));
            }
            if(appointment.getStatus().equals("rejected")){
                status.setText(appointment.getStatus());
                status.setTextColor(Color.rgb(255,0,0));
            }

            String[] dateTime = appointment.getDateTime().split("AND");
            date.setText(dateTime[0]);
            time.setText(dateTime[1]);

            String imageAddress = "http://"+Home.ip+"/img/User/"+ownerID.getText().toString()+".jpg";
            Glide.with(itemView.getContext())
                    .load(imageAddress)
                    .transform(new CircleTransform(itemView.getContext()))
                    .override(50,50)
                    .into(img);
        }


        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition(), view);
        }


        @Override
        public boolean onLongClick(View v) {
            longClickListener.onItemLongClick(getAdapterPosition(), v);
            return true;
        }
    }

    public interface MyOnClick{
        void onItemClick(int position, View v);
    }

    public interface MyOnLongClick{
        boolean onItemLongClick(int position, View v);
    }

    public void setOnItemClickListener(MyOnClick myOnClick){
        ViewAppointmentAdapter.clickListener = myOnClick;
    }

    public void setOnLongClickListener(MyOnLongClick myOnLongClick){
        ViewAppointmentAdapter.longClickListener = myOnLongClick;
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
