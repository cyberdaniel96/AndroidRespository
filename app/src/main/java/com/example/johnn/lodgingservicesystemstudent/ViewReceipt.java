package com.example.johnn.lodgingservicesystemstudent;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

    Button btnCapture, btnReset;

    RecyclerView recyclerView;
    ExpenseAdapter adapter;

    ArrayList<Expense> expenseArrayList = new ArrayList<>();

    String leaseID = "";
    String rentalID = "";

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
        btnReset = (Button)findViewById(R.id.btnReset);
        btnCapture.setOnClickListener(capture);
        btnReset.setOnClickListener(reset);
        btnReset.setVisibility(View.GONE);

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
          if(imgReceipt.getDrawable().getConstantState()
                  .equals(ContextCompat.getDrawable(ViewReceipt.this, R.drawable.example_receipt).getConstantState())){
              imgReceipt.setImageDrawable(null);
              imgReceipt.setImageDrawable(ContextCompat.getDrawable(ViewReceipt.this, R.drawable.example_receipt));
              btnReset.setVisibility(View.GONE);
              btnCapture.setText("CAPTURE");
          }
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

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST = 1888;
    private View.OnClickListener capture = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnReset.setVisibility(View.VISIBLE);
            String text = btnCapture.getText().toString().toUpperCase();
            if(text.equals("CAPTURE")){
                capture();
                btnCapture.setText("SUBMIT");
            }
            if(text.equals("SUBMIT")){
                btnReset.setVisibility(View.GONE);
                Toast.makeText(ViewReceipt.this, "submmited", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private View.OnClickListener reset = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imgReceipt.setImageDrawable(null);
            imgReceipt.setImageDrawable(ContextCompat.getDrawable(ViewReceipt.this, R.drawable.example_receipt));
            btnReset.setVisibility(View.GONE);
            btnCapture.setText("CAPTURE");
        }
    };

    public void capture(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(ViewReceipt.this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new
                        Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }

    }
    String mCurrentPhotoPath = "";

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            // Get the dimensions of the View
            int targetW = imgReceipt.getWidth();
            int targetH = imgReceipt.getHeight();

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            imgReceipt.setImageDrawable(d);
        }
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }
}
