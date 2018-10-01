package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import domain.Favourite;
import domain.Lodging;
import service.Converter;

public class FavouriteLodging extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    List<Lodging> ll = new ArrayList<>();
    ProgressDialog pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_lodging);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb = new ProgressDialog(FavouriteLodging.this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        clientId = getIntent().getStringExtra("UserID")+"9";

    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                String command = c.ToHex("004819");
                String reserve = c.ToHex("000000000000000000000000");
                String senderClientId = c.ToHex(clientId);
                String receiverClientId = c.ToHex("serverLSSserver");
                String userId = c.ToHex(getIntent().getStringExtra("UserID"));
                String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + userId;
                Publish(payload);
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
                    if (command.equals("004819")) {
                        String size = c.ToString(head[4]);
                        if (size.equals("0")) {
                            Toast.makeText(FavouriteLodging.this, "No recored found", Toast.LENGTH_LONG).show();
                            pb.dismiss();
                        } else {
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

    public void SetData(String message) throws Exception {
        ll.clear();

        String[] datas = message.split("\\$");
        String[] head = datas[0].split("/");
        int size = Integer.parseInt(c.ToString(head[4]));
        for (int i = 0; i < size; i++) {
            Lodging l = new Lodging();
            String[] body = datas[i + 1].split("/");
            l.setLodgingId(c.ToString(body[0]));
            l.setTitle(c.ToString(body[1]));
            l.setPrice(Double.parseDouble(c.ToString(body[2])));
            l.setExpireDate(c.ToString(body[3]));
            l.setLodgingType(c.ToString(body[4]));
            l.setImage(c.ToString(body[5]));
            ll.add(l);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lodgingRV);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        LodgingAdapter lodgingAdapter = new LodgingAdapter(ll);
        recyclerView.setAdapter(lodgingAdapter);
        pb.dismiss();
        lodgingAdapter.setOnItemClickListener(new LodgingAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(FavouriteLodging.this, ViewFavouriteDetails.class);
                intent.putExtra("UserID",getIntent().getStringExtra("UserID"));
                intent.putExtra("LodgingID",ll.get(position).getLodgingId());
                startActivity(intent);
            }
        });
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
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
