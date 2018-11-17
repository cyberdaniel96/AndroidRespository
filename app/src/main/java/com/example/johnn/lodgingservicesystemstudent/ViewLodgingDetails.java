package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import domain.PrivateChat;
import service.Converter;

public class ViewLodgingDetails extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    ProgressDialog pb;

    private Button btnPrivateChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lodging_details);
        Button chat = (Button) findViewById(R.id.chatBtn);
        Button add = (Button) findViewById(R.id.favouriteBtn);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        clientId = getIntent().getStringExtra("UserID")+"6";

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewLodgingDetails.this, ChatRoom.class);
                intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
                intent.putExtra("LodgingID", getIntent().getStringExtra("LodgingID"));
                startActivity(intent);
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = c.ToHex("004818");
                String reserve = c.ToHex("000000000000000000000000");
                String senderClientId = c.ToHex(clientId);
                String receiverClientId = c.ToHex("serverLSSserver");
                String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
                String userId = c.ToHex(getIntent().getStringExtra("UserID"));
                String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + lodgingId + "/"
                        +userId;
                Publish(payload);
                pb.show();
            }
        });
    }

    public void navToPrivateChat(View view){
        Intent intent = new Intent(this, PrivateChatList.class);
        TextView textView = (TextView)findViewById(R.id.logingOwner);
        intent.putExtra("lodgingOwner",textView.getText().toString());
        startActivity(intent);
    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);


        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                String command = c.ToHex("004809");
                String reserve = c.ToHex("000000000000000000000000");
                String senderClientId = c.ToHex(clientId);
                String receiverClientId = c.ToHex("serverLSSserver");
                String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
                String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + lodgingId;
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
                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if (receiverClientId.equals(clientId)) {
                    if (command.equals("004809")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            if (c.ToString(datas[15]).equals(getIntent().getStringExtra("LodgingID")))
                                SetData(mqttMessage.toString());
                        } else if (state.equals("1")) {
                            pb.dismiss();
                            Toast.makeText(ViewLodgingDetails.this, "Retrieve fail(Server error)", Toast.LENGTH_LONG).show();
                        }
                    } else if (command.equals("004818")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            pb.dismiss();
                            Toast.makeText(ViewLodgingDetails.this, "Already in favourite list", Toast.LENGTH_LONG).show();
                        } else if (state.equals("1")) {
                            pb.dismiss();
                            Toast.makeText(ViewLodgingDetails.this, "Successfully added to favourite list", Toast.LENGTH_LONG).show();
                        } else if (state.equals("2")) {
                            pb.dismiss();
                            Toast.makeText(ViewLodgingDetails.this, "Failed to add(Server error)", Toast.LENGTH_LONG).show();
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

    public void btnMakeAppointment(View view){
        Intent intent = new Intent(this, CreateAppointment.class);
        intent.putExtra("clientID", getIntent().getStringExtra("UserID"));
        intent.putExtra("lodgingID",getIntent().getStringExtra("LodgingID"));
        TextView textView = (TextView)findViewById(R.id.logingOwner);
        intent.putExtra("ownerID", textView.getText().toString().replace("Owner ID: ", ""));
        startActivity(intent);
    }

    public void SetData(String message) {

        String[] datas = message.split("/");
        TextView title = (TextView) findViewById(R.id.titleTv);
        TextView address = (TextView) findViewById(R.id.addressTv);
        TextView facility = (TextView) findViewById(R.id.facilityTv);
        TextView price = (TextView) findViewById(R.id.priceTv);
        TextView lodgingType = (TextView) findViewById(R.id.lodgingTypeTv);
        TextView description = (TextView) findViewById(R.id.descriptionTv);
        TextView owner = (TextView) findViewById(R.id.ownerTv);
        TextView contactNo = (TextView) findViewById(R.id.contactNoTv);
        TextView email = (TextView) findViewById(R.id.emailTv);
        TextView expireDate = (TextView) findViewById(R.id.expireDateTv);
        TextView ownerId = (TextView)findViewById(R.id.logingOwner);
        ImageView lodgingPic = (ImageView) findViewById(R.id.lodgingPicIv);
        String sFacility = "";

        if (!c.ToString(datas[8]).equals("000")) {
            for (int i = 0; i < c.ToString(datas[8]).length(); i++) {
                if (c.ToString(datas[8]).charAt(i) == '1') {
                    if (i == 0) {
                        sFacility += "(Washing Machine)";
                    } else if (i == 1) {
                        sFacility += "(Water Heater)";
                    } else if (i == 2) {
                        sFacility += "(Refrigerator)";
                    }
                }
            }
        } else {
            sFacility += "No facility provided";
        }
        title.setText("Title: " + c.ToString(datas[5]));
        address.setText("Address: " + c.ToString(datas[6]).replace("$", ","));
        facility.setText("Facility: " + sFacility);
        price.setText("Price: RM" + String.format("%.2f", Double.parseDouble(c.ToString(datas[7]))));
        lodgingType.setText("Lodging Type: " + c.ToString(datas[9]));
        description.setText("Description: " + c.ToString(datas[10]));
        owner.setText("Owner: " + c.ToString(datas[12]));
        contactNo.setText("Contact No: " + c.ToString(datas[13]));
        email.setText("Email: " + c.ToString(datas[14]));
        expireDate.setText("Expire Date: " + c.ToString(datas[11]));
        ownerId.setText("Owner ID: " + c.ToString(datas[18]));
        Glide.with(this)
                .load(c.ToString(datas[16]))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(lodgingPic);
        pb.dismiss();
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
