package com.example.esqueletoapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.esqueletoapp.Fragments.DashboardFragment;
import com.example.esqueletoapp.Fragments.HostFragment;
import com.example.esqueletoapp.Fragments.ProblemFragment;
import com.example.esqueletoapp.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

public class MenuActivity extends AppCompatActivity {
    private MaterialCardView cardDashboard, cardProblems, cardHosts;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        cardDashboard = findViewById(R.id.cardDashboard);
        cardHosts = findViewById(R.id.cardHosts);
        cardProblems = findViewById(R.id.cardProblems);

        toolbar = findViewById(R.id.toolbarMainMenu);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        cardDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DashboardFragment dashboardFragment = new DashboardFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.constraintMenu,dashboardFragment).
                        addToBackStack(null).commit();
                cardDashboard.setVisibility(View.GONE);
                cardHosts.setVisibility(View.GONE);
                cardProblems.setVisibility(View.GONE);
            }
        });

        cardHosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HostFragment hostFragment = new HostFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.constraintMenu,hostFragment).
                        addToBackStack(null).commit();
                cardDashboard.setVisibility(View.GONE);
                cardHosts.setVisibility(View.GONE);
                cardProblems.setVisibility(View.GONE);
            }
        });

        cardProblems.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProblemFragment problemFragment = new ProblemFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.constraintMenu,problemFragment).
                        addToBackStack(null).commit();
                cardDashboard.setVisibility(View.GONE);
                cardHosts.setVisibility(View.GONE);
                cardProblems.setVisibility(View.GONE);
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
        } else if(count == 1){
            getSupportFragmentManager().popBackStack();
            cardDashboard.setVisibility(View.VISIBLE);
            cardHosts.setVisibility(View.VISIBLE);
            cardProblems.setVisibility(View.VISIBLE);
        }else{
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