package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button ButtonSubMail = (Button) findViewById(R.id.idSubMail);

        ButtonSubMail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get from the SharedPreferences
                SharedPreferences settings = getApplicationContext().getSharedPreferences("TokenNotifApp",0);
                String TOKEN = settings.getString("Token","");
                if(TOKEN!=""){
                    String RECIPIENT = "";
                    String MESSAGE = "Token: "+ TOKEN;
                    String SUBJECT = "Solicitud de Alta de Usuario - Zabbix";
                    Intent emailIntent = new Intent(Intent.ACTION_VIEW);
                    emailIntent.setType("message/rfc822");
                    emailIntent.setData(Uri.parse("mailto:?subject=" + SUBJECT+ " &body=" + MESSAGE + " &to= " + RECIPIENT));

                    try {
                        startActivity(Intent.createChooser(emailIntent, "Elegir cliente de mail..."));
                        finish();
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(MainActivity.this, "No hay cliente de mail instalado.", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "No hay token generado.", Toast.LENGTH_SHORT).show();
                }

            }


        });



    }

}