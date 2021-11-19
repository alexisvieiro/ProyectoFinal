package com.example.myapplication;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    @Override
    public void onNewToken(String token) {
        //Guardo TOKEN nuevo/actualizado
        SharedPreferences settings = getApplicationContext().getSharedPreferences("TokenNotifApp", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("Token", token);
        editor.apply();
        //Guardo TOKEN nuevo/actualizado
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.
        Log.d("TokenNotif", "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("TokenNotif", "Message data payload: " + remoteMessage.getData());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TokenNotif", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

}