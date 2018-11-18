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
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.security.PublicKey;
import java.util.HashMap;

import domain.Appointment;
import service.Converter;
import service.SessionManager;

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
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        init();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        try{
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

        }

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

            String message = mqttMessage.toString();
            String splitDollar[] = message.split("\\$");
            String serverData[] = c.convertToString(splitDollar[0]);
            String notiData[] = c.convertToString(splitDollar[1]);

            String command = serverData[0];
            if(command.equals("004841")){
                SessionManager session = new SessionManager(getApplicationContext());
                HashMap<String, String> user = session.getUserDetails();
                String UserID = user.get(SessionManager.KEY_ID);

                if(notiData[3].equals(UserID)){
                    checkNotifierClass(mqttMessage.toString());
                }
            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    };

    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken iMqttToken) {

            subscribe();

        }

        @Override
        public void onFailure(IMqttToken iMqttToken, Throwable throwable) {

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

            return true;
        }else{

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
                case "ACCEPTED":
                    AllNotification.CancelAppointment(getApplicationContext(), title, content);
                    break;
                case "REJECTED":
                    AllNotification.CancelAppointment(getApplicationContext(), title, content);
                    break;
                default:
                    break;
            }
        }

        if(clsCommand.equals("LEASE")){
            String title = notiData[0];
            String content = notiData[1];
            String[] resourceData = data[2].split("@");

            Log.e("output",notiType);
            switch (notiType) {
                case "RECEIVED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;
                case "TERMINATED":
                    AllNotification.LeaseReceived(getApplicationContext(),title, content);
                    break;
                case "EXPIRED":
                    AllNotification.LeaseExpired(getApplicationContext(),title, content);
                    break;
                default:
                    break;
            }
        }
        Log.e("first", notiType);
        Log.e("sec", clsCommand);
        if(clsCommand.equals("RENTAL")){
            String title = notiData[0];
            String content = notiData[1];
            String[] resourceData = data[2].split("@");
            Log.e("first", notiType);
            Log.e("sec", clsCommand);
            switch (notiType) {
                case "UPLOADED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;
                case "EXPIRED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;
                case "ACCEPTED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;
                case "REJECTED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;
                case "EDITED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;

                default:
                    break;
            }
        }
        Toast.makeText(getApplicationContext(), clsCommand, Toast.LENGTH_LONG).show();
        if(clsCommand.equals("PRIVATECHAT")){
            String title = notiData[0];
            String content = notiData[1];
            String[] resourceData = data[2].split("@");

            switch (notiType) {
                case "RECEIVED":
                    AllNotification.LeaseReceived(getApplicationContext(), title, content);
                    break;
                default:
                    break;
            }
        }

    }
}
