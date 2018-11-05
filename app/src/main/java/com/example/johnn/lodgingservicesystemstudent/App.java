package com.example.johnn.lodgingservicesystemstudent;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;


public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        registerIntentFilter();
        createNotificationChannel();
    }

    private void registerIntentFilter() {
        BroadcastReceiver br = new Receiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
        getBaseContext().registerReceiver(br, filter);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    Notify.CHANNEL_1_ID,
                    "Importand Message",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);

            //create further channel if you want

//            NotificationChannel channel2 = new NotificationChannel(
//                    Notify.CHANNEL_2_ID,
//                    "Channel 2",
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            channel1.setDescription("This is channel 2");
//
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            manager.createNotificationChannel(channel2);

        }

    }
}

