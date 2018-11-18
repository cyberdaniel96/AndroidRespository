package com.example.johnn.lodgingservicesystemstudent;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import domain.Appointment;
import service.Converter;

public class MonthCalendarWidget extends AppWidgetProvider {
    //*************************
    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = Home.broker;
    String clientId = "";
    String receiverClientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    final Converter c = new Converter();
    private HashMap<String, Appointment> appMap = new HashMap<>();
    private Context context;
    //***********************************
    public void Connect() throws Exception {

        client = new MqttAndroidClient(context.getApplicationContext(), broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(false);

        client.connect(connOpts, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Subscribe();
                Retrieve();
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
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = c.convertToString(datas[0]);
                String command = head[0];
                String reserve = head[1];
                String senderID = head[2];
                String receiverID = head[3];
                if(receiverID.equals(clientId)){
                    if(command.equals("004831")){
                        int totalList = Integer.parseInt(head[4]);
                        for (int index = 1; index <= totalList; index++) {
                            String[] data = c.convertToString(datas[index]);
                            Appointment app = new Appointment(data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9]);
                            if (app.getStatus().equals("accepted")) {
                                String date = app.getDateTime().split("AND")[0];
                                appMap.put(date,app);

                            }
                        }

                        redrawWidgets(context);
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

    public void Retrieve(){
        String payload = c.convertToHex(new String[]{"004831", "000000000000000000000000", clientId, "serverLSSserver",clientId.substring(0, clientId.length()-1)});
        Publish(payload);
    }

//***************************************

    private static final String ACTION_PREVIOUS_MONTH
            = "com.example.android.monthcalendarwidget.action.PREVIOUS_MONTH";
    private static final String ACTION_NEXT_MONTH
            = "com.example.android.monthcalendarwidget.action.NEXT_MONTH";
    private static final String ACTION_RESET_MONTH
            = "com.example.android.monthcalendarwidget.action.RESET_MONTH";
    private static final String Action_Day
            ="com.example.android.monthcalendarwidget.DAY_CLICK";

    private static final String PREF_MONTH = "month";
    private static final String PREF_YEAR = "year";

    private String userID = "";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        SharedPreferences userDetails = context.getSharedPreferences("LoggedInUser", context.MODE_PRIVATE);
        this.userID = userDetails.getString("UserID","");
        this.clientId = userID + 7;
        this.context = context;

        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int appWidgetId : appWidgetIds) {
            drawWidget(context, appWidgetId);
        }

    }


    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String[] action = intent.getAction().split("@");
        this.appMap = getMap(context);

        if(!appMap.isEmpty()){

            if(Action_Day.equals(action[0])){
                Appointment app = null;
                app = appMap.get(action[1]);
                Intent passIntent = new Intent(context, ViewAppointmentDetails.class);
                passIntent.putExtra("anAppointment",app);


                context.startActivity(passIntent);
            }
            redrawWidgets(context);
        }

        if (ACTION_PREVIOUS_MONTH.equals(action[0])) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar cal = Calendar.getInstance();
            int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
            int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MONTH, thisMonth);
            cal.set(Calendar.YEAR, thisYear);
            cal.add(Calendar.MONTH, -1);
            sp.edit()
                    .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                    .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                    .apply();
            redrawWidgets(context);

        } else if (ACTION_NEXT_MONTH.equals(action[0])) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            Calendar cal = Calendar.getInstance();
            int thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
            int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MONTH, thisMonth);
            cal.set(Calendar.YEAR, thisYear);
            cal.add(Calendar.MONTH, 1);
            sp.edit()
                    .putInt(PREF_MONTH, cal.get(Calendar.MONTH))
                    .putInt(PREF_YEAR, cal.get(Calendar.YEAR))
                    .apply();
            redrawWidgets(context);

        } else if (ACTION_RESET_MONTH.equals(action[0])) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            sp.edit().remove(PREF_MONTH).remove(PREF_YEAR).apply();
            redrawWidgets(context);
        }
    }

    @Override
    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        drawWidget(context, appWidgetId);
    }

    private void drawWidget(Context context, int appWidgetId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Resources res = context.getResources();
        Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        boolean shortMonthName = false;
        boolean mini = false;
        int numWeeks = 6;
        if (widgetOptions != null) {
            int minWidthDp = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int minHeightDp = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            shortMonthName = minWidthDp <= res.getInteger(R.integer.max_width_short_month_label_dp);
            mini = minHeightDp <= res.getInteger(R.integer.max_height_mini_view_dp);
            if (mini) {
                numWeeks = minHeightDp <= res.getInteger(R.integer.max_height_mini_view_1_row_dp)
                        ? 1 : 2;
            }
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);

        Calendar cal = Calendar.getInstance();
        int today = cal.get(Calendar.DAY_OF_YEAR); //Number of day for the years
        int todayYear = cal.get(Calendar.YEAR); //get the year

        int thisMonth;
        if (!mini) {
            thisMonth = sp.getInt(PREF_MONTH, cal.get(Calendar.MONTH));
            int thisYear = sp.getInt(PREF_YEAR, cal.get(Calendar.YEAR));
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MONTH, thisMonth);
            cal.set(Calendar.YEAR, thisYear);
        } else {
            thisMonth = cal.get(Calendar.MONTH);
        }

        rv.setTextViewText(R.id.month_label, DateFormat.format(
                shortMonthName ? "MMM yy" : "MMMM yyyy", cal));

        if (!mini) {
            cal.set(Calendar.DAY_OF_MONTH, 1);
            int monthStartDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_MONTH, 1 - monthStartDayOfWeek);
        } else {
            int todayDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            cal.add(Calendar.DAY_OF_MONTH, 1 - todayDayOfWeek);
        }

        rv.removeAllViews(R.id.calendar);

        RemoteViews headerRowRv = new RemoteViews(context.getPackageName(),
                R.layout.row_header);
        DateFormatSymbols dfs = DateFormatSymbols.getInstance();
        String[] weekdays = dfs.getShortWeekdays();
        for (int day = Calendar.SUNDAY; day <= Calendar.SATURDAY; day++) {
            RemoteViews dayRv = new RemoteViews(context.getPackageName(), R.layout.cell_header);
            dayRv.setTextViewText(android.R.id.text1, weekdays[day]);
            headerRowRv.addView(R.id.row_container, dayRv);
        }
        rv.addView(R.id.calendar, headerRowRv);
        Toast.makeText(context, appMap.isEmpty()+"", Toast.LENGTH_LONG).show();
        if (!appMap.isEmpty()) {
            saveMap(context, appMap);
            for (int week = 0; week < numWeeks; week++) {
                RemoteViews rowRv = new RemoteViews(context.getPackageName(), R.layout.row_week);
                for (int day = 0; day < 7; day++) {
                    boolean inMonth = cal.get(Calendar.MONTH) == thisMonth;
                    boolean inYear = cal.get(Calendar.YEAR) == todayYear;
                    boolean isToday = inYear && inMonth && (cal.get(Calendar.DAY_OF_YEAR) == today);
                    boolean isFirstOfMonth = cal.get(Calendar.DAY_OF_MONTH) == 1;
                    String dateInStr = cal.get(Calendar.DAY_OF_MONTH) +"/"+ (cal.get(Calendar.MONTH)+1) +"/"+cal.get(Calendar.YEAR);
                    boolean appDay = false;

                    //check contain
                    if(appMap.containsKey(dateInStr)){
                       appDay = true;
                    }


                    int cellLayoutResId = R.layout.cell_day;
                    Intent intent = new Intent(context, MonthCalendarWidget.class);
                    if (isToday) {
                        cellLayoutResId = R.layout.cell_today;
                        intent.setAction("NONE");
                        intent.putExtra("Layout_ID", R.layout.cell_today);

                    } else if (inMonth) {
                        cellLayoutResId = R.layout.cell_day_this_month;
                        intent.setAction("NONE");
                        intent.putExtra("Layout_ID", R.layout.cell_day_this_month);
                    }
                    if (appDay) {
                        cellLayoutResId = R.layout.cell_has_appointment;
                        intent.setAction(Action_Day+"@"+dateInStr);
                        intent.putExtra("Layout_ID", R.layout.cell_has_appointment);
                    }
                    if (appDay && isToday) {
                        cellLayoutResId = R.layout.cell_has_appointment;
                        intent.setAction(Action_Day+"@"+dateInStr);
                        intent.putExtra("Layout_ID", R.layout.cell_has_appointment);
                    }
                    RemoteViews cellRv = new RemoteViews(context.getPackageName(), cellLayoutResId);
                    cellRv.setTextViewText(android.R.id.text1,
                            Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));

                    if (isFirstOfMonth) {
                        cellRv.setTextViewText(R.id.month_label, DateFormat.format("MMM", cal));
                    }

                    cellRv.setOnClickPendingIntent(android.R.id.text1, PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
                    ));

                    rowRv.addView(R.id.row_container, cellRv);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                rv.addView(R.id.calendar, rowRv);

                rv.setViewVisibility(R.id.prev_month_button, mini ? View.GONE : View.VISIBLE);

                rv.setOnClickPendingIntent(R.id.prev_month_button,
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, MonthCalendarWidget.class)
                                        .setAction(ACTION_PREVIOUS_MONTH),
                                PendingIntent.FLAG_UPDATE_CURRENT));

                rv.setViewVisibility(R.id.next_month_button, mini ? View.GONE : View.VISIBLE);
                rv.setOnClickPendingIntent(R.id.next_month_button,
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, MonthCalendarWidget.class)
                                        .setAction(ACTION_NEXT_MONTH),
                                PendingIntent.FLAG_UPDATE_CURRENT));
                rv.setOnClickPendingIntent(R.id.month_label,
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, MonthCalendarWidget.class)
                                        .setAction(ACTION_RESET_MONTH),
                                PendingIntent.FLAG_UPDATE_CURRENT));
                rv.setViewVisibility(R.id.month_bar, numWeeks <= 1 ? View.GONE : View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, rv);
            }
        }else{

            for (int week = 0; week < numWeeks; week++) {
                RemoteViews rowRv = new RemoteViews(context.getPackageName(), R.layout.row_week);
                for (int day = 0; day < 7; day++) {
                    boolean inMonth = cal.get(Calendar.MONTH) == thisMonth;
                    boolean inYear = cal.get(Calendar.YEAR) == todayYear;
                    boolean isToday = inYear && inMonth && (cal.get(Calendar.DAY_OF_YEAR) == today);
                    boolean isFirstOfMonth = cal.get(Calendar.DAY_OF_MONTH) == 1;
                    String dateInStr = cal.get(Calendar.DAY_OF_MONTH) +"/"+ (cal.get(Calendar.MONTH)+1) +"/"+cal.get(Calendar.YEAR);
                    boolean appDay = false;


                    int cellLayoutResId = R.layout.cell_day;




                    if (isToday) {
                        cellLayoutResId = R.layout.cell_today;

                    } else if (inMonth) {
                        cellLayoutResId = R.layout.cell_day_this_month;
                    }


                    RemoteViews cellRv = new RemoteViews(context.getPackageName(), cellLayoutResId);
                    cellRv.setTextViewText(android.R.id.text1,
                            Integer.toString(cal.get(Calendar.DAY_OF_MONTH)));
                    if (isFirstOfMonth) {
                        cellRv.setTextViewText(R.id.month_label, DateFormat.format("MMM", cal));
                    }
                    rowRv.addView(R.id.row_container, cellRv);
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                }
                rv.addView(R.id.calendar, rowRv);

                rv.setViewVisibility(R.id.prev_month_button, mini ? View.GONE : View.VISIBLE);

                rv.setOnClickPendingIntent(R.id.prev_month_button,
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, MonthCalendarWidget.class)
                                        .setAction(ACTION_PREVIOUS_MONTH),
                                PendingIntent.FLAG_UPDATE_CURRENT));

                rv.setViewVisibility(R.id.next_month_button, mini ? View.GONE : View.VISIBLE);
                rv.setOnClickPendingIntent(R.id.next_month_button,
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, MonthCalendarWidget.class)
                                        .setAction(ACTION_NEXT_MONTH),
                                PendingIntent.FLAG_UPDATE_CURRENT));
                rv.setOnClickPendingIntent(R.id.month_label,
                        PendingIntent.getBroadcast(context, 0,
                                new Intent(context, MonthCalendarWidget.class)
                                        .setAction(ACTION_RESET_MONTH),
                                PendingIntent.FLAG_UPDATE_CURRENT));
                rv.setViewVisibility(R.id.month_bar, numWeeks <= 1 ? View.GONE : View.VISIBLE);
                appWidgetManager.updateAppWidget(appWidgetId, rv);
            }
        }
    }

    private void redrawWidgets(Context context) {
        int[] appWidgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context, MonthCalendarWidget.class));

        for (int appWidgetId : appWidgetIds) {
            drawWidget(context, appWidgetId);
        }
    }


    public void saveMap(Context context, HashMap<String, Appointment> hashMap){
        SharedPreferences.Editor sharedPreferences = context.getSharedPreferences("appMap",Context.MODE_PRIVATE).edit();
        sharedPreferences.clear();
        sharedPreferences.commit();
        HashMap<String, Appointment> tempMap = hashMap;

        for(String key: tempMap.keySet()){
            Appointment value = tempMap.get(key);
            sharedPreferences.putString(key, c.convertToHex(new String[]{value.getAppointmentID(), value.getDateTime(), value.getReason(),
            value.getState(), value.getPriority(), value.getComment(),value.getState(), value.getLodgingID(), value.getTenantID(), value.getOwnerID()}));
        }
        sharedPreferences.apply();
    }

    public HashMap<String, Appointment> getMap(Context context){
        SharedPreferences prefs = context.getSharedPreferences("appMap", Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();
        Map<String, Appointment> mapReturn = new HashMap<>();
        for(String key: map.keySet()){
            String[] value = c.convertToString(map.get(key).toString());
            Appointment app = new Appointment(value[0],value[1],value[2],value[3],value[4],value[5],value[6],value[7],value[8],value[9]);
            mapReturn.put(key, app);
        }

        return (HashMap<String, Appointment>) mapReturn;
    }
}
