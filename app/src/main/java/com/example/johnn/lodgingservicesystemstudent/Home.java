package com.example.johnn.lodgingservicesystemstudent;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import service.Converter;
import service.SessionManager;
import domain.Lodging;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    boolean doubleBackToExitPressedOnce = false;
    MqttAndroidClient client;
    String topic = "MY/TARUC/LSS/000000001/PUB";
    int qos = 1;
    String broker = "tcp://test.mosquitto.org:1883";
    String clientId = "";
    MemoryPersistence persistence = new MemoryPersistence();
    Converter c = new Converter();
    List<Lodging> ll = new ArrayList<>();
    ProgressDialog pb;
    public static String ip = "192.168.0.171";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        pb = new ProgressDialog(this);
        pb.setCanceledOnTouchOutside(false);
        pb.setMessage("Loading...");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        TextView userId = header.findViewById(R.id.userIDTv);
        TextView email = header.findViewById(R.id.emailTv);

        userId.setText(getIntent().getStringExtra("UserID"));
        email.setText(getIntent().getStringExtra("Email"));

        final SearchView s = (SearchView) findViewById(R.id.keywordSV);
        clientId = getIntent().getStringExtra("UserID");
        s.setQueryHint("Address");

        s.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String command = c.ToHex("004817");
                String reserve = c.ToHex("000000000000000000000000");
                String senderClientId = c.ToHex(clientId);
                String receiverClientId = c.ToHex("serverLSSserver");
                String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId + "/" + c.ToHex(query);
                Publish(payload);
                pb.show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        s.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                pb.show();
                Retrieve();
                return false;
            }
        });


    }

    public void Connect() throws Exception {
        client = new MqttAndroidClient(this, broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);

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
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                Converter c = new Converter();
                String[] datas = mqttMessage.toString().split("\\$");
                String[] head = datas[0].split("/");
                String command = c.ToString(head[0]);
                String receiverClientId = c.ToString(head[3]);
                if (receiverClientId.equals(clientId)) {
                    if (command.equals("004807")) {
                        String size = c.ToString(head[4]);
                        if (size.equals("0")) {
                            Toast.makeText(Home.this, "No recored found", Toast.LENGTH_LONG).show();
                            pb.dismiss();
                        } else {
                            SetLodging(mqttMessage.toString());
                            SetData();
                        }
                    } else if (command.equals("004817")) {
                        String state = c.ToString(head[4]);
                        if (state.equals("0")) {
                            Toast.makeText(Home.this, "No recored found", Toast.LENGTH_LONG).show();
                            pb.dismiss();
                        } else {
                            SetLodging(mqttMessage.toString());
                            SetData();
                        }
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
            client.subscribe(topic, 1);
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

    public void Retrieve() {
        String command = c.ToHex("004807");
        String reserve = c.ToHex("000000000000000000000000");
        String senderClientId = c.ToHex(clientId);
        String receiverClientId = c.ToHex("serverLSSserver");
        String payload = command + "/" + reserve + "/" + senderClientId + "/" + receiverClientId;
        Publish(payload);
    }

    public void SetData() throws Exception {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.lodgingRV);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        final List<Lodging> temp = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        Date current = new Date();
        Date expire = new Date();
        c.setTime(current);
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String scurrent = df.format(c.getTime());
        for (int i = 0; i < ll.size(); i++) {
            expire = df.parse(ll.get(i).getExpireDate());
            current = df.parse(scurrent);
            if (expire.compareTo(current) > 0) {
                temp.add(ll.get(i));
            }
        }
        LodgingAdapter lodgingAdapter = new LodgingAdapter(temp);
        recyclerView.setAdapter(lodgingAdapter);
        pb.dismiss();
        lodgingAdapter.setOnItemClickListener(new LodgingAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Intent intent = new Intent(Home.this, ViewLodgingDetails.class);
                intent.putExtra("LodgingID", temp.get(position).getLodgingId());
                intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
                startActivity(intent);
            }
        });
    }

    public void SetLodging(String message) {
        ll.clear();
        String[] datas = message.split("\\$");
        String[] head = datas[0].split("/");
        int size = Integer.parseInt(c.ToString(head[4]));
        for (int i = 0; i < size; i++) {
            Lodging l = new Lodging();
            String[] body = datas[i + 1].split("/");
            l.setLodgingId(c.ToString(body[0]));
            l.setTitle(c.ToString(body[1]));
            l.setPrice(Double.parseDouble(c.ToString(body[2])));
            l.setExpireDate(c.ToString(body[3]));
            l.setLodgingType(c.ToString(body[4]));
            l.setImage(c.ToString(body[5]));
            ll.add(l);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please tap BACK again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_addLodging) {
            Intent intent = new Intent(this, AddLodging.class);
            intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
            startActivity(intent);
        } else if (id == R.id.nav_viewLodging) {
            Intent intent = new Intent(this, MyLodging.class);
            intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
            startActivity(intent);
        } else if (id == R.id.nav_viewFavourite) {
            Intent intent = new Intent(this, FavouriteLodging.class);
            intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
            startActivity(intent);
        } else if (id == R.id.nav_info) {
            Intent intent = new Intent(this, AboutUs.class);
            intent.putExtra("UserID", getIntent().getStringExtra("UserID"));
            intent.putExtra("Email", getIntent().getStringExtra("Email"));
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            SessionManager session = new SessionManager(getApplicationContext());
            session.logoutUser();
            this.finish();
        }else if(id == R.id.nav_private_chat){
            Intent intent = new Intent(this, Listed_Private_Chat.class);
            startActivity(intent);
        }else if(id == R.id.nav_viewAppointment){
            Intent intent = new Intent(this, ViewAppointment.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);
        ImageView profile = header.findViewById(R.id.profilePicIv);
        Glide.with(this)
                .load("http://" + ip + "/img/User/" + getIntent().getStringExtra("UserID") + ".jpg")
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .transform(new CircleTransform(this))
                .into(profile);
        pb.show();
        try {
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            client.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
