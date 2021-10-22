package com.example.esqueletoapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.esqueletoapp.Adapters.DeviceSampleAdapter;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MenuActivity extends AppCompatActivity {

    private RecyclerView rclDeviceList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DeviceSampleAdapter deviceSampleAdapter;
    private ArrayList<DeviceSampleItem> sampleItemArrayList = new ArrayList<>();
    private Handler hostHandler;
    private String sMessage;
    private JSONObject jsonResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        rclDeviceList = findViewById(R.id.listDevices);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        SharedPreferences userData = getSharedPreferences("UserData", MODE_PRIVATE);
        String sToken = userData.getString("Token",null);

        rclDeviceList.setHasFixedSize(true);
        rclDeviceList.setLayoutManager(new LinearLayoutManager(this));
        rclDeviceList.addItemDecoration
                (new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));

        deviceSampleAdapter = new DeviceSampleAdapter(sampleItemArrayList, getApplicationContext());
        rclDeviceList.setAdapter(deviceSampleAdapter);

        sampleItemArrayList.add(new DeviceSampleItem("Tablero"));
        sampleItemArrayList.add(new DeviceSampleItem("Hosts"));
        sampleItemArrayList.add(new DeviceSampleItem("Problemas"));

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                recreate();
            }
        });
    }
}