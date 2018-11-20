package com.example.johnn.lodgingservicesystemstudent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;

import domain.Lodging;
import domain.Rental;
import service.Converter;

public class ViewRentalLodging extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "1610480"+9;
    String receiverClientId = "serverLSSserver";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    ArrayList<Lodging> lodgingArrayList = new ArrayList<>();
    Rental rental = new Rental();
    RecyclerView recyclerView;
    ViewLeaseStatusAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rental_lodging);

        recyclerView = (RecyclerView) findViewById(R.id.viewRentalList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ViewLeaseStatusAdapter(this, lodgingArrayList);
        recyclerView.setAdapter(adapter);

        adapter.setOnClickListener(new ViewLeaseStatusAdapter.OnClickListener() {
            @Override
            public void onClick(View v, int position) {
                Retreive("RENTAL", rental.getRentalID());
            }
        });
    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                Retreive("LODGING", clientId.substring(0, clientId.length()-1));

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
                String mycommand = head[5];
                if(receiverID.equals(clientId)){
                    if(command.equals("004853")){
                        if(mycommand.equals("LODGING")){
                            SetLodgingData(mqttMessage.toString());
                        }
                        if(mycommand.equals("RENTAL")){
                            SetRentalData(mqttMessage.toString());
                        }
                    }
;                }

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

    public void Retreive(String mycommand, String id){
        String command = "004853";
        String reserve = "000000000000000000000000";
        String senderClientID = clientId;


        String head = c.convertToHex(new String[]{command, reserve, senderClientID, receiverClientId, ""});
        String body = c.convertToHex(new String[]{id, mycommand});

        Publish(head + "$" + body);
    }

    public void SetLodgingData(String message){
        lodgingArrayList.clear();
        String data[] = message.split("\\$");
        String head[] = c.convertToString(data[0]);
        int size = Integer.parseInt(head[4]);

        for(int index = 1; index <= size; index ++){
            String temp[] = c.convertToString(data[index]);
            Lodging lodging = new Lodging();
            lodging.setTitle(temp[0]);
            lodging.setAddress(temp[1].replace("$", " "));
            lodging.setImage(temp[2]);
            rental.setRentalID(temp[3]);
            lodgingArrayList.add(lodging);
        }
        adapter.notifyDataSetChanged();
    }

    public void SetRentalData(String message){
        String data[] = message.split("\\$");
        String body[] = c.convertToString(data[1]);
        Rental r = new Rental();
        r.setRentalID(body[0]);
        r.setIssueDate(body[1]);
        r.setDueDate(body[2]);
        r.setTotalAmount(Double.parseDouble(body[3]));
        r.setStatus(body[4]);
        r.setLeaseID(body[5]);


    }
}
