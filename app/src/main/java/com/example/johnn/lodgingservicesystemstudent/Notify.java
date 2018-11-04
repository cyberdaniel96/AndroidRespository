package com.example.johnn.lodgingservicesystemstudent;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class Notify {
    public static final String CHANNEL_0_ID = "Channel0";
    public static final String CHANNEL_1_ID = "Channel1";
    //public static final String CHANNEL_2_ID = "Channel2";

    private NotificationManagerCompat notificationManagerCompat;
    private Context context;

    private int channel;
    private int channel_ID;
    private int drawable;
    private String category;
    private String title;
    private String message;


    public Notify(Context context) {
        notificationManagerCompat = NotificationManagerCompat.from(context);
        this.context = context;

        this.channel = 0;
        this.channel_ID = 0; //do not duplicate assign the channel_id
        this.drawable = R.drawable.ic_android_default_notification_icon_24dp;
        this.category = NotificationCompat.CATEGORY_MESSAGE;
        this.title = "DEMO NOTIFICATION";
        this.message = "Palce you content here.";

    }

    public Notify BuildNotification() {
        Notification notification = new Notification.Builder(context, getChannel(channel))
                .setSmallIcon(drawable)
                .setPriority(getPriority(channel))
                .setCategory(category)
                .setContentTitle(title)
                .setContentText(message)
                .build();
        notificationManagerCompat.notify(channel_ID, notification);
        return this;
    }

    public Notify setChannel(int channel){
        this.channel = channel;
        return this;
    }

    public Notify setChannelID(int channel_ID){
        this.channel_ID = channel_ID;
        return this;
    }

    public Notify setDrawable(int drawable){
        this.drawable = drawable;
        return this;
    }

    public Notify setCategory(String category){
        this.category = category;
        return this;
    }

    public Notify setTitle(String title){
        this.title = title;
        return this;
    }

    public Notify setMessage(String message){
        this.message = message;
        return this;
    }

    private int getPriority(int channel) {
        switch (channel) {
            case 0:
                return NotificationCompat.PRIORITY_DEFAULT;
            case 1:
                return NotificationCompat.PRIORITY_HIGH;
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
        }
    }

    private String getChannel(int channel) {
        switch (channel) {
            case 0:
                return CHANNEL_0_ID;
            case 1:
                return CHANNEL_1_ID;
            default:
                return CHANNEL_0_ID;
        }
    }

}
