package com.example.johnn.lodgingservicesystemstudent;

import android.app.AlertDialog;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.File;
import java.nio.file.FileSystem;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domain.VisitTime;
import service.Converter;

public class CreateAppointment extends AppCompatActivity {


    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();
    ProgressDialog pb;


    String clientID = "";
    String serverID = "";

    TextView txtAppointmentID;
    Spinner spinDate;
    Spinner spinTime;
    Spinner spinLocation;
    String txtDate = "";
    String txtTime = "";
    TextView txtownerID;
    TextView txttenantID;
    TextView txtlodgingID;
    TextView txtstatus;
    EditText txtcomment;
    Button submitApp;

    Map<String, String> statemap;
    HashMap<String , List<String>> map = new HashMap<>();

    List<String> dateList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();


        clientID = intent.getStringExtra("clientID") + "8";
        serverID = "serverLSSserver";
        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        statemap = new HashMap<>();

        txtAppointmentID = (TextView) findViewById(R.id.txtAppointmentID);
        spinDate = (Spinner) findViewById(R.id.spinDate);
        spinTime = (Spinner) findViewById(R.id.spinTime);
        spinLocation = (Spinner) findViewById(R.id.spinnerLocation);
        txtownerID = (TextView) findViewById(R.id.txtOwnerID);
        txttenantID = (TextView) findViewById(R.id.txtTenantID);
        txtlodgingID = (TextView) findViewById(R.id.txtLodgingID);
        txtstatus = (TextView) findViewById(R.id.txtStatus);
        txtcomment = (EditText) findViewById(R.id.txtComment);
        submitApp = (Button) findViewById(R.id.btnCreateAppointment);
        txtownerID.setText("" + intent.getStringExtra("ownerID"));
        txttenantID.setText("" + intent.getStringExtra("clientID"));
        txtlodgingID.setText("" + intent.getStringExtra("lodgingID"));

        submitApp.setOnClickListener(new SubmitApp());
        //start:temporary data


        spinDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String date = spinDate.getItemAtPosition(position).toString();
                List<String> myTime = map.get(date);
                Toast.makeText(getApplicationContext(), myTime.isEmpty()+"", Toast.LENGTH_LONG).show();
                String[] timeArray = myTime.toArray(new String[myTime.size()]);
                ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(CreateAppointment.this, android.R.layout.simple_spinner_item, timeArray);
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinTime.setAdapter(timeAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        List<String> stateList = new ArrayList<>();
        String[] stateResourceArr = getResources().getStringArray(R.array.state);
        for (String tempData : stateResourceArr) {
            String[] stateSplit = tempData.split("-");
            statemap.put(stateSplit[0], stateSplit[1]);
            stateList.add(stateSplit[0]);
        }

        String[] stateArr = stateList.toArray(new String[stateList.size()]);
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, stateArr);
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinLocation.setAdapter(stateAdapter);

    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this, broker, clientID, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {

                Subscribe();
                GetID();
            }
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if (receiverClientId.equals(clientID)) {
                    if (command.equals("004839")) {
                        setID(mqttMessage.toString());
                        GetAvailableTime();
                    }
                    if (command.equals("004823")) {
                        String results = c.ToString(datas[5]);

                        if ("Success".compareTo(results) == 0) {
                            Toast.makeText(CreateAppointment.this, "Appointment Created", Toast.LENGTH_LONG).show();
                        }
                        if ("Fail".compareTo(results) == 0) {
                            Toast.makeText(CreateAppointment.this, "Appointment Create Failed", Toast.LENGTH_LONG).show();
                        }
                    }

                    if (command.equals("004852")) {

                        SetData(mqttMessage.toString());
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    public void Subscribe() {
        try {
            client.subscribe(topic, 1);
            pb.dismiss();
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
        pb.show();
        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {

            client.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetAvailableTime() {
        String payload = c.convertToHex(new String[]{"004852", "000000000000000000000000", clientID, serverID, txtlodgingID.getText().toString()});
        Publish(payload);
    }

    public void GetID() {
        String payload = c.convertToHex(new String[]{"004839", "000000000000000000000000", clientID, serverID});
        Publish(payload);
    }

    public void setID(String message) {
        String[] data = c.convertToString(message);
        String id = data[4];
        txtAppointmentID.setText(id);
    }

    public void SetData(String message) {
        map.clear();
        String[] datas = message.split("\\$");
        String[] head = datas[0].split("/");
        int size = Integer.parseInt(c.ToString(head[4]));
        for (int i = 0; i < size; i++) {
            String[] body = datas[i + 1].split("/");
            VisitTime vt = new VisitTime(
                    c.ToString(body[0]), c.ToString(body[1]), c.ToString(body[2]), c.ToString(body[3]), c.ToString(body[4]), Integer.parseInt(c.ToString(body[5]))
            );
                String date = vt.getVisitDateTime().split(" ")[0];

            String input = vt.getVisitDateTime().split(" ")[1];
            DateFormat inputFormat = new SimpleDateFormat("HH:mm");
            DateFormat outputFormat = new SimpleDateFormat("HH:mm aa");
            String time = "";
            try {
                time = outputFormat.format(inputFormat.parse(input)).toUpperCase();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if(map.isEmpty()){
                List<String> temp  = new ArrayList<>();
                temp.add(time);
                map.put(date,temp);
            }

            if(!map.isEmpty()){
                if(map.containsKey(date)){
                    map.get(date).add(time);
                }
                if(!map.containsKey(date)){
                    List<String> temp  = new ArrayList<>();
                    temp.add(time);
                    map.put(date, temp);
                }
            }
        }

        for ( String key : map.keySet() ) {
            dateList.add(key);
        }

        String[] dateArray = dateList.toArray(new String[dateList.size()]);
        ArrayAdapter<String> dateAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dateArray);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDate.setAdapter(dateAdapter);

    }

    private class SubmitApp implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String appointmentID = txtAppointmentID.getText().toString();
            String dateTime = spinDate.getSelectedItem().toString() + "AND" + spinTime.getSelectedItem().toString();
            //String reason = "";
            String state = spinLocation.getSelectedItem().toString();
            String priority = "";
            if (!statemap.isEmpty()) {
                if (statemap.containsKey(state)) {
                    priority = statemap.get(state);
                }
            } else {
                priority = "NOTHING";
            }
            String comment = txtcomment.getText().toString();
            String status = txtstatus.getText().toString();
            String lodgingID = txtlodgingID.getText().toString();
            String tenantID = txttenantID.getText().toString();
            String ownerID = txtownerID.getText().toString();

            String payload = c.convertToHex(new String[]{"004823", "000000000000000000000000", clientID, serverID,
                    appointmentID, dateTime, state, priority, comment, status, lodgingID, tenantID, ownerID});

            Publish(payload);
            Toast.makeText(CreateAppointment.this, "Creating Appointment....", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateAppointment.this);
            builder.setTitle("Message");
            builder.setMessage("You have made the appointment with " + txtownerID.getText().toString())
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

