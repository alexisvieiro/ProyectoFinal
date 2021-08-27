package com.example.esqueletoapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText edtUsername;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnLogin = findViewById(R.id.buttonLogin);
        edtUsername = findViewById(R.id.textName);
        edtPassword = findViewById(R.id.textPassword);

        final Intent aMenu = new Intent(MainActivity.this, MenuActivity.class);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sUsername = edtUsername.getText().toString();
                String sPassword = edtPassword.getText().toString();
                //if(sUsername.equals("AdministradorUTNEricnet")){
                if(sUsername.equals("hola")){
                    //if(sPassword.equals("DinosaurioElectronico39744836")){
                    if(sPassword.equals("1234")){
                        startActivity(aMenu);
                        finish();
                    }else{
                        Snackbar.make(v,"Contraseña incorrecta", Snackbar.LENGTH_LONG).show();
                        edtPassword.setError("Contraseña incorrecta");
                    }
                }else{
                    Snackbar.make(v,"No existe usuario", Snackbar.LENGTH_LONG).show();
                    edtUsername.setError("No existe usuario");
                }
            }
        });
    }
}