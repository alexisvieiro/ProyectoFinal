package com.example.esqueletoapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.example.esqueletoapp.Adapters.DeviceSampleAdapter;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NewDashboardItemFragment extends Fragment {
    private SearchableSpinner spinnerHost;
    private SearchableSpinner spinnerItem;
    private Button btnConfirm;
    private Handler hostHandler;
    private Handler itemHandler;
    private String sMessageHost;
    private String sMessageItem;
    private JSONObject jsonResponseHost;
    private JSONObject jsonResponseItem;
    private String[] sHostList;
    private String[] sHostIDs;
    private String[] sItemList;
    private String[] sItemIDList;
    private String[] sItemUnitList;
    private String[] sItemValueType;

    public NewDashboardItemFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_dashboard_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        spinnerHost = view.findViewById(R.id.spinnerGetHost);
        spinnerItem = view.findViewById(R.id.spinnerGetItem);
        btnConfirm = view.findViewById(R.id.buttonCommitNewItem);

        spinnerHost.setTitle("Seleccione Host");
        spinnerHost.setPositiveButton("OK");
        spinnerItem.setTitle("Seleccione Item");
        spinnerItem.setPositiveButton("OK");

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sToken = userData.getString("Token",null);
        String sURL = userData.getString("URL", null);
        hostHandler = new Handler(Looper.getMainLooper());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n   \"jsonrpc\": \"2.0\",\n   \"method\": \"host.get\"," +
                        "\n   \"params\": {\n       \"output\": [\n           \"hostid\",\n           \"host\"\n" +
                        "       ]\n   },\n   \"id\": 1,\n   \"auth\": \"" +
                        sToken + "\"\n}\n"
                , mediaType);
        Request request = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                .method("POST", body).addHeader("Content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sMessageHost = e.toString();
                hostHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(),sMessageHost,Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                sMessageHost = response.body().string();
                hostHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jsonResponseHost = new JSONObject(sMessageHost);
                        }catch (JSONException jsonException){
                            Log.e("Parsing","Could not parse malformed JSON: \"" + sMessageHost + "\"");
                        }
                        if(jsonResponseHost.has("result")){
                            JSONArray jsonResult = jsonResponseHost.optJSONArray("result");
                            sHostList = new String[jsonResult.length()+1];
                            sHostIDs = new String[jsonResult.length()];
                            sHostList[0]="Host no seleccionado";
                            for (int i=0; i<jsonResult.length(); i++){
                                sHostList[i+1] = jsonResult.optJSONObject(i).optString("host");
                                sHostIDs[i]= jsonResult.optJSONObject(i).optString("hostid");
                            }
                            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                                    (getContext(), android.R.layout.simple_spinner_dropdown_item, sHostList);
                            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerHost.setAdapter(dataAdapter);
                        }
                    }
                });
            }
        });

        spinnerHost.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    itemHandler = new Handler(Looper.getMainLooper());
                    OkHttpClient clientItem = new OkHttpClient().newBuilder().build();
                    MediaType mediaTypeItem = MediaType.parse("application/json");
                    RequestBody bodyItem = RequestBody.create("{\n   \"jsonrpc\": \"2.0\",\n   \"method\":" +
                                    " \"item.get\",\n   \"params\": {\n       \"output\": [\"name\",\"value_type\",\"units\"],\n       " +
                                    "\"hostids\": \"" +
                                    sHostIDs[position-1] +
                                    "\",\n       \"filter\": {\n           \"value_type\": [\"0\",\"3\"]\n       },\n       " +
                                    "\"sortfield\": \"name\",\n       \"sortorder\": \"DESC\"\n   },\n   \"id\": 1,\n   " +
                                    "\"auth\": \"" +
                                    sToken+"\"\n}\n"
                            , mediaTypeItem);
                    Request requestItem = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                            .method("POST", bodyItem).addHeader("Content-Type","application/json")
                            .build();
                    clientItem.newCall(requestItem).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            sMessageItem = e.toString();
                            itemHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Snackbar.make(getView(),sMessageItem,Snackbar.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            sMessageItem = response.body().string();
                            itemHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        jsonResponseItem = new JSONObject(sMessageItem);
                                    }catch (JSONException jsonException){
                                        Log.e("Parsing","Could not parse malformed JSON: \"" + sMessageItem + "\"");
                                    }
                                    if(jsonResponseItem.has("result")){
                                        JSONArray jsonResultItem = jsonResponseItem.optJSONArray("result");
                                        sItemList = new String[jsonResultItem.length()];
                                        sItemIDList = new String[jsonResultItem.length()];
                                        sItemUnitList = new String[jsonResultItem.length()];
                                        sItemValueType = new String[jsonResultItem.length()];
                                        for (int i=0; i<jsonResultItem.length(); i++){
                                            if(jsonResultItem.optJSONObject(i).has("name")){
                                                sItemList[i] = jsonResultItem.optJSONObject(i).optString("name");
                                                sItemIDList[i] = jsonResultItem.optJSONObject(i).optString("itemid");
                                                sItemUnitList[i] = jsonResultItem.optJSONObject(i).optString("units");
                                                sItemValueType[i] = jsonResultItem.optJSONObject(i).optString("value_type");
                                            }
                                        }
                                        ArrayAdapter<String> dataAdapter =  new ArrayAdapter<String>
                                                (getContext(), android.R.layout.simple_spinner_dropdown_item, sItemList);
                                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinnerItem.setAdapter(dataAdapter);
                                    }
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MutatingSharedPrefs")
            @Override
            public void onClick(View v) {
                if(spinnerHost.getSelectedItemPosition()!=0){
                    SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
                    String sDashboardHostName = userData.getString("HostNames", null);
                    String sDashboardItemName = userData.getString("ItemNames", null);
                    String sDashboardItemID = userData.getString("ItemIDs", null);
                    String sDashboardItemUnit = userData.getString("ItemUnits", null);
                    String sDashboardItemValueType = userData.getString("ValueType", null);

                    Integer pos = spinnerItem.getSelectedItemPosition();

                    if (sItemUnitList[pos].equals("")){
                        sItemUnitList[pos]="No units";
                    }

                    if ((sDashboardItemName==null)&&(sDashboardHostName==null)&&
                            (sDashboardItemID==null)&&(sDashboardItemUnit==null)&&
                            (sDashboardItemValueType==null)){
                        sDashboardItemName = spinnerItem.getSelectedItem().toString();
                        sDashboardHostName = spinnerHost.getSelectedItem().toString();
                        sDashboardItemID = sItemIDList[pos];
                        sDashboardItemUnit = sItemUnitList[pos];
                        sDashboardItemValueType = sItemValueType[pos];
                    }else{
                        sDashboardItemName = sDashboardItemName+","+spinnerItem.getSelectedItem().toString();
                        sDashboardHostName = sDashboardHostName+","+spinnerHost.getSelectedItem().toString();
                        sDashboardItemID = sDashboardItemID+","+sItemIDList[pos];
                        sDashboardItemUnit = sDashboardItemUnit+","+sItemUnitList[pos];
                        sDashboardItemValueType = sDashboardItemValueType+","+sItemValueType[pos];
                    }

                    sDashboardItemName = TrimString(sDashboardItemName);

                    SharedPreferences.Editor editor = userData.edit();
                    editor.putString("HostNames", sDashboardHostName);
                    editor.putString("ItemNames", sDashboardItemName);
                    editor.putString("ItemIDs", sDashboardItemID);
                    editor.putString("ItemUnits", sDashboardItemUnit);
                    editor.putString("ValueType", sDashboardItemValueType);
                    editor.commit();

                    Snackbar.make(getView(),"Nuevo ítem agregado al tablero",Snackbar.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStack();
                }else{
                    Snackbar.make(getView(),"No ha seleccionado host",Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    String TrimString(String str){
        str=str.replaceAll("[(]","\\(");
        str=str.replaceAll("[)]","\\)");
        str=str.replaceAll("[.]","\\.");
        str=str.replaceAll("[\\\\]","");
        str=str.replaceAll("\\^","");
        str=str.replaceAll("\\?","");
        str=str.replaceAll("\\$","");
        str=str.replaceAll("\\|","");
        str=str.replaceAll("\\*","");
        str=str.replaceAll("\\+","");
        str=str.replaceAll("\\[","");
        str=str.replaceAll("\\{","");
        str=str.replaceAll(", "," ");
        return str;
    }
}