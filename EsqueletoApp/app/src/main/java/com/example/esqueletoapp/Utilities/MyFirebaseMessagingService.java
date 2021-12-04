package com.example.esqueletoapp.Utilities;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.esqueletoapp.R;
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
        super.onMessageReceived(remoteMessage);
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d("TokenNotif", "Message data payload: " + remoteMessage.getData());
            //se puede ignorar esto, es por si queres mandar mas datos que no se muestran en la propia notificacion
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            NotificationManager notificationManager = (NotificationManager)getSystemService
                    (Context.NOTIFICATION_SERVICE);
            Notification notification= new NotificationCompat.Builder(this,"Default")
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(remoteMessage.getNotification().getBody()))
                    .setSmallIcon(R.drawable.ic_zabbix_notif).build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("Default", "Default channel", NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(0,notification);
        }
    }
}
