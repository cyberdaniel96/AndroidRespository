package com.example.johnn.lodgingservicesystemstudent;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    Button btn1;
    private static MyOnClick clickListener;

    boolean activate = false;

    public ViewAppointmentAdapter(Context context, List<Appointment> list){
        this.context = context;
        this.list = list;

    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ViewHolder(View itemView){
            super(itemView);
            itemView.setOnClickListener(this);
            ownerID = (TextView)itemView.findViewById(R.id.txtOwnerID);
            status  = (TextView)itemView.findViewById(R.id.txtStatus);
            date = (TextView)itemView.findViewById(R.id.txtDate);
            time = (TextView)itemView.findViewById(R.id.txtTime);

            btn1 = (Button)itemView.findViewById(R.id.button1);

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Deleting Message");
                    alert.setMessage(Html.fromHtml("<font color='#FF7F27'>Are you sure you want to delete??</font>"));
                    alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences userDetails = context.getSharedPreferences("LoggedInUser", context.MODE_PRIVATE);
                            String loggedInID  = userDetails.getString("UserID","")+7;
                            try {
                                CancelAppointment cancelAppointment = new CancelAppointment(loggedInID, context, list.get(getAdapterPosition()));

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    });
                    alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
            });
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

            if(activate){
                btn1.setVisibility(View.VISIBLE);
            }else{
                btn1.setVisibility(View.GONE);
            }

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

    public void setButtonVisible(boolean activate){

        this.activate = activate;
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
