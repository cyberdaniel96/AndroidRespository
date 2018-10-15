package com.example.johnn.lodgingservicesystemstudent;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import domain.Appointment;

public class ViewAppointmentDetails extends AppCompatActivity {

    TextView txtAppointmentID;
    TextView txtDate;
    TextView txtTime;
    TextView txtownerID;
    TextView txttenantID;
    TextView txtlodgingID;
    TextView txtstatus;
    TextView txtReason;
    TextView viewReason;

    String clientID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment_details);

        Intent intent = getIntent();
        Appointment app = (Appointment) intent.getSerializableExtra("anAppointment");
        clientID = "1610480";

        Log.e("status", app.getStatus());
        txtAppointmentID = (TextView)findViewById(R.id.txtAppointmentID);
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtTime = (TextView)findViewById(R.id.txtTime);
        txtownerID = (TextView)findViewById(R.id.txtOwnerID);
        txttenantID = (TextView)findViewById(R.id.txtTenantID);
        txtlodgingID = (TextView)findViewById(R.id.txtLodgingID);
        txtstatus = (TextView)findViewById(R.id.txtStatus);

        txtReason = (TextView)findViewById(R.id.txtReason);
        viewReason = (TextView)findViewById(R.id.viewReason);
        txtReason.setVisibility(View.GONE);
        viewReason.setVisibility(View.GONE);

        txtAppointmentID.setText(app.getAppointmentID());
        String[] dateTime = app.getDateTime().split("AND");
        txtDate.setText(dateTime[0]);
        txtTime.setText(dateTime[1]);
        txtownerID.setText(app.getOwnerID());
        txttenantID.setText(app.getTenantID());
        txtlodgingID.setText(app.getLodgingID());

        if(app.getStatus().equals("pending")){
            txtstatus.setText(app.getStatus());
            txtstatus.setTextColor(Color.rgb(255,215,0));
        }
        if(app.getStatus().equals("accepted")){
            txtstatus.setText(app.getStatus());
            txtstatus.setTextColor(Color.rgb(124,252,0));
        }
        if(app.getStatus().equals("rejected")){
            txtstatus.setText(app.getStatus());
            txtstatus.setTextColor(Color.rgb(255,0,0));
        }

        String[] splitReason = app.getReason().split("AND");
        String reasonTenant = splitReason[0];
        String reasonOwner = splitReason[1];
        String[] splitBYTenant = reasonTenant.split("BY");
        String[] splitBYOwner = reasonOwner.split("BY");

        /*
        * 1. confirm both message are tenant and owner written
        * 2. check is owner or written having the message
        * 3. if having the message then show
        * 4. no then invisible is setted*/

        if(splitBYTenant[1].equals(app.getTenantID()) && splitBYOwner[1].equals(app.getOwnerID())){
            if(app.getStatus().equals("rejected")) {
                if (!splitBYOwner[0].equals("NOTHING")) {
                    viewReason.setVisibility(View.VISIBLE);
                    txtReason.setText(splitBYOwner[0]);
                    txtReason.setVisibility(View.VISIBLE);
                    txtReason.setTextColor(Color.RED);
                }
            }
        }
    }
}
