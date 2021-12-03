package com.example.esqueletoapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.esqueletoapp.Adapters.DeviceSampleAdapter;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;

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
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DeviceSampleAdapter deviceSampleAdapter;
    private ArrayList<DeviceSampleItem> sampleItemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        rclDeviceList = findViewById(R.id.listDevices);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        toolbar = findViewById(R.id.toolbarMainMenu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notifications_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_notifications:
                String[] sOptions = {"Activadas", "Desactivadas"};
                SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                Integer iNotifStatus = userData.getInt("Notif",1);
                final Integer[] checkedSelection = {iNotifStatus};
                AlertDialog.Builder notifDialog = new
                        AlertDialog.Builder(this);
                notifDialog.setTitle("¿Activar notificaciones?");
                notifDialog.setSingleChoiceItems(sOptions, iNotifStatus, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        checkedSelection[0] =which;
                    }
                });
                notifDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (checkedSelection[0]==iNotifStatus){
                            Snackbar.make(getWindow().getDecorView(),
                                    "No cambiaste nada papu",Snackbar.LENGTH_LONG).show();
                            dialog.dismiss();
                        }else{
                            if (checkedSelection[0]==0){
                                String sNotifToken =userData.getString("NotifToken","");
                                if(sNotifToken!=""){
                                    RequestNotifications(dialog, sNotifToken, userData);
                                }else{
                                    Snackbar.make(getWindow().getDecorView(),
                                            "No hay token generado",Snackbar.LENGTH_LONG).show();
                                }
                            }else{
                                RequestCancelation(dialog, userData);
                            }
                        }
                    }
                });
                notifDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                notifDialog.show();
                break;
        }
        return true;
    }

    private void RequestNotifications(DialogInterface dialog, String sNotifToken,
                                      SharedPreferences userData){
        String RECIPIENT = "";
        String MESSAGE = "Token: "+ sNotifToken;
        String SUBJECT = "Solicitud de Alta de Usuario - Zabbix";
        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setType("message/rfc822");
        emailIntent.setData(Uri.parse("mailto:?subject=" + SUBJECT+ " &body="
                + MESSAGE + " &to= " + RECIPIENT));
        try {
            startActivity(Intent.createChooser(emailIntent,
                    "Elegir cliente de mail..."));
            SharedPreferences.Editor editor = userData.edit();
            editor.putInt("Notif", 0);
            editor.apply();
            dialog.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MenuActivity.this,
                    "No hay cliente de mail instalado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void RequestCancelation( DialogInterface dialog, SharedPreferences userData){
        String RECIPIENT = "";
        String MESSAGE = "Solicito la baja de mi usuario";
        String SUBJECT = "Solicitud de Baja de Usuario - Zabbix";
        Intent emailIntent = new Intent(Intent.ACTION_VIEW);
        emailIntent.setType("message/rfc822");
        emailIntent.setData(Uri.parse("mailto:?subject=" + SUBJECT+ " &body="
                + MESSAGE + " &to= " + RECIPIENT));
        try {
            startActivity(Intent.createChooser(emailIntent,
                    "Elegir cliente de mail..."));
            SharedPreferences.Editor editor = userData.edit();
            editor.putInt("Notif", 1);
            editor.apply();
            dialog.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MenuActivity.this,
                    "No hay cliente de mail instalado.", Toast.LENGTH_SHORT).show();
        }
    }
}