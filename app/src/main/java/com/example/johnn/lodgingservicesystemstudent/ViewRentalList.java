package com.example.johnn.lodgingservicesystemstudent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import domain.Lodging;
import domain.Rental;

public class ViewRentalList extends AppCompatActivity {


    ArrayList<Rental> current = new ArrayList<>();
    ArrayList<Rental> history = new ArrayList<>();
    Lodging lodging = new Lodging();

    TextView viewTitle, viewAddress, viewNoRecord;
    Button btnCurrent, btnHistory;
    ImageView img;


    RecyclerView recyclerView;
    RentalListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rental_list);

        Intent i = getIntent();
        current = (ArrayList<Rental>) i.getSerializableExtra("RentalCurrent");
        history = (ArrayList<Rental>) i.getSerializableExtra("RentalHistory");
        lodging = (Lodging) i.getSerializableExtra("SpecificLodging");
        viewTitle = (TextView) findViewById(R.id.viewTitle);
        viewAddress = (TextView) findViewById(R.id.viewAddress);
        viewNoRecord  = (TextView)findViewById(R.id.txtNoRecord);
        img = (ImageView) findViewById(R.id.imageView);

        viewTitle.setText(lodging.getTitle());
        viewAddress.setText(lodging.getAddress());
        Glide.with(this)
                .load(lodging.getImage())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(img);
        btnCurrent = (Button)findViewById(R.id.btnCurrent);
        btnHistory = (Button) findViewById(R.id.btnHistory);

        recyclerView = (RecyclerView) findViewById(R.id.RVRentalList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RentalListAdapter(this, current);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        if(adapter.getItemCount() != 0){
            viewNoRecord.setVisibility(View.GONE);
        }else if(adapter.getItemCount() == 0){
            viewNoRecord.setVisibility(View.VISIBLE);
        }

        btnCurrent.setOnClickListener(btnCurrentClickListener);
        btnHistory.setOnClickListener(btnHistoryListener);

        adapter.setOnClickListener(adapterOnClickListener);
    }


    private View.OnClickListener btnCurrentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            adapter = new RentalListAdapter(ViewRentalList.this, current);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.setOnClickListener(adapterOnClickListener);

            if(adapter.getItemCount() != 0){
                viewNoRecord.setVisibility(View.GONE);
            }else if(adapter.getItemCount() == 0){
                viewNoRecord.setVisibility(View.VISIBLE);
            }


            btnCurrent.setBackground(ContextCompat.getDrawable(ViewRentalList.this, R.drawable.btnclickable));
            btnHistory.setBackground(ContextCompat.getDrawable(ViewRentalList.this, R.drawable.btnnon_clickable));
        }
    };

    private View.OnClickListener btnHistoryListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            adapter = new RentalListAdapter(ViewRentalList.this, history);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            adapter.setOnClickListener(adapterOnClickListener);

            if(adapter.getItemCount() != 0){
                viewNoRecord.setVisibility(View.GONE);
            }else if(adapter.getItemCount() == 0){
                viewNoRecord.setVisibility(View.VISIBLE);
            }
            btnCurrent.setBackground(ContextCompat.getDrawable(ViewRentalList.this, R.drawable.btnnon_clickable));
            btnHistory.setBackground(ContextCompat.getDrawable(ViewRentalList.this, R.drawable.btnclickable));
        }

    };


    private RentalListAdapter.OnClickListener adapterOnClickListener = new RentalListAdapter.OnClickListener() {
        @Override
        public void onClick(View v, int position, Rental rental) {
            Intent tempIntent = getIntent();
            Intent intent = new Intent(ViewRentalList.this, ViewReceipt.class);
            intent.putExtra("LeaseID", tempIntent.getStringExtra("LeaseID"));
            intent.putExtra("RentalID", rental.getRentalID());
            startActivity(intent);
            ViewRentalList.this.finish();
        }
    };
}
