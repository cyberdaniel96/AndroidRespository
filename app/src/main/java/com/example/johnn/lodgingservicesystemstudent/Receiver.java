package com.example.johnn.lodgingservicesystemstudent;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String myAction = intent.getAction();
        boolean noConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        Log.e("my action", myAction);
        if(!noConnection){
            context.startService(new Intent(context, Services.class));
        }
        if(myAction.equals("ACTION_2")){

        }
    }

}
