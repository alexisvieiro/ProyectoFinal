package com.example.zabbixMobileApp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.zabbixMobileApp.R;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends AppCompatActivity {

    private ImageView imgSplash;
    private TimerTask tmrTask;
    private static final long SPLASH_SCREEN_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        imgSplash = findViewById(R.id.splash_logo);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        imgSplash.setAlpha(0f);
        imgSplash.animate().setDuration(1500).alpha(1f);

        tmrTask = new TimerTask() {
            @Override
            public void run() {
                Intent aLogin = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(aLogin);
                finish();
            }
        };

        Timer tTimer = new Timer();
        tTimer.schedule(tmrTask,SPLASH_SCREEN_DELAY);

    }
}