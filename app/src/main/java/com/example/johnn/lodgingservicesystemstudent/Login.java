package com.example.johnn.lodgingservicesystemstudent;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.HashMap;

import service.Converter;
import service.SessionManager;

public class Login extends AppCompatActivity {

    SessionManager session;
    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = MqttClient.generateClientId();
    MemoryPersistence persistence = new MemoryPersistence();
    ProgressDialog pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        final CheckBox showPass = (CheckBox) findViewById(R.id.showPassChk);
        final EditText password = (EditText) findViewById(R.id.passwordEt);

        showPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (showPass.isChecked()) {
                    password.setTransformationMethod(null);
                } else {
                    password.setTransformationMethod(new PasswordTransformationMethod());
                }
            }
        });

        session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            HashMap<String, String> user = session.getUserDetails();
            String UserID = user.get(SessionManager.KEY_ID);
            String Email = user.get(SessionManager.KEY_EMAIL);
            session.createLoginSession(UserID, Email);
            Intent intent = new Intent(this, Home.class);
            intent.putExtra("UserID", UserID);
            intent.putExtra("Email", Email);
            startActivity(intent);
            this.finish();
        }

    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this.getApplicationContext(), broker, clientId, persistence);
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

                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if (receiverClientId.equals(clientId)) {
                    if (command.equals("004803")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            String userId = c.ToString(datas[5]);
                            String email = c.ToString(datas[6]);
                            session.createLoginSession(userId, email);
                            Intent intent = new Intent(Login.this, Home.class);
                            intent.putExtra("UserID", userId);
                            intent.putExtra("Email", email);
                            startActivity(intent);
                            Login.this.finish();
                            Toast.makeText(Login.this, "Welcome " + userId, Toast.LENGTH_LONG).show();

                            SharedPreferences.Editor editor = getSharedPreferences("LoggedInUser", MODE_PRIVATE).edit();
                            editor.putString("UserID", userId);
                            editor.putString("Email", email);
                            editor.apply();
                            Toast.makeText(getBaseContext(),c.ToString(datas[7]),Toast.LENGTH_LONG).show();
                            Intent in = new Intent(Login.this, PrivateChatList.class);
                            startActivity(in);
                        } else if (state.equals("1")){
                            pb.dismiss();
                            Toast.makeText(Login.this, "Wrong User ID or password", Toast.LENGTH_LONG).show();
                        }else if (state.equals("2")){
                            pb.dismiss();
                            Toast.makeText(Login.this, "Wrong User ID or password", Toast.LENGTH_LONG).show();
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

    public void Login(View view) {
        Converter c = new Converter();
        EditText userId = (EditText) findViewById(R.id.userIdEt);
        EditText password = (EditText) findViewById(R.id.passwordEt);
        String command = c.ToHex("004803");
        String reserve = c.ToHex("000000000000000000000000");
        String senderClientId = c.ToHex(clientId);
        String receiverClientId = c.ToHex("serverLSSserver");
        int error = 0;
        if (userId.getText().toString().length() == 0) {
            userId.setError("Please enter your User ID.");
            error += 1;
        }
        if (password.getText().toString().length() == 0) {
            password.setError("Please enter your password.");
            error += 1;
        }
        if (error == 0) {
            String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/"
                    + c.ToHex(userId.getText().toString()) + "/" + c.ToHex(password.getText().toString());
            Publish(payload);
            pb.show();
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

    @Override
    protected void onPause() {
        super.onPause();
        try {
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
