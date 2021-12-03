package com.example.esqueletoapp.Utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String sNotifToken) {
        SharedPreferences userData = getApplicationContext()
                .getSharedPreferences("UserData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = userData.edit();
        editor.putString("NotifToken",sNotifToken);
        editor.apply();
        Log.d("TokenNotif", "cambio el token");
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("TokenNotif", "Message data payload: " + remoteMessage.getData());
            //se puede ignorar esto, es por si queres mandar mas datos que no se muestran en la propia notificacion
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("TokenNotif", "Message Notification Body: " + remoteMessage.getNotification().getBody());

            Log.d("TokenNotif", "Message Notification Title: " + remoteMessage.getNotification().getTitle());
        }
    }
}
