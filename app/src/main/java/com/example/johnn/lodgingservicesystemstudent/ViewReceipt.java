package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import java.io.File;
import java.util.ArrayList;

import domain.Expense;
import domain.Receipt;
import service.Converter;
import service.ImageUtility;

public class ViewReceipt extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "1610480"+9;
    String receiverClientId = "serverLSSserver";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();
    Intent intent;
    ProgressDialog pb;

    TextView txtReceiptID, txtAmount, txtStatus, txtDate, txtTime;
    ImageView imgReceipt;
    Receipt receipt = new Receipt();

    Button btnCapture;

    RecyclerView recyclerView;
    ExpenseAdapter adapter;

    ArrayList<Expense> expenseArrayList = new ArrayList<>();

    String leaseID = "";
    String rentalID = "";

    int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_receipt);
        intent = getIntent();

        txtReceiptID = (TextView)findViewById(R.id.txtReceiptID);
        txtAmount = (TextView)findViewById(R.id.txtAmount);
        txtStatus = (TextView)findViewById(R.id.txtStatus);
        txtDate = (TextView)findViewById(R.id.txtDate);
        txtTime = (TextView)findViewById(R.id.txtTime);
        imgReceipt = (ImageView)findViewById(R.id.imgReceipt);
        btnCapture = (Button)findViewById(R.id.btnCapture);

        btnCapture.setOnClickListener(capture);

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        pb.show();

        recyclerView = (RecyclerView) findViewById(R.id.rvExpense);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new ExpenseAdapter(this, expenseArrayList);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void Connect() throws Exception {

        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                leaseID = intent.getStringExtra("LeaseID");
                rentalID = intent.getStringExtra("RentalID");
                Retrieve("RECEIPT",  leaseID+"|"+rentalID);
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
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = c.convertToString(datas[0]);
                String command = head[0];
                String reserve = head[1];
                String senderID = head[2];
                String receiverID = head[3];
                String mycommand = head[5];
                if(receiverID.equals(clientId)){
                    if(command.equals("004853")){
                        if(mycommand.equals("RECEIPT")){
                            SetReceiptData(mqttMessage.toString());
                        }
                        if(mycommand.equals("EXPENDSES")){
                            SetExpense(mqttMessage.toString());
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

    public void Retrieve(String mycommand, String id){
        String command = "004853";
        String reserve = "000000000000000000000000";
        String senderClientID = clientId;


        String head = c.convertToHex(new String[]{command, reserve, senderClientID, receiverClientId, ""});
        String body = c.convertToHex(new String[]{id, mycommand});

        Publish(head + "$" + body);
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

    public void Publish(String payload) {
        try {
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SetReceiptData(String message){
        String[] data = message.split("\\$");
        String[] body = c.convertToString(data[1]);

        receipt.setReceiptID(body[0]);
        receipt.setAmount(Double.parseDouble(body[1]));
        receipt.setImage(body[2]);
        receipt.setPayStatus(body[3]);
        receipt.setDateTime(body[4]);

        txtReceiptID.setText(receipt.getReceiptID());
        txtAmount.setText(receipt.getAmount()+"");
        txtStatus.setText(receipt.getPayStatus());
        String[] tempDate = receipt.getDateTime().split(" ");
        String date = tempDate[0];
        String time = tempDate[1].substring(0, 5);
        txtDate.setText(date);
        txtTime.setText(time);

        Retrieve("EXPENDSES", rentalID);
    }

    public void SetExpense(String message) {
        expenseArrayList.clear();
        String[] data = message.split("\\$");
        String[] head = c.convertToString(data[0]);


        int size = Integer.parseInt(head[4]);

        for (int index = 1; index <= size; index++){
            String[] body = c.convertToString(data[index]);
            Expense expense = new Expense();
            expense.setCategory(body[0]);
            expense.setAmount(Double.parseDouble(body[1]));
            expenseArrayList.add(expense);
        }
        adapter.notifyDataSetChanged();
        pb.dismiss();
    }

    private Uri outPutfileUri;
    Bitmap bitmap = null;


    private View.OnClickListener capture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent pictureIntent = new Intent(
                    MediaStore.ACTION_IMAGE_CAPTURE
            );
            if(pictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(pictureIntent,
                        REQUEST_IMAGE_CAPTURE);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");

                imgReceipt.setImageBitmap(ImageUtility.getPhoto(ImageUtility.getByte(imageBitmap)));
            }
        }
    }

}
