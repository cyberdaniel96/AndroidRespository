package com.example.johnn.lodgingservicesystemstudent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.List;

import domain.Appointment;
import service.Converter;

public class ViewAppointment extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    String receiverClientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    List<Appointment> list = new ArrayList<>();
    RecyclerView recyclerView;
    ViewAppointmentAdapter adapter;
    ProgressDialog pb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);
        setTitle("View Appointment List");
        SharedPreferences userDetails = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
        clientId = userDetails.getString("UserID","")+7;
        receiverClientId = "serverLSSserver";
        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.viewAppointmentListRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ViewAppointmentAdapter(this, list);
        recyclerView.setAdapter(adapter);


        adapter.setOnItemClickListener(new ViewAppointmentAdapter.MyOnClick() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(ViewAppointment.this, ViewAppointmentDetails.class);
                intent.putExtra("anAppointment", list.get(position));
                startActivity(intent);
            }
        });

        adapter.setOnLongClickListener(new ViewAppointmentAdapter.MyOnLongClick() {
            @Override
            public boolean onItemLongClick(int position, View v) {
                final Appointment app = list.get(position);
                AlertDialog.Builder alert = new AlertDialog.Builder(ViewAppointment.this);
                alert.setTitle("Cancelling Appointment");
                alert.setMessage(Html.fromHtml("Are you sure you want to cancel appointment with "+app.getOwnerID()));
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {

                            cancel(app);
                            finish();
                            startActivity(getIntent());
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
                return true;
            }
        });
        adapter.notifyDataSetChanged();
    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                Retrieve();
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
            public void messageArrived(String s, MqttMessage mqttMessage) {

                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = c.convertToString(datas[0]);
                String command = head[0];
                String reserve = head[1];
                String senderID = head[2];
                String receiverID = head[3];
                if(receiverID.equals(clientId)){
                    if(command.equals("004831")){
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

    public void Retrieve(){
        String payload = c.convertToHex(new String[]{"004831", "000000000000000000000000", clientId, "serverLSSserver",clientId.substring(0, clientId.length()-1)});
        Publish(payload);
    }

    public void SetData(String message){
        list.clear();
        Converter c = new Converter();
        String[] datas = message.split("\\$");
        String[] head = c.convertToString(datas[0]);
        int totalList = Integer.parseInt(head[4]);

        for(int index = 1; index <= totalList; index++){
            String[] data = c.convertToString(datas[index]);
            Appointment app = new Appointment(data[0], data[1], data[2], data[3],data[4],  data[5], data[6], data[7], data[8],data[9]);
            if(!app.getStatus().equals("cancel")){
                list.add(app);
            }
        }
        TextView view = (TextView) findViewById(R.id.txtNoRecord);
        if (list.size() > 0) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
    }


    public void cancel(Appointment app){
        String command = "004829";
        String reserve = "000000000000000000000000";
        String senderClientId = clientId;
        String receiverClientId = "serverLSSserver";

        String appID = app.getAppointmentID();

        String payload =  c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId, appID, app.getOwnerID()});



        Publish(payload);
        Toast.makeText(this, "This Appointment has been cancelled.", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}
