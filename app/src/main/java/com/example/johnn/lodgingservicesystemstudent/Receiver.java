package com.example.johnn.lodgingservicesystemstudent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String myAction = intent.getAction();
        boolean noConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        if(!noConnection){
            context.startService(new Intent(context, Services.class));
        }

    }

}
