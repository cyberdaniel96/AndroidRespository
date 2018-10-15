package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import domain.Appointment;
import service.Converter;

public class CancelAppointment {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String receiverClientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    private String clientID;
    private Context context;
    private Appointment app;

    public CancelAppointment(String clientID, Context context, Appointment appointment) throws Exception {
        this.clientID = clientID;
        this.context = context;
        this.app = appointment;
        Connect();

    }



    public void Connect() throws Exception {

        client = new MqttAndroidClient(context, broker, clientID, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                Cancel();
                System.out.println("Canceling");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                System.out.println("connectionLost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {

                String[] datas = mqttMessage.toString().split("\\$");
                String data[] = datas[0].split("/");
                String command = c.ToString(data[0]);
                String receiverClientId = c.ToString(data[3]);
                if (receiverClientId.equals(clientID)) {

                }

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
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

    public void Subscribe() {
        try {
            client.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void Cancel() {
        String payload = c.convertToHex(new String[]{"004829", "000000000000000000000000", clientID + 7, "serverLSSserver", app.getAppointmentID()});
        Publish(payload);
    }
}
