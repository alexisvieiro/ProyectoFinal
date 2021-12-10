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
    private DeviceSampleAdapter deviceSampleAdapter;
    private ArrayList<DeviceSampleItem> sampleItemArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        rclDeviceList = findViewById(R.id.listDevices);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notifications_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_request:
                SharedPreferences userData = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                String sNotifToken =userData.getString("NotifToken","");
                AlertDialog.Builder requestDialog = new
                        AlertDialog.Builder(this);
                requestDialog.setTitle("¿Solicitar alta de notificaciones?");
                requestDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(sNotifToken!=""){
                            RequestNotifications(dialog, sNotifToken);
                        }else{
                            Snackbar.make(getWindow().getDecorView(),
                                    "No hay token generado",Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
                requestDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                requestDialog.show();
                break;
            case R.id.action_unrequest:
                AlertDialog.Builder unrequestDialog = new
                        AlertDialog.Builder(this);
                unrequestDialog.setTitle("¿Solicitar baja de notificaciones?");
                unrequestDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UnrequestNotifications(dialog);
                    }
                });
                unrequestDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                unrequestDialog.show();
                break;
            case R.id.action_logout:
                AlertDialog.Builder logoutDialog = new AlertDialog.Builder(this);
                logoutDialog.setTitle("¿Cerrar sesión?");
                logoutDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent aMain = new Intent(MenuActivity.this, MainActivity.class);
                        startActivity(aMain);
                        finish();
                    }
                });
                logoutDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                logoutDialog.show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            moveTaskToBack(true);
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void RequestNotifications(DialogInterface dialog, String sNotifToken){
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
            dialog.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MenuActivity.this,
                    "No hay cliente de mail instalado.", Toast.LENGTH_SHORT).show();
        }
    }

    private void UnrequestNotifications(DialogInterface dialog){
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
            dialog.dismiss();
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MenuActivity.this,
                    "No hay cliente de mail instalado.", Toast.LENGTH_SHORT).show();
        }
    }
}