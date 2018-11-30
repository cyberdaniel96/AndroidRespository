package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import domain.Message;
import domain.PrivateChat;
import service.Converter;

public class PrivateChatList extends AppCompatActivity {
    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    int count = 0;
    List<Message> ml = new ArrayList<>();
    List<Message> tempml = new ArrayList<>();
    ProgressDialog pb;


    //Layout Tools
    private Button btnSend;
    private EditText messageBody;

    private RecyclerView mMessageRecycler;
    private PrivateChatAdapter privateChatAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat_list);
        setTitle(getIntent().getStringExtra("lodgingOwner").replace("Owner ID: ",""));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
        clientId = prefs.getString("UserID", "UserID Not Found!") + 7;

        //start

        Intent intent1 = getIntent();

        try{
            String value = intent1.getStringExtra("Launched");
            if(value == null && !value.equals(value) && value.isEmpty()){


                startActivity(intent1);
            }
        }catch(Exception ex){
            Intent intent = new Intent(this, PrivateChat.class);
            intent.putExtra("Launched", "YES");
        }



        //end

        messageBody = (EditText) findViewById(R.id.edittext_chatbox);
        messageBody.setText("");
        btnSend = (Button) findViewById(R.id.button_chatbox_send);

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        mMessageRecycler = (RecyclerView) findViewById(R.id.privatechatRV);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(linearLayoutManager);

        privateChatAdapter = new PrivateChatAdapter(this, ml);
        privateChatAdapter.setID(clientId);
        mMessageRecycler.setAdapter(privateChatAdapter);

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

                if(command.equals("004833")){
                    if(receiverClientID.equals(clientId)){
                        ml.add(new PrivateChat(value[8],value[4],value[5],value[6],value[7]));
                        privateChatAdapter.setID(clientId);
                        privateChatAdapter.notifyDataSetChanged();
                        mMessageRecycler.smoothScrollToPosition(privateChatAdapter.getItemCount()-1);
                    }else if(receiverClientID.equals(value[6]+7) && (value[7]+7).equals(clientId)){
                        ml.add(new PrivateChat(value[8],value[4],value[5],value[6],value[7]));
                        privateChatAdapter.setID(clientId);
                        privateChatAdapter.notifyDataSetChanged();
                        mMessageRecycler.smoothScrollToPosition(privateChatAdapter.getItemCount()-1);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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

    public void send(View v) throws Exception {
        String newContent = messageBody.getText().toString();
        //check is empty body
        if(newContent.compareTo("") == 0 && newContent == null){
            Toast.makeText(this, "Please Enter Message!!", Toast.LENGTH_LONG).show();
            return;
        }else if(newContent.length() > 999 ){
            Toast.makeText(this, "Text is too long", Toast.LENGTH_LONG).show();
            return;
        }

        String command = "004833";
        String reserve = "000000000000000000000000";
        String senderClientId = clientId;
        String receiverClientId = "serverLSSserver";

        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date = new Date();

        String sentTime = formatter.format(date);
        String sender = clientId.substring(0, clientId.length() -1);
        Intent intent = getIntent();
        String receiver = intent.getStringExtra("lodgingOwner").replace("Owner ID: ",""); //lodging owner
        String payload = c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId,newContent,sentTime, sender, receiver});

        if(sender.compareTo(receiver) == 0){
            Toast.makeText(this, "YOU CANNOT SEND TO YOURSELF..!",Toast.LENGTH_LONG).show();
            return;
        }
        messageBody.setText("");
        Publish(payload);
    }

    public void SetData(String message) throws Exception{
        ml.clear();
        pb.show();
        String datas[] = message.toString().split("\\$");
        for (String tempDatas: datas){
            if(tempDatas.charAt(tempDatas.length()-1) == '\\'){
                tempDatas = tempDatas.substring(0,tempDatas.length()-1);
            }
            String[] data = c.convertToString(tempDatas);
            if(!(data[0].compareTo("004835") == 0)){
                if(data[5].compareTo("NOTHING") != 0){
                    String[] value = data[5].split("AND");
                    String myID = clientId.substring(0, clientId.length()-1);
                    int length = value.length;
                    if(length == 1){
                        if(!value[0].equals(myID)){//"myID" deleted the chat
                            Message message1 = new PrivateChat(data[0],data[1],data[2],data[3],data[4]);
                            ml.add(message1);
                        }
                    }

                    if(length == 2){
                        String compa = data[3]+"AND"+data[4];
                        String compa2 = data[4]+"AND"+data[3];
                        if(compa.equals(data[5]) || compa2.equals(data[5])){

                        }else{
                            Message message1 = new PrivateChat(data[0],data[1],data[2],data[3],data[4]);
                            ml.add(message1);
                        }
                    }
                }else {
                    Message message1 = new PrivateChat(data[0],data[1],data[2],data[3],data[4]);
                    ml.add(message1);
                }
            }
        }

        privateChatAdapter.notifyDataSetChanged();


        pb.dismiss();
    }


    public void Retrieve() {
        String command = "004835";
        String reserve = "000000000000000000000000";
        String senderClientId = clientId;//change to client id later
        String receiverClientId = "serverLSSserver";
        String sender = clientId.substring(0, clientId.length() -1);
        Intent intent = getIntent();
        String receiverID = intent.getStringExtra("lodgingOwner").replace("Owner ID: ",""); //lodging owner
        System.out.println(receiverID);
        String payload = c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId, sender, receiverID});
        Publish(payload);

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
}


