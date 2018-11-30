package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
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
import service.Converter;

public class ChatRoom extends AppCompatActivity {

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
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        clientId = getIntent().getStringExtra("UserID") + "7";

        final EditText message = (EditText) findViewById(R.id.messageEt);
        Button send = (Button) findViewById(R.id.sendBtn);
        final SearchView s = (SearchView) findViewById(R.id.keywordSV);

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.messageRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


        messageAdapter = new MessageAdapter(ml);
        recyclerView.setAdapter(messageAdapter);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int error = 0;
                if (message.getText().toString().length() < 1 || message.getText().toString().length() > 200) {
                    message.setError("Please type in your message.(Message cannot more than 200 characters.)");
                    error += 1;
                }

                if (error == 0) {
                    Date current = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
                    String time = sdf.format(current.getTime());
                    String command = c.ToHex("004813");
                    String reserve = c.ToHex("000000000000000000000000");
                    String senderClientId = c.ToHex(clientId);
                    String receiverClientId = c.ToHex("serverLSSserver");
                    String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
                    String content = c.ToHex(message.getText().toString());
                    String userId = c.ToHex(getIntent().getStringExtra("UserID"));
                    String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/"
                            + lodgingId + "/" + content + "/" + c.ToHex(time) + "/" + userId;
                    Publish(payload);
                    message.setText("");
                }
            }
        });

        s.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                tempml.clear();
                for(int i=0; i<ml.size(); i++){
                    try{
                        if(ml.get(i).getContent().matches(".*("+query+").*")){
                            Message m = new Message();
                            m = ml.get(i);
                            tempml.add(m);
                        }
                    }catch (Exception e){

                    }
                }
                messageAdapter = new MessageAdapter(tempml);
                recyclerView.setAdapter(messageAdapter);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        s.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                messageAdapter = new MessageAdapter(ml);
                recyclerView.setAdapter(messageAdapter);
                return false;
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
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = datas[0].split("/");
                String command = c.ToString(head[0]);
                String receiverClientId = c.ToString(head[3]);
                if (receiverClientId.equals(clientId)) {
                    if (command.equals("004812")) {
                        String size = c.ToString(head[4]);
                        if (size.equals("0")) {
                            pb.dismiss();
                            Toast.makeText(ChatRoom.this, "No message", Toast.LENGTH_LONG).show();
                        } else {
                            SetData(mqttMessage.toString());
                        }
                    } else if (command.equals("004813")) {
                        Toast.makeText(ChatRoom.this, "Failed to send message.", Toast.LENGTH_LONG).show();
                    }
                } else if (receiverClientId.equals(getIntent().getStringExtra("LodgingID"))) {
                    UpdateMessage(mqttMessage.toString());
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

    public void Retrieve() {
        String command = c.ToHex("004812");
        String reserve = c.ToHex("000000000000000000000000");
        String senderClientId = c.ToHex(clientId);
        String receiverClientId = c.ToHex("serverLSSserver");
        String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
        String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + lodgingId;
        Publish(payload);
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

    public void SetData(String message) throws Exception {

        ml.clear();
        String[] datas = message.split("\\$");
        String[] head = datas[0].split("/");
        int size = Integer.parseInt(c.ToString(head[4]));
        for (int i = 0; i < size; i++) {
            Message m = new Message();
            String[] body = datas[i + 1].split("/");
            m.setMessageId(c.ToString(body[0]));
            m.setContent(c.ToString(body[1]));
            m.setSentTime(c.ToString(body[2]));
            m.setUserId(c.ToString(body[3]));
            m.setName(c.ToString(body[4]));
            m.setImage(c.ToString(body[5]));
            m.setLodgingId(c.ToString(body[6]));
            ml.add(m);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.messageRV);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);


        messageAdapter = new MessageAdapter(ml);
        recyclerView.setAdapter(messageAdapter);
        pb.dismiss();
        messageAdapter.setOnItemClickListener(new MessageAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                if (ml.get(position).getUserId().equals(getIntent().getStringExtra("UserID"))) {
                    Intent intent = new Intent(ChatRoom.this, ViewMessage.class);
                    intent.putExtra("MessageID", ml.get(position).getMessageId());
                    intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
                    startActivity(intent);
                }
            }
        });
    }


    public void UpdateMessage(String message) {
        Message m = new Message();
        String[] datas = message.split("/");
        m.setMessageId(c.ToString(datas[4]));
        m.setContent(c.ToString(datas[5]));
        m.setSentTime(c.ToString(datas[6]));
        m.setUserId(c.ToString(datas[7]));
        m.setName(c.ToString(datas[8]));
        m.setImage(c.ToString(datas[9]));
        m.setLodgingId(c.ToString(datas[10]));
        ml.add(m);
        messageAdapter.notifyDataSetChanged();
        if(c.ToString(datas[7]).equals(c.ToString(datas[10]))){
            String command = c.ToHex("004821");
            String reserve = c.ToHex("000000000000000000000000");
            String senderClientId = c.ToHex(clientId);
            String receiverClientId = c.ToHex("serverLSSserver");
            String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
            String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + lodgingId;
            Publish(payload);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
