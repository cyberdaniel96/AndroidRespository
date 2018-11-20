package com.example.johnn.lodgingservicesystemstudent;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import domain.CodeGenerator;
import domain.Message;
import domain.PrivateChat;
import service.Converter;

public class CodeChoosePeople extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    String receiverClientID = "serverLSSserver";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    ProgressDialog pb;
    ListedPrivateChatAdapter adapter;
    private RecyclerView mMessageRecycler;
    List<Message> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_choose_people);

        setTitle("Code Generator");
        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        setContentView(R.layout.activity_listed__private__chat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences prefs = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
        clientId = prefs.getString("UserID", "UserID Not Found!") + 7;

        mMessageRecycler = (RecyclerView) findViewById(R.id.myListedPrivateChat);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(false);
        mMessageRecycler.setLayoutManager(linearLayoutManager);


        adapter = new ListedPrivateChatAdapter(this, list);
        mMessageRecycler.setAdapter(adapter);
        pb.dismiss();

        adapter.setOnItemClickListener(new ListedPrivateChatAdapter.MyOnClick(){
            @Override
            public void onItemClick(int position, final View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(CodeChoosePeople.this);
                alert.setTitle("Confirmation");
                alert.setMessage(Html.fromHtml("<font color='#FF7F27'>Are you sure you want to send to this person?</font>"));
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                TextView name = v.findViewById(R.id.nametxt);
                Intent intent = getIntent();
                String randomCode = intent.getStringExtra("myCode");
                String head = c.convertToHex(new String[]{
                   "004847","000000000000000000000000",clientId,receiverClientID,""
                });
                CodeGenerator code = new CodeGenerator("",new Date(),new Date(),Integer.parseInt( randomCode),clientId.substring(0, clientId.length()-1));
                String body = c.convertToHex(new String[]{
                        code.getIssueDate(),
                        code.getIssueTime(),
                        String.format("%d", code.getVerifyCode()),
                        code.getUserID(),
                        name.getText().toString()

                });

                String payload = head + "$" + body;
                Publish(payload);
                sendToChat("Code: "+randomCode,code.getIssueDate(),code.getIssueTime(),clientId.substring(0, clientId.length()-1),name.getText().toString());
                //codeNotifying(name.getText().toString(), randomCode);
                finish();
                Toast.makeText(getApplication(), "Code has been sent to owner: "+name.getText().toString(), Toast.LENGTH_LONG).show();
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alert.show();
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
                if(receiverClientID.equals(clientId)){
                    if(command.equals("004835")){
                        if(list.isEmpty() && list.size() == 0) {
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

    public void sendToChat(String content,String date, String time, String sender, String receiver){
        String command = "004833";
        String reserve = "000000000000000000000000";
        String senderClientId = clientId;
        String receiverClientId = "serverLSSserver";

        String payload = c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId,
        content, date + " " + time, sender, receiver});
        Publish(payload);
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

    public void Retrieve() {
        String command = "004835";
        String reserve = "000000000000000000000000";
        String senderClientId = clientId;//change to client id later
        String receiverClientId = "serverLSSserver";
        String sender = clientId.substring(0, clientId.length() - 1);

        String payload = c.convertToHex(new String[]{command, reserve, senderClientId, receiverClientId, sender, "none"});
        Publish(payload);
    }

    public void SetData(String message){
        list.clear();
        List<String> longData = new ArrayList<>();
        List<Message> appList = new ArrayList<>();
        List<String> listConsists = new ArrayList<>();
        String datas[] = message.split("\\$");
        for (String tempDatas: datas){
            longData.add(tempDatas);
        }

        for(int count = 1; count < longData.size(); count++){
            String data[] = c.convertToString(longData.get(count));
            Message message1 = new PrivateChat(data[0],data[1],data[2],data[3],data[4]);
            ((PrivateChat) message1).setDelStatus(data[5]);
            appList.add(message1);
        }

        for(int index = 0; index < appList.size(); index++){
            PrivateChat chat = (PrivateChat) appList.get(index);
            SharedPreferences prefs = getSharedPreferences("LoggedInUser", MODE_PRIVATE);
            if(!chat.getReceiverID().equals(prefs.getString("UserID", "UserID Not Found!"))) {


                String splitStatus[] = chat.getDelStatus().split("AND");

                if (splitStatus[0].equals("NOTHING")) {
                    if (list.isEmpty()) {
                        list.add(chat);
                        listConsists.add(chat.getReceiverID());
                    } else if (!list.isEmpty()) {
                        TextView visibility = (TextView) findViewById(R.id.txtNoRecord);
                        visibility.setVisibility(View.GONE);
                        if (!listConsists.contains(chat.getReceiverID())) {
                            list.add(chat);
                        }
                    }
                }
            }
        }

        if(listConsists.isEmpty()){
            TextView visibility = (TextView)findViewById(R.id.txtNoRecord);
            visibility.setVisibility(View.VISIBLE);
        }

        adapter.notifyDataSetChanged();
        pb.dismiss();

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

    public void codeNotifying(String ownerID, String code){
        String serverData = c.convertToHex(new String[]{
                "004841",
                "000000000000000000000000",
                clientId,
                "serverLSSserver",
                "",
        });

        String notificationData = c.convertToHex(new String[]{"Lodging Service System",
                "Your verification code is " + code,
                "VERIFICATION RECEIVED",
                ownerID});

        String resourcesData =  "Hello" + "@" +"world";

        String servicePayload = serverData + "$" + notificationData + "$" + resourcesData;

    }
}
