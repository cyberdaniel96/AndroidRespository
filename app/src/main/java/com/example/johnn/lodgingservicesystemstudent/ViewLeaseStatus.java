package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.List;

import domain.Lease;
import domain.Lodging;
import domain.Tenant;
import service.Converter;

public class ViewLeaseStatus extends AppCompatActivity {
    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    String receiverClientId = "serverLSSserver";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();

    List<Lodging> lodgingList = new ArrayList<>();
    List<Lease> leaseList = new ArrayList<>();
    RecyclerView recyclerView;
    ViewLeaseStatusAdapter adapter;
    ProgressDialog pb;

    int myposition = 0;
    View view = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_lease_status);
        clientId = "16104809";

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");


        recyclerView = (RecyclerView) findViewById(R.id.viewLeaseStatus);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ViewLeaseStatusAdapter(this, lodgingList);
        recyclerView.setAdapter(adapter);

        adapter.setOnClickListener(new ViewLeaseStatusAdapter.OnClickListener() {
            @Override
            public void onClick(View v, int position) {

                ViewLeaseStatus.this.myposition = position;
                ViewLeaseStatus.this.view = v;

                Retrieve("GETTENANT", leaseList.get(myposition).getLeaseID());
            }
        });

    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                Retrieve("GETLOD","");
                Log.e("view lease", "onSuccess");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e("view lease", "onFailure");
            }
        });
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Log.e("view lease", "connectionLost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {
                System.out.println("Message Arrived");
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = c.convertToString(datas[0]);
                String command = head[0];
                String reserve = head[1];
                String senderID = head[2];
                String receiverID = head[3];

                if(receiverID.equals(clientId)){
                    if(command.equals("004849")){

                        SetData(mqttMessage.toString());
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
    protected void onResume() {
        super.onResume();
        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void Retrieve(String mycommand, String id){
        String command = "004849";
        String reserve = "000000000000000000000000";
        String senderClientID = clientId;
        String tenantID = "1610480";

        String payload = "";
        if(mycommand.equals("GETLOD")){
            payload = c.convertToHex(new String[]{command, reserve, senderClientID, receiverClientId, ""}) + "$" + c.ToHex(tenantID) +"/"+ c.ToHex(mycommand);
        }
        if(mycommand.equals("GETTENANT")){
            payload =  c.convertToHex(new String[]{command, reserve, senderClientID, receiverClientId, ""}) + "$" + c.ToHex(tenantID) +"/"+ c.ToHex(mycommand)+"/"+c.ToHex(id);
        }
        Log.e("retrieve", mycommand);

        Publish(payload);
    }

    public void SetData(String message){

        pb.show();
        String[] splitDollar = message.split("\\$");
        String[] serverData = c.convertToString(splitDollar[0]);

        String[] data = splitDollar[1].split("@");

        //section get number of data
        String[] firstdata = c.convertToString(data[0]);
        String mycommand = firstdata[0];
        if(mycommand.equals("GETLOD")){
            lodgingList.clear();
            leaseList.clear();
            int numOfData = Integer.parseInt(firstdata[9]);
            for(int index = 0; index < numOfData; index++){
                String[] tempData = c.convertToString(data[index]);

                Lease lease = new Lease();
                lease.setLeaseID(tempData[1]);
                lease.setDueDay(tempData[2]);
                lease.setIssueDate(tempData[3]);
                lease.setStatus(tempData[4]);
                lease.setLodgingID(tempData[5]);
                leaseList.add(lease);

                Lodging lodging = new Lodging();
                lodging.setImage(tempData[6]);
                lodging.setTitle(tempData[7]);
                lodging.setAddress(tempData[8].replace("$", ","));
                lodgingList.add(lodging);
            }

            adapter.notifyDataSetChanged();
            pb.dismiss();
        }

        if(mycommand.equals("GETTENANT")){
            String[] myData = c.convertToString(splitDollar[1]);
            pb.dismiss();
            Tenant t = new Tenant();

            t.setLeaseID(myData[1]);
            t.setRoomType(myData[2]);
            t.setRole(myData[3]);
            t.setLeaseStart(myData[4]);
            t.setLeaseEnd(myData[5]);
            t.setRent(Double.parseDouble(myData[6]));
            t.setDeposit(Double.parseDouble(myData[7]));
            t.setStatus(myData[8]);
            t.setUserID(myData[11]);
            t.setLeaseID(myData[12]);

            PopupLease pop = new PopupLease(getApplicationContext(), leaseList.get(this.myposition), t);
            pop.showWindows(this.view);
        }

    }

}
