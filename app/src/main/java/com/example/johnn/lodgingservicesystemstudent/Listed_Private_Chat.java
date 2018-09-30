package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import domain.Message;
import domain.PrivateChat;
import service.Converter;

public class Listed_Private_Chat extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    int count = 0;
    ProgressDialog pb;
    ListedPrivateChatAdapter adapter;
    private RecyclerView mMessageRecycler;
    List<Message> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listed__private__chat);
        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
        clientId = prefs.getString("UserID", "UserID Not Found!") + 7;


        mMessageRecycler = (RecyclerView) findViewById(R.id.myListedPrivateChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(linearLayoutManager);

        adapter = new ListedPrivateChatAdapter(this, list);
        mMessageRecycler.setAdapter(adapter);
    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

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
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                System.out.println("Message Arrived");
                Converter c = new Converter();
                String datas[] = mqttMessage.toString().split("\\$");
                String[] head = datas[0].split("/");
                String command = c.ToString(head[0]);
                String receiverClientID =  c.ToString(head[3]);
                String[] value = c.convertToString(datas[0]);

                if(receiverClientID.equals(clientId)){
                    if(command.equals("004835")){

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
            pb.dismiss();
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

    public void Retrieve(){
        String command = "004835";
        String reserve = "000000000000000000000000";
        String senderClientId = clientId;//change to client id later
        String receiverClientId = "server";
        String sender = clientId.substring(0, clientId.length() -1);

        String payload = c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId, sender});
        Publish(payload);
    }

    public void SetData(String message){

    }
}
