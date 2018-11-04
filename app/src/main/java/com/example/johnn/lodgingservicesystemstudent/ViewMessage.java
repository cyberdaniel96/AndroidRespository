package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

import service.Converter;

public class ViewMessage extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    ProgressDialog pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_message);

        final Button update = (Button) findViewById(R.id.updateBtn);
        Button delete = (Button) findViewById(R.id.deleteBtn);
        final EditText content = (EditText) findViewById(R.id.messageEt);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb = new ProgressDialog(ViewMessage.this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        clientId = getIntent().getStringExtra("UserID")+"8";

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(update.getText().toString().equals("Edit")){
                    content.setEnabled(true);
                    update.setText("Update");
                } else if(update.getText().toString().equals("Update")){
                    int error = 0;
                    if (content.getText().toString().length() < 1 || content.getText().toString().length() > 200) {
                        content.setError("Please type in your message.(Message cannot more than 200 characters.)");
                        error += 1;
                    }
                    if (error == 0) {
                        content.setEnabled(false);
                        String command = c.ToHex("004816");
                        String reserve = c.ToHex("000000000000000000000000");
                        String senderClientId = c.ToHex(clientId);
                        String receiverClientId = c.ToHex("serverLSSserver");
                        String messageId = c.ToHex(getIntent().getStringExtra("MessageID"));
                        String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/"
                                + messageId + "/" + c.ToHex(content.getText().toString());
                        Publish(payload);
                        update.setText("Edit");
                        pb.show();
                    }
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewMessage.this);
                builder.setTitle("Confirm");
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String command = c.ToHex("004815");
                        String reserve = c.ToHex("000000000000000000000000");
                        String senderClientId = c.ToHex(clientId);
                        String receiverClientId = c.ToHex("serverLSSserver");
                        String messageId = c.ToHex(getIntent().getStringExtra("MessageID"));
                        String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/"
                                + messageId;
                        Publish(payload);
                        pb.show();
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();;
                    }
                });
                AlertDialog alert = builder.create();
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
                String command = c.ToHex("004814");
                String reserve = c.ToHex("000000000000000000000000");
                String senderClientId = c.ToHex(clientId);
                String receiverClientId = c.ToHex("serverLSSserver");
                String messageId = c.ToHex(getIntent().getStringExtra("MessageID"));
                String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/"
                        + messageId;
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
                    if (command.equals("004814")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            SetData(mqttMessage.toString());
                        }  else if (state.equals("1")) {
                            Toast.makeText(ViewMessage.this, "Failed to retrieve", Toast.LENGTH_LONG).show();
                            pb.dismiss();
                        }
                    } else if (command.equals("004815")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            Toast.makeText(ViewMessage.this, "Successfully deleted", Toast.LENGTH_LONG).show();
                            ViewMessage.this.finish();
                        }  else if (state.equals("1")) {
                            Toast.makeText(ViewMessage.this, "Failed to delete", Toast.LENGTH_LONG).show();
                            pb.dismiss();
                        }
                    } else if (command.equals("004816")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            Toast.makeText(ViewMessage.this, "Successfully update", Toast.LENGTH_LONG).show();
                            pb.dismiss();
                        }  else if (state.equals("1")) {
                            Toast.makeText(ViewMessage.this, "Failed to update", Toast.LENGTH_LONG).show();
                            pb.dismiss();
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
        EditText content = (EditText) findViewById(R.id.messageEt);
        String[] datas = message.split("/");
        content.setText(c.ToString(datas[5]));
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
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
