package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import domain.Appointment;

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
        Intent intent =  new Intent(context, Testing.class);
        intent.putExtra("MyData", "DAta Here");



        Notify notify = new Notify(context)
                .setCategory(NotificationCompat.EXTRA_MESSAGES)
                .setChannel(1)
                .setNotificationID(1)
                .setDrawable(R.mipmap.ic_launcher_round)
                .setTitle(title)
                .setMessage(message)
                .setIntent(intent)
                .setAction("ACTION_1", "ACTION_1_TITLE");
        notify.buildNotification();
    }
}
