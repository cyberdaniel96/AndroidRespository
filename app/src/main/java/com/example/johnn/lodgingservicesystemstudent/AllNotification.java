package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;


public class AllNotification{


    public static void LeaseReceived(Context context, String title, String message){
        Intent intent =  new Intent(context, ViewLeaseStatus.class);
        Notify notify = new Notify(context)
                .setCategory(NotificationCompat.EXTRA_MESSAGES)
                .setChannel(1)
                .setNotificationID(1)
                .setDrawable(R.mipmap.ic_launcher_round)
                .setTitle(title)
                .setMessage(message)
                .setIntent(intent);
        notify.buildNotification();
    }

    public static void CancelAppointment(Context context, String title, String message){
        Notify notify = new Notify(context)
                .setCategory(NotificationCompat.EXTRA_MESSAGES)
                .setChannel(1)
                .setNotificationID(1)
                .setDrawable(R.mipmap.ic_launcher_round)
                .setTitle(title)
                .setMessage(message);
        notify.buildNotification();
    }

    public static void LeaseExpired(Context context, String title, String message){
        Intent intent =  new Intent(context, ViewLeaseStatus.class);
        Notify notify = new Notify(context)
                .setCategory(NotificationCompat.EXTRA_MESSAGES)
                .setChannel(1)
                .setNotificationID(9)
                .setDrawable(R.mipmap.ic_launcher_round)
                .setTitle(title)
                .setMessage(message)
                .setIntent(intent);
        notify.buildNotification();
    }
}
