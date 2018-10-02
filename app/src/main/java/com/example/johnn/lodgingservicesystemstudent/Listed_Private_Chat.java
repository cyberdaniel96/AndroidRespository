package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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



    TextView nametxt;
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

        nametxt = (TextView)findViewById(R.id.nametxt);


        mMessageRecycler = (RecyclerView) findViewById(R.id.myListedPrivateChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        mMessageRecycler.setLayoutManager(linearLayoutManager);

        adapter = new ListedPrivateChatAdapter(this, list);
        mMessageRecycler.setAdapter(adapter);
        pb.dismiss();

        adapter.setOnItemClickListener(new ListedPrivateChatAdapter.MyOnClick() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(Listed_Private_Chat.this, PrivateChatList.class);
                Message message2 = (PrivateChat)list.get(position);
                intent.putExtra("lodgingOwner",((PrivateChat) message2).getReceiverID());
                Toast.makeText(getApplication(), ((PrivateChat) message2).getReceiverID(), Toast.LENGTH_LONG).show();
                startActivity(intent);
            }
        });


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
//        pb.show();
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
        String receiverClientId = "serverLSSserver";
        String sender = clientId.substring(0, clientId.length() -1);

        String payload = c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId, sender,"none"});
        Publish(payload);
    }

    public void SetData(String message){
        list.clear();
        List<String> temp = new ArrayList<>();
        String datas[] = message.toString().split("\\$");
        for (String tempDatas: datas){
            if(tempDatas.charAt(tempDatas.length()-1) == '\\'){
                tempDatas = tempDatas.substring(0,tempDatas.length()-1);
            }
            String[] data = c.convertToString(tempDatas);
            if(!(data[0].compareTo("004835") == 0)){
                Message message1 = new PrivateChat(data[0],data[1],data[2],data[3],data[4]);
                String tempvalue = "http://192.168.0.153/img/User/"+data[4]+".jpg";

                //delStatus: determine who delete the chat
                if(data[5].compareTo("NOTHING") != 0){
                    String[] value = data[5].split("AND");
                    int length = value.length;
                    if(length == 1){
                        if(!value[0].equals(clientId.substring(0, clientId.length()-1))){//who deleted the room
                            if(!temp.contains(tempvalue)){
                                temp.add(tempvalue);
                                message1.setImage(tempvalue);
                                list.add(message1);
                            }
                        }
                    }

                    if(length == 2){
                        String compa = data[3]+"AND"+data[4];
                        String compa2 = data[4]+"AND"+data[3];
                        if(compa.equals(data[5]) || compa2.equals(data[5])){

                        }else{
                            if(!temp.contains(tempvalue)){
                                temp.add(tempvalue);
                                message1.setImage(tempvalue);
                                list.add(message1);
                            }
                        }
                    }
                }else{
                    if(!temp.contains(tempvalue)){
                        temp.add(tempvalue);
                        message1.setImage(tempvalue);
                        list.add(message1);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        }
    }
}
