package com.example.johnn.lodgingservicesystemstudent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
    String broker = "tcp://test.mosquitto.org:1883";
    String clientId = "";
    String receiverClientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    List<Appointment> list = new ArrayList<>();
    RecyclerView recyclerView;
    ViewAppointmentAdapter adapter;
    boolean boolCacnel = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);

       // SharedPreferences userDetails = getSharedPreferences("userdetails", MODE_PRIVATE);
       // System.out.println("shared preference " + userDetails.getString("LoggedInUser",""));
        clientId = "16104807";
        receiverClientId = "serverLSSserver";

        recyclerView = (RecyclerView) findViewById(R.id.viewAppointmentListRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new ViewAppointmentAdapter(this, list);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.appointment_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.appCancel:
                if(!boolCacnel){
                    adapter.setButtonVisible(true);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    boolCacnel = true;
                    break;
                }else{
                    adapter.setButtonVisible(false);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    boolCacnel = false;
                    break;
                }

        }

        return true;
    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                GetData();
               // Toast.makeText(ViewAppointment.this, "onSuccess", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
               // Toast.makeText(ViewAppointment.this, "onFailure", Toast.LENGTH_LONG).show();
            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
             //   Toast.makeText(ViewAppointment.this, "connection lost", Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {

                String[] datas = mqttMessage.toString().split("\\$");
                String data[] = datas[0].split("/");
                String command = c.ToString(data[0]);
                String receiverClientId = c.ToString(data[3]);
                if (receiverClientId.equals(clientId)) {
                    if(command.equals("004831")){
                        if(c.ToString(data[4]).equals("Success")){
                            SetData(mqttMessage.toString());
                        }
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

    private void GetData(){
        String payload = c.convertToHex(new String[]{"004831", "000000000000000000000000", clientId, receiverClientId,clientId.substring(0,clientId.length()-1)});
        Publish(payload);
    }

    private void SetData(String message){

        String[] splitDollar = message.split("\\$");

       list = new ArrayList<>();

        int loop = splitDollar.length-1;
        for (int count = 1; count <= splitDollar.length; count++) {
            String[] data = c.convertToString(splitDollar[count]);

            Appointment app = new Appointment(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
            if(!app.getStatus().equals("cancel")){
                list.add(app);
            }
           if(loop == count){
                break;
           }
        }

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

        adapter.notifyDataSetChanged();
    }
}
