package com.example.johnn.lodgingservicesystemstudent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import domain.Appointment;
import service.Converter;

public class Services extends Service {

    private static MqttAndroidClient client;
    private MqttConnectOptions connectOpt;

    //hosted
    private String broker = Home.broker;
    private static String topic = "MY/TARUC/LSS/000000001/PUB";
    private String clientID = "studentNotification";//MQTT not allow same clientID to be used for multiple deveices
    private String receiverClientID = "serverLSSserver";
    private static int qos = 1;
    MemoryPersistence persistence = new MemoryPersistence();
    private Converter c = new Converter();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.e("Service," ,"onCreated");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("Service," ,"onStartCommand");
        init();
        Log.e("Service," ,"onStartCommandFinish");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try{
            Log.e("Service," ,"onDestroy Called");
            client.disconnect();
        }catch (MqttException e){
            e.printStackTrace();
        }
        super.onDestroy();
    }



    private void init() {
        client = new MqttAndroidClient(getApplicationContext(), broker, clientID, persistence);
        client.setCallback(mqttCallback);
        connectOpt = new MqttConnectOptions();
        connectOpt.setCleanSession(true);
        connectOpt.setConnectionTimeout(10);
        connectOpt.setKeepAliveInterval(20);
        doClientConnection();
    }



    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable throwable) {
            Log.e("Connection", "connectionLost");
        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            Log.e("Connection", "messageArrived");
            String message = mqttMessage.toString();
            String splitDollar[] = message.split("\\$");
            String serverData[] = c.convertToString(splitDollar[0]);
            String notiData[] = c.convertToString(splitDollar[1]);

            String command = serverData[0];
            Log.e("command", command);
            if(command.equals("004841")){
                Log.e("notiData", notiData[3]);
                if(notiData[3].equals("johnny96")){
                    checkNotifierClass(mqttMessage.toString());
                }
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            Log.e("Connection", "deliveryComplete");
        }
    };

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken iMqttToken) {
            Log.e("Connection: ", "onSuccess");
            subscribe();

        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
            Log.e("Connection: ", "onFailure");
        }
    };

    private void doClientConnection() {
        if(!client.isConnected() && isConnectIsNormal()){
            try{
                client.connect(connectOpt, null, iMqttActionListener);
            }catch (MqttException e){
                e.printStackTrace();
            }
        }
    }

    private boolean isConnectIsNormal(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if(info != null && info.isAvailable()){
            String name = info.getTypeName();
            Log.e("MQTT Connection", "Name: " + name);
            return true;
        }else{
            Log.e("MQTT Connection", "Name: Lost");
            return false;
        }
    }

    public static void publish(String payload){
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void subscribe(){
        try {
            client.subscribe(topic, qos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkNotifierClass(String message){
        String data[] = message.split("\\$");
        String[] serverData = c.convertToString(data[0]);
        String[] notiData = c.convertToString(data[1]);

        String[] notifyCommand = notiData[2].split(" ");
        String clsCommand = notifyCommand[0];
        String notiType = notifyCommand[1];

        if(clsCommand.equals("APPOINTMENT")){

            String title = notiData[0];
            String content = notiData[1];
            String[] resourceData = data[2].split("@");

            switch (notiType) {
                case "CANCEL":
                    AppointmentNotifier.CancelAppointment(getApplicationContext(), title, content);
                    break;
                default:
                    break;
            }
        }
    }
}
