package com.example.johnn.lodgingservicesystemstudent;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

public class Notify {
    public static final String CHANNEL_0_ID = "Channel0";
    public static final String CHANNEL_1_ID = "Channel1";

    private NotificationManagerCompat notificationManagerCompat = null;
    private Notification.Builder notificationBuilder = null;
    private Context context = null;
    private Notification notification = null;

    private int channel;
    private int notification_ID;

    private int drawable;
    private String category;
    private String title;
    private String message;
    private Intent intent;

    public Notify(Context context) {

        this.context = context;
        this.channel = 0;
        this.notification_ID = 0; //do not duplicate assign the channel_id
        this.drawable = R.drawable.ic_android_default_notification_icon_24dp;
        this.category = NotificationCompat.CATEGORY_MESSAGE;
        this.title = "DEMO NOTIFICATION";
        this.message = "Place you content here.";
        this.intent = null;

        notificationManagerCompat = NotificationManagerCompat.from(this.context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new Notification.Builder(context, getChannel(channel));
        }
    }

    //which channel
    public Notify setChannel(int channel){
        this.channel = channel;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(getChannel(channel));
        }
        notificationBuilder.setPriority(getPriority(channel));
        return this;
    }

    //assign channel id
    public Notify setNotificationID(int notification_ID){
        this.notification_ID = notification_ID;
        return this;
    }

    public Notify setDrawable(int drawable){
        this.drawable = drawable;
        notificationBuilder.setSmallIcon(this.drawable);
        return this;
    }

    public Notify setCategory(String category){
        this.category = category;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setCategory(this.category);
        }
        return this;
    }


    public Notify setTitle(String title){
        this.title = title;
        notificationBuilder.setContentTitle(this.title);
        return this;
    }

    public Notify setMessage(String message){
        this.message = message;
        notificationBuilder.setContentText(this.message);
        return this;
    }

    public Notify setIntent(Intent intent){
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);
        this.intent = intent;
        return this;
    }

    public Notify setAction(String action, String title){
        Intent actionIntent = new Intent(context, Receiver.class);
        actionIntent.setAction(action);
        PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, actionIntent, 0);
        notificationBuilder.addAction(R.mipmap.ic_launcher_round, title, actionPendingIntent);
        return this;
    }

    public void buildNotification(){
        notification = notificationBuilder.build();

        if(intent != null){
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
        }

        notificationManagerCompat.notify(this.notification_ID, notification);
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
