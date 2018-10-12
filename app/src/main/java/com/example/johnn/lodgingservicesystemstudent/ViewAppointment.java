package com.example.johnn.lodgingservicesystemstudent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import service.Converter;

public class ViewAppointment extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String clientId = "";
    String receiverClientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointment);

        clientId = "16104807";
        receiverClientId = "serverLSSserver";
    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                Toast.makeText(ViewAppointment.this, "onSuccess", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Toast.makeText(ViewAppointment.this, "onFailure", Toast.LENGTH_LONG).show();
            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Toast.makeText(ViewAppointment.this, "connection lost", Toast.LENGTH_LONG).show();
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {

                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if (receiverClientId.equals(clientId)) {

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

    private void GetData(){
        String payload = c.convertToHex(new String[]{"004831", "000000000000000000000000", clientId, receiverClientId});
        Publish(payload);
    }
}
