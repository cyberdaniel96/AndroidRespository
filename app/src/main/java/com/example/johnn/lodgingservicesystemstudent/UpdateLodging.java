package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

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
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import domain.Lodging;
import service.Converter;

public class UpdateLodging extends AppCompatActivity {

    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();
    ProgressDialog pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_lodging);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");
        pb.show();

        Spinner spinner = (Spinner) findViewById(R.id.lodgingTypeS);
        ArrayAdapter<CharSequence> lodgingTypeAdapter = ArrayAdapter.createFromResource(this, R.array.lodgingType, android.R.layout.simple_spinner_dropdown_item);
        lodgingTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setEnabled(false);
        spinner.setAdapter(lodgingTypeAdapter);
        final Button edit = (Button) findViewById(R.id.editBtn);

        clientId = getIntent().getStringExtra("UserID") + "6";

        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (edit.getText().toString().equals("Edit")) {
                        ChangeUIState("Edit");
                    } else {
                        Lodging l = getData();
                        if (l != null) {
                            String command = c.ToHex("004811");
                            String reserve = c.ToHex("000000000000000000000000");
                            String senderClientId = c.ToHex(clientId);
                            String receiverClientId = c.ToHex("serverLSSserver");
                            String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
                            String title = c.ToHex(l.getTitle());
                            String address = c.ToHex(l.getAddress());
                            String price = c.ToHex(l.getPrice() + "");
                            String facility = c.ToHex(l.getFacility());
                            String lodgignType = c.ToHex(l.getLodgingType());
                            String description = c.ToHex(l.getDescription());
                            String expireDate = c.ToHex(l.getExpireDate());
                            String image = c.ToHex(l.getImage());
                            String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + lodgingId + "/"
                                    + title + "/" + address + "/" + price + "/" + facility + "/" + lodgignType + "/" + description + "/"
                                    + expireDate + "/" + image;
                            Publish(payload);
                            pb.show();
                            ChangeUIState("Update");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
                String command = c.ToHex("004809");
                String reserve = c.ToHex("000000000000000000000000");
                String senderClientId = c.ToHex(clientId);
                String receiverClientId = c.ToHex("serverLSSserver");
                String lodgingId = c.ToHex(getIntent().getStringExtra("LodgingID"));
                String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/"
                        + lodgingId;
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
            public void messageArrived(String s, MqttMessage mqttMessage) {
                String[] datas = mqttMessage.toString().split("/");
                String command = c.ToString(datas[0]);
                String receiverClientId = c.ToString(datas[3]);
                if (receiverClientId.equals(clientId)) {
                    if (command.equals("004809")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            if (c.ToString(datas[15]).equals(getIntent().getStringExtra("LodgingID")))
                                SetData(mqttMessage.toString());
                            pb.dismiss();
                        } else if (state.equals("1")) {
                            pb.dismiss();
                            Toast.makeText(UpdateLodging.this, "Retrieve fail(Server error)", Toast.LENGTH_LONG).show();
                        }
                    } else if (command.equals("004811")) {
                        String state = c.ToString(datas[4]);
                        if (state.equals("0")) {
                            pb.dismiss();
                            Toast.makeText(UpdateLodging.this, "Update successfully", Toast.LENGTH_LONG).show();
                        } else if (state.equals("1")) {
                            pb.dismiss();
                            Toast.makeText(UpdateLodging.this, "Update failed(Server error)", Toast.LENGTH_LONG).show();
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

    public void SetData(String message) {

        String[] datas = message.split("/");
        EditText title = (EditText) findViewById(R.id.titleEt);
        EditText address = (EditText) findViewById(R.id.addressEt);
        EditText postcode = (EditText) findViewById(R.id.postcodeEt);
        EditText city = (EditText) findViewById(R.id.cityEt);
        EditText state = (EditText) findViewById(R.id.stateEt);
        EditText country = (EditText) findViewById(R.id.countryEt);
        EditText price = (EditText) findViewById(R.id.priceEt);
        CheckBox washingMachine = (CheckBox) findViewById(R.id.washingMachineCb);
        CheckBox waterHeater = (CheckBox) findViewById(R.id.waterHeaterCb);
        CheckBox refrigerator = (CheckBox) findViewById(R.id.refrigeratorCb);
        Spinner lodgingType = (Spinner) findViewById(R.id.lodgingTypeS);
        EditText description = (EditText) findViewById(R.id.descriptionEt);
        EditText expiryDate = (EditText) findViewById(R.id.expiryDateEt);
        ImageView lodgingPic = (ImageView) findViewById(R.id.lodgingPicIv);

        String[] addressS = c.ToString(datas[6]).split("\\$");
        String sAddress = addressS[0];
        String[] addressS1 = addressS[1].split(" ");
        String sPostcode = addressS1[0];
        String sCity = addressS1[1];
        String sState = addressS[2];
        String sCountry = addressS[3];


        if (!c.ToString(datas[8]).equals("000")) {
            for (int i = 0; i < c.ToString(datas[8]).length(); i++) {
                if (c.ToString(datas[8]).charAt(i) == '1') {
                    if (i == 0) {
                        washingMachine.setChecked(true);
                    } else if (i == 1) {
                        waterHeater.setChecked(true);
                    } else if (i == 2) {
                        refrigerator.setChecked(true);
                    }
                }
            }
        }

        title.setText(c.ToString(datas[5]));
        address.setText(sAddress);
        postcode.setText(sPostcode);
        city.setText(sCity);
        state.setText(sState);
        country.setText(sCountry);
        price.setText(String.format("%.2f", Double.parseDouble(c.ToString(datas[7]))));
        if (c.ToString(datas[9]).equals("Landed")) {
            lodgingType.setSelection(1);
        } else if (c.ToString(datas[9]).equals("Condominium")) {
            lodgingType.setSelection(2);
        }
        description.setText(c.ToString(datas[10]));
        expiryDate.setText("Expire Date: " + c.ToString(datas[11]));
        Glide.with(this)
                .load(c.ToString(datas[16])).asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(lodgingPic);

    }

    public void ChangeUIState(String s) {
        EditText title = (EditText) findViewById(R.id.titleEt);
        EditText address = (EditText) findViewById(R.id.addressEt);
        EditText postcode = (EditText) findViewById(R.id.postcodeEt);
        EditText city = (EditText) findViewById(R.id.cityEt);
        EditText state = (EditText) findViewById(R.id.stateEt);
        EditText country = (EditText) findViewById(R.id.countryEt);
        EditText price = (EditText) findViewById(R.id.priceEt);
        CheckBox washingMachine = (CheckBox) findViewById(R.id.washingMachineCb);
        CheckBox waterHeater = (CheckBox) findViewById(R.id.waterHeaterCb);
        CheckBox refrigerator = (CheckBox) findViewById(R.id.refrigeratorCb);
        Spinner lodgingType = (Spinner) findViewById(R.id.lodgingTypeS);
        EditText description = (EditText) findViewById(R.id.descriptionEt);
        Button edit = (Button) findViewById(R.id.editBtn);
        Button choose = (Button) findViewById(R.id.chooseBtn);
        EditText expiryDate = (EditText) findViewById(R.id.expiryDateEt);

        if (s.equals("Edit")) {
            title.setEnabled(true);
            address.setEnabled(true);
            postcode.setEnabled(true);
            city.setEnabled(true);
            state.setEnabled(true);
            country.setEnabled(true);
            price.setEnabled(true);
            washingMachine.setEnabled(true);
            waterHeater.setEnabled(true);
            refrigerator.setEnabled(true);
            lodgingType.setEnabled(true);
            description.setEnabled(true);
            choose.setVisibility(View.VISIBLE);

            Date current = new Date();
            Calendar cc = Calendar.getInstance();
            cc.setTime(current);
            cc.add(Calendar.DATE, 7);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            String expireDate = sdf.format(cc.getTime());
            expiryDate.setText("Expire Date: " + expireDate);

            edit.setText("Update");
        } else {
            title.setEnabled(false);
            address.setEnabled(false);
            postcode.setEnabled(false);
            city.setEnabled(false);
            state.setEnabled(false);
            country.setEnabled(false);
            price.setEnabled(false);
            washingMachine.setEnabled(false);
            waterHeater.setEnabled(false);
            refrigerator.setEnabled(false);
            lodgingType.setEnabled(false);
            description.setEnabled(false);
            choose.setVisibility(View.GONE);
            edit.setText("Edit");
        }
    }

    public Lodging getData() {
        EditText title = (EditText) findViewById(R.id.titleEt);
        EditText address = (EditText) findViewById(R.id.addressEt);
        EditText postcode = (EditText) findViewById(R.id.postcodeEt);
        EditText city = (EditText) findViewById(R.id.cityEt);
        EditText state = (EditText) findViewById(R.id.stateEt);
        EditText country = (EditText) findViewById(R.id.countryEt);
        EditText price = (EditText) findViewById(R.id.priceEt);
        CheckBox washingMachine = (CheckBox) findViewById(R.id.washingMachineCb);
        CheckBox waterHeater = (CheckBox) findViewById(R.id.waterHeaterCb);
        CheckBox refrigerator = (CheckBox) findViewById(R.id.refrigeratorCb);
        Spinner lodgingType = (Spinner) findViewById(R.id.lodgingTypeS);
        EditText description = (EditText) findViewById(R.id.descriptionEt);
        EditText expiryDate = (EditText) findViewById(R.id.expiryDateEt);
        ImageView ivImg = (ImageView) findViewById(R.id.lodgingPicIv);
        BitmapDrawable drawable = (BitmapDrawable) ivImg.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String image = Base64.encodeToString(byteArray, Base64.NO_WRAP);
        int error = 0;
        String facility = "";

        if (title.getText().toString().length() < 1 || title.getText().toString().length() > 50) {
            title.setError("Title cannot be blank or more than 50 character.");
            error += 1;
        }
        if (address.getText().toString().length() > 100) {
            address.setError("Address cannot more than 100 character.");
            error += 1;
        }
        if (!address.getText().toString().matches("^[a-zA-Z0-9][a-zA-Z0-9\\.\\s\\/\\,\\-]*$")) {
            address.setError("Address cannot be blank and only can contain alphanumeric characters, whitespace and ,./-");
            error += 1;
        }
        if (postcode.getText().toString().length() != 5) {
            postcode.setError("Postcode format only contain 5 numbers.");
            error += 1;
        }
        try {
            Integer.parseInt(postcode.getText().toString());
        } catch (Exception e) {
            postcode.setError("Postcode format only contain 5 numbers.");
            error += 1;
        }
        if (city.getText().toString().length() > 20) {
            city.setError("City cannot more than 20 character.");
            error += 1;
        }
        if (!city.getText().toString().matches("^[a-zA-Z][a-zA-Z\\s]*$")) {
            city.setError("City cannot be blank and only can contain alphbet characters and whitespace");
            error += 1;
        }
        if (state.getText().toString().length() > 20) {
            state.setError("State cannot be more than 20 character.");
            error += 1;
        }
        if (!state.getText().toString().matches("^[a-zA-Z][a-zA-Z\\s]*$")) {
            state.setError("State cannot be blank and only can contain alphbet characters and whitespace");
            error += 1;
        }
        if (country.getText().toString().length() > 20) {
            country.setError("Country cannot more than 20 character.");
            error += 1;
        }
        if (!country.getText().toString().matches("^[a-zA-Z][a-zA-Z\\s]*$")) {
            country.setError("Country cannot be blank and only can contain alphbet characters and whitespace");
            error += 1;
        }
        try {
            Double.parseDouble(price.getText().toString());
        } catch (Exception e) {
            price.setError("Please enter a valid value.");
            error += 1;
        }
        if (washingMachine.isChecked()) {
            facility += "1";
        } else {
            facility += "0";
        }
        if (waterHeater.isChecked()) {
            facility += "1";
        } else {
            facility += "0";
        }
        if (refrigerator.isChecked()) {
            facility += "1";
        } else {
            facility += "0";
        }
        if (lodgingType.getSelectedItem().toString().equals("Choose your lodging type")) {
            Toast.makeText(this, "Please choose your lodging type.", Toast.LENGTH_LONG).show();
            error += 1;
        }
        if (description.getText().toString().length() < 1 || description.getText().toString().length() > 100) {
            description.setError("Write something but don't exceed 100 character.");
            error += 1;
        }

        String cAddress = address.getText().toString() + "$" + postcode.getText().toString() + " " + city.getText().toString() + "$"
                + state.getText().toString() + "$" + country.getText().toString();

        Lodging l = new Lodging();

        if (error == 0) {
            l.setTitle(title.getText().toString());
            l.setAddress(cAddress);
            l.setPrice(Double.parseDouble(price.getText().toString()));
            l.setFacility(facility);
            l.setLodgingType(lodgingType.getSelectedItem().toString());
            l.setDescription(description.getText().toString());
            l.setExpireDate(expiryDate.getText().toString().substring(13));
            l.setImage(image);
        } else {
            l = null;
        }
        return l;
    }


    public void ChoosePicture(View v) {
        final CharSequence[] items = {"Take Photo", "Choose from Library",
                "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Picture");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 0);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select File"), 1);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView lodgingPic = (ImageView) findViewById(R.id.lodgingPicIv);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            try {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                lodgingPic.setImageBitmap(thumbnail);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == 1) {
            Bitmap bm = null;
            if (data != null) {
                try {
                    bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                    lodgingPic.setImageBitmap(getResizedBitmap(bm,500,500));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
                matrix, false);

        return resizedBitmap;
    }

}
