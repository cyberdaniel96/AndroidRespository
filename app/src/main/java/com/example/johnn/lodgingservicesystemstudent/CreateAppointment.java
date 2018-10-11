package com.example.johnn.lodgingservicesystemstudent;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import service.Converter;

public class CreateAppointment extends AppCompatActivity {


    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
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
    String txtLocation = "";
    TextView txtownerID;
    TextView txttenantID;
    TextView txtlodgingID;
    TextView txtstatus;
    EditText txtcomment;
    Button submitApp;

    Map<String, String> statemap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_appointment);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();



        clientID =  intent.getStringExtra("clientID") + "8";
        serverID = "serverLSSserver";
        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        statemap = new HashMap<>();

        txtAppointmentID = (TextView)findViewById(R.id.txtAppointmentID);
        spinDate = (Spinner)findViewById(R.id.spinDate);
        spinTime = (Spinner)findViewById(R.id.spinTime);
        spinLocation = (Spinner)findViewById(R.id.spinnerLocation);
        txtownerID = (TextView)findViewById(R.id.txtOwnerID);
        txttenantID = (TextView)findViewById(R.id.txtTenantID);
        txtlodgingID = (TextView)findViewById(R.id.txtLodgingID);
        txtstatus = (TextView)findViewById(R.id.txtStatus);
        txtcomment = (EditText)findViewById(R.id.txtComment);
        submitApp = (Button)findViewById(R.id.btnCreateAppointment);
        txtownerID.setText(""+intent.getStringExtra("ownerID"));
        txttenantID.setText(""+intent.getStringExtra("clientID"));
        txtlodgingID.setText(""+intent.getStringExtra("lodgingID"));
        //start:temporary data
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

        List<String> stateList = new ArrayList<>();
        String[] stateResourceArr = getResources().getStringArray(R.array.state);
        for(String tempData: stateResourceArr){
            String[] stateSplit = tempData.split("-");
            statemap.put(stateSplit[0], stateSplit[1]);
            stateList.add(stateSplit[0]);
        }

        String[] stateArr = stateList.toArray(new String[stateList.size()]);
        ArrayAdapter<String> stateAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, stateArr);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinLocation.setAdapter(stateAdapter);

    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this, broker, clientID, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Toast.makeText(getApplication(), "onSuccess", Toast.LENGTH_LONG).show();
                Subscribe();
                GetID();
            }
//
            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Toast.makeText(getApplication(), "onFailure", Toast.LENGTH_LONG).show();
            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                 Toast.makeText(getApplication(), "connectionLost", Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if(receiverClientId.equals(clientID)) {
                    if(command.equals("004839")){
                        setID(mqttMessage.toString());
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
            Toast.makeText(this,"onPause", Toast.LENGTH_LONG).show();
            client.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GetID(){
        String payload = c.convertToHex(new String[]{"004839", "000000000000000000000000",clientID,serverID});
        Publish(payload);
    }

    public void setID(String message){
        String[] data = c.convertToString(message);
        String id = data[4];
        System.out.println(id);
        txtAppointmentID.setText(id);
    }

    private class SubmitApp implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            String appointmentID = txtAppointmentID.getText().toString();
            String dateTime = spinDate.getSelectedItem().toString()+"AND"+spinTime.getSelectedItem().toString();
            //String reason = "";
            String state = spinLocation.getSelectedItem().toString();
            String priority = "";
            if(!statemap.isEmpty()){
                if(statemap.containsKey(state)){
                    priority  = statemap.get(state);
                }
            }else{
                priority = "NOTHING";
            }
            String comment = txtcomment.getText().toString();
            String status = txtstatus.getText().toString();
            String lodgingID = txtlodgingID.getText().toString();
            String tenantID = txttenantID.getText().toString();
            String ownerID  = txtownerID.getText().toString();

            String payload = c.convertToHex(new String[]{"004823", "000000000000000000000000", clientID, serverID,
                    appointmentID, dateTime, state, priority, comment, status, lodgingID, tenantID, ownerID});

            Publish(payload);
            Toast.makeText(CreateAppointment.this, "Creating Appointment....", Toast.LENGTH_LONG).show();
        }
    }

   public void tell(View view){

   }

}

