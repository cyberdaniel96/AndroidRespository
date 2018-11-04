package com.example.johnn.lodgingservicesystemstudent;

import android.content.Context;
import android.support.v7.app.NotificationCompat;

public class AppointmentNotifier{

    public static void CancelAppointment(Context context, String title, String message){
        Notify notify = new Notify(context)
                .setCategory(NotificationCompat.EXTRA_MESSAGES)
                .setChannel(1)
                .setChannelID(1)
                .setDrawable(R.mipmap.ic_launcher_round)
                .setTitle(title)
                .setMessage(message);
        notify.BuildNotification();
    }
}
