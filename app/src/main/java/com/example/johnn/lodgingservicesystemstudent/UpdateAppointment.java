package com.example.johnn.lodgingservicesystemstudent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import domain.Appointment;
import service.Converter;
import service.SessionManager;

public class UpdateAppointment extends AppCompatActivity {


    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String clientID = "";
    String receiverClientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();


    TextView txtAppointmentID;
    Spinner spinDate;
    Spinner spinTime;
    TextView txtownerID;
    TextView txttenantID;
    TextView txtlodgingID;
    TextView txtstatus;
    TextView txtReason;
    Appointment app;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_appointment);



        SharedPreferences userDetails = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
        clientID = userDetails.getString("UserID","");
        receiverClientId = "serverLSSserver";
        Intent intent = getIntent();
        app = (Appointment) intent.getSerializableExtra("anAppointment");

        txtAppointmentID = (TextView)findViewById(R.id.txtAppointmentID);
        spinDate = (Spinner)findViewById(R.id.spinDate);
        spinTime = (Spinner)findViewById(R.id.spinTime);
        txtownerID = (TextView)findViewById(R.id.txtOwnerID);
        txttenantID = (TextView)findViewById(R.id.txtTenantID);
        txtlodgingID = (TextView)findViewById(R.id.txtLodgingID);
        txtstatus = (TextView)findViewById(R.id.txtStatus);
        txtReason = (TextView)findViewById(R.id.txtReason);

        txtAppointmentID.setText(app.getAppointmentID());
        txtownerID.setText(app.getOwnerID());
        txttenantID.setText(app.getTenantID());


        List<String> dateList = new ArrayList<>();
        dateList.add("10/10/2018");
        dateList.add("9/10/2018");
        String [] dateArray = dateList.toArray(new String[dateList.size()]);
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, dateArray);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDate.setAdapter(dateAdapter);

        List<String> timeList = new ArrayList<>();
        timeList.add("06:30 PM");
        timeList.add("03:30 PM");
        String [] timeArray = timeList.toArray(new String[timeList.size()]);
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, timeArray);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTime.setAdapter(timeAdapter);
    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientID, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();

            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                // Toast.makeText(ViewAppointment.this, "onFailure", Toast.LENGTH_LONG).show();
            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {

                String[] datas = mqttMessage.toString().split("\\$");
                String data[] = datas[0].split("/");
                String command = c.ToString(data[0]);
                String receiverClientId = c.ToString(data[3]);
                if (receiverClientId.equals(clientID)) {

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    public void Subscribe() {
        try {
            client.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Publish(String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateConfirmation(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Remake Appointment");
        builder.setMessage("Are you sure you want to remake the appointment?");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateAppointment();
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void updateAppointment(){
        String appointmentID = txtAppointmentID.getText().toString();
        String date = spinDate.getSelectedItem().toString();
        String time = spinTime.getSelectedItem().toString();
        String ownerID = txtownerID.getText().toString();
        String tenantID = txttenantID.getText().toString();
        String status = "pending";
        String reason = txtReason.getText().toString();

        if(reason.isEmpty() || reason.equals("") || reason == null || reason.contains("AND")){
            txtReason.setError("Field cannot be empty or contain keyword 'AND'");
            return;
        }else{
            Intent intent = getIntent();
            app = (Appointment) intent.getSerializableExtra("anAppointment");
            app.setAppointmentID(appointmentID);
            app.setDateTime(date+"AND"+time);
            app.setOwnerID(ownerID);
            app.setTenantID(tenantID);
            app.setStatus(status);
            String[] splitAnd = app.getReason().split("AND");

            String newReason = reason+"BY"+app.getTenantID() +"AND"+ splitAnd[1];
            app.setReason(newReason);
            String payload = c.convertToHex(new String[]{"004827","000000000000000000000000",clientID,receiverClientId,
                    app.getAppointmentID(),app.getDateTime(),app.getReason(),app.getState(),
            app.getPriority(),app.getComment(),app.getStatus(),app.getLodgingID(),app.getTenantID(),app.getOwnerID()});
            Publish(payload);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Message");
            builder.setMessage("Your request has been send to owner")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences prefs = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
                            Intent intent = new Intent(getApplicationContext(), Home.class);
                            intent.putExtra("UserID", prefs.getString("UserID", "User ID Not Found"));
                            intent.putExtra("Email", prefs.getString("Email", "Email not found"));
                            startActivity(intent);
                            finish();

                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

    }
}

