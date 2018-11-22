package com.example.johnn.lodgingservicesystemstudent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import domain.Lodging;
import domain.Rental;

public class ViewRentalList extends AppCompatActivity {

    ArrayList<Rental> current = new ArrayList<>();
    ArrayList<Rental> histoty = new ArrayList<>();
    Lodging lodging = new Lodging();

    TextView viewTitle, viewAddress;

    RecyclerView recyclerView;
    RentalListAdapter adapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_rental_list);

        Intent i = getIntent();
        current = (ArrayList<Rental>) i.getSerializableExtra("RentalCurrent");
        histoty = (ArrayList<Rental>) i.getSerializableExtra("RentalHistory");
        lodging = (Lodging) i.getSerializableExtra("SpecificLodging");
        Toast.makeText(this, current.size()+"", Toast.LENGTH_LONG).show();
        viewTitle = (TextView) findViewById(R.id.viewTitle);
        viewAddress = (TextView) findViewById(R.id.viewAddress);

        viewTitle.setText(lodging.getTitle());
        viewAddress.setText(lodging.getAddress());

        recyclerView = (RecyclerView) findViewById(R.id.RVRentalList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RentalListAdapter(this, current);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
