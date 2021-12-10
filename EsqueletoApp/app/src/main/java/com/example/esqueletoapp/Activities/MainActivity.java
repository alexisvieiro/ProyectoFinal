package com.example.esqueletoapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.esqueletoapp.R;
import com.example.esqueletoapp.Utilities.Utilities;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText edtUsername;
    private EditText edtPassword;
    private EditText edtURL;
    private Handler mHandler;
    private String sMessage;
    private JSONObject jsonResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        btnLogin = findViewById(R.id.buttonLogin);
        edtUsername = findViewById(R.id.textName);
        edtPassword = findViewById(R.id.textPassword);
        edtURL = findViewById(R.id.textURL);

        SharedPreferences userData = getSharedPreferences("UserData", MODE_PRIVATE);
        String sUsernameSP = userData.getString("Username",null);
        String sPasswordSP = userData.getString("Password",null);
        String sURLSP = userData.getString("URL",null);
        if ((sUsernameSP!=null)&&(sPasswordSP!=null)&&(sURLSP!=null)){
            edtUsername.setText(sUsernameSP);
            edtPassword.setText(sPasswordSP);
            edtURL.setText(sURLSP);
        }

        final Intent aMenu = new Intent(MainActivity.this, MenuActivity.class);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sUsername = edtUsername.getText().toString();
                String sPassword = edtPassword.getText().toString();
                String sURL = edtURL.getText().toString();
                mHandler = new Handler(Looper.getMainLooper());
                OkHttpClient client = new OkHttpClient().newBuilder().build();
                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(Utilities.LOGIN_REQUEST_1 +
                        sUsername +
                        Utilities.LOGIN_REQUEST_2 +
                        sPassword +
                        Utilities.LOGIN_REQUEST_3, mediaType);
                Request request = new Request.Builder().url("http://"+ sURL+ "/zabbix/api_jsonrpc.php")
                        .method("POST",body).addHeader("Content-Type","application/json").build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        sMessage = e.toString();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(v,"La dirección IP ingresada no es correcta"
                                        ,Snackbar.LENGTH_LONG).show();
                                edtURL.setError("IP incorrecta");
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        sMessage = response.body().string();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    jsonResponse = new JSONObject(sMessage);
                                }catch (JSONException jsonException){
                                    Log.e("Parsing","Could not parse malformed JSON: \"" + sMessage + "\"");
                                }
                                if(jsonResponse.has("error")){
                                    JSONObject jsonError = jsonResponse.optJSONObject("error");
                                    int errorValue = jsonError.optInt("code");
                                    if(errorValue==-32602){
                                        Snackbar.make(v,"No existe usuario", Snackbar.LENGTH_LONG).show();
                                        edtUsername.setError("No existe usuario");
                                    }
                                    if(errorValue==-32500){
                                        Snackbar.make(v,"Contraseña incorrecta", Snackbar.LENGTH_LONG).show();
                                        edtPassword.setError("Contraseña incorrecta");
                                    }
                                }else{
                                    String sToken = jsonResponse.optString("result");
                                    SharedPreferences.Editor editor = userData.edit();
                                    editor.putString("Token", sToken);
                                    editor.putString("Username", sUsername);
                                    editor.putString("Password", sPassword);
                                    editor.putString("URL", sURL);
                                    editor.commit();
                                    startActivity(aMenu);
                                    finish();
                                }
                            }
                        });
                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed() {
        //Do nothing
    }
}