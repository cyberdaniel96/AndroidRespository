package com.example.johnn.lodgingservicesystemstudent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import org.eclipse.paho.client.mqttv3.MqttCallback;

public class Testing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing);
        onNewIntent(getIntent());
       // String data = getIntent().getExtras().getString("Testing");
    }

    public void click(View v) throws Exception {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if(extras != null){
            if(extras.containsKey("MyData"))
            {
                // extract the extra-data in the Notification
                String msg = extras.getString("MyData");
                Log.e("DATA", msg);

            }
        }
    }
}
