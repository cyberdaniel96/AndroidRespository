package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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

import domain.Lodging;
import service.Converter;

public class ViewLeaseStatus extends AppCompatActivity {
    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    String receiverClientId = "serverLSSserver";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    List<Lodging> list = new ArrayList<>();
    RecyclerView recyclerView;
    ViewLeaseStatusAdapter adapter;
    ProgressDialog pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lease_status);
        clientId = "16104809";

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");


        recyclerView = (RecyclerView) findViewById(R.id.viewLeaseStatus);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ViewLeaseStatusAdapter(this, list);
        recyclerView.setAdapter(adapter);

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
                Log.e("view lease", "onSuccess");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e("view lease", "onFailure");
            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Log.e("view lease", "connectionLost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {
                System.out.println("Message Arrived");
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = c.convertToString(datas[0]);
                String command = head[0];
                String reserve = head[1];
                String senderID = head[2];
                String receiverID = head[3];

                if(receiverID.equals(clientId)){
                    if(command.equals("004849")){

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Retrieve(){
        String command = "004849";
        String reserve = "000000000000000000000000";
        String senderClientID = clientId;


        String tenantID = "1610480";

        String payload = c.convertToHex(new String[]{command, reserve, senderClientID, receiverClientId, ""}) + "$" + c.ToHex(tenantID);
        Publish(payload);
    }

    public void SetData(String message){
        list.clear();
        pb.show();
        String[] splitDollar = message.split("\\$");
        String[] serverData = c.convertToString(splitDollar[0]);

        String lodgingData[] = splitDollar[1].split("@");
        int getLodgingcount = Integer.parseInt(c.convertToString(lodgingData[0])[3]);
        Log.e("out", getLodgingcount + "H");
        for(int i = 0; i < getLodgingcount; i++){
            String[] arrLod = c.convertToString(lodgingData[i]);
            Lodging lodging = new Lodging();
            lodging.setImage(arrLod[0]);
            lodging.setTitle(arrLod[1]);
            lodging.setAddress(arrLod[2].replace("$", ","));

            list.add(lodging);

        }
        adapter.notifyDataSetChanged();
        pb.dismiss();
    }

}
