package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
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

import service.Converter;

public class Feedback extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();
    ProgressDialog pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView email = (TextView) findViewById(R.id.emailTv);

        email.setText("Email: "+getIntent().getStringExtra("Email"));

        clientId = getIntent().getStringExtra("UserID") + "10";

        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Spinner spinner = (Spinner) findViewById(R.id.feedbackTypeS);
        ArrayAdapter<CharSequence> feedbackTypeAdapter = ArrayAdapter.createFromResource(this, R.array.feedbackType, android.R.layout.simple_spinner_dropdown_item);
        feedbackTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(feedbackTypeAdapter);
    }

    public void Submit(View v){
        Spinner feedbackType = (Spinner) findViewById(R.id.feedbackTypeS);
        EditText feedback = (EditText) findViewById(R.id.feedbackEt);

        if (feedback.getText().toString().length() < 1 || feedback.getText().toString().length() > 200) {
            feedback.setError("Feedback cannot blank or more than 200 character.");
        } else {
            String command = c.ToHex("004822");
            String reserve = c.ToHex("000000000000000000000000");
            String senderClientId = c.ToHex(clientId);
            String receiverClientId = c.ToHex("server");

            String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" +
                    c.ToHex(getIntent().getStringExtra("Email")) + "/" + c.ToHex(feedbackType.getSelectedItem().toString()) + "/" +
                    c.ToHex(feedback.getText().toString());
            Publish(payload);
            pb = new ProgressDialog(Feedback.this);
            pb.setCanceledOnTouchOutside(false);
            pb.setMessage("Loading...");
            pb.show();
        }
    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
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

                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if (receiverClientId.equals(clientId)) {
                    if (command.equals("004822")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            pb.dismiss();
                            Toast.makeText(Feedback.this, "Feedback submit successfully", Toast.LENGTH_LONG).show();
                        } else if (state.equals("1")) {
                            pb.dismiss();
                            Toast.makeText(Feedback.this, "Feedback submit failed(Server error)", Toast.LENGTH_LONG).show();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
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
