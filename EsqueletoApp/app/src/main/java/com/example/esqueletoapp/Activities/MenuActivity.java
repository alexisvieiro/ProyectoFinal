package com.example.esqueletoapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.widget.Toast;

import com.example.esqueletoapp.Adapters.DeviceSampleAdapter;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.R;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView rclDeviceList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DeviceSampleAdapter deviceSampleAdapter;
    private ArrayList<DeviceSampleItem> sampleItemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        rclDeviceList = findViewById(R.id.listDevices);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        if(swipeRefreshLayout.isRefreshing()){
            Toast.makeText(this,"cargando",Toast.LENGTH_SHORT).show();
        }

        rclDeviceList.setHasFixedSize(true);
        rclDeviceList.setLayoutManager(new LinearLayoutManager(this));
        rclDeviceList.addItemDecoration
                (new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        deviceSampleAdapter = new DeviceSampleAdapter(sampleItemArrayList, this);
        rclDeviceList.setAdapter(deviceSampleAdapter);

        sampleItemArrayList.add(new DeviceSampleItem("dispo 1"));
        sampleItemArrayList.add(new DeviceSampleItem("dispo 2"));
        sampleItemArrayList.add(new DeviceSampleItem("dispo 3"));
        sampleItemArrayList.add(new DeviceSampleItem("dispo 4"));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                recreate();
            }
        });
    }
}