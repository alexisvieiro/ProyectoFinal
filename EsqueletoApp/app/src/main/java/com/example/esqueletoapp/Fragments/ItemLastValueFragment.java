package com.example.esqueletoapp.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.esqueletoapp.Adapters.ItemSampleAdapter;
import com.example.esqueletoapp.Models.ItemSampleItem;
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

public class ItemLastValueFragment extends Fragment {
    private String sHostID;
    private String sAppName;
    private RecyclerView rclLastValueList;
    private SwipeRefreshLayout swRefreshLayout;
    private EditText edtSearchLastValue;
    private Handler lastValueHandler;
    private String sMessage;
    private JSONObject jsonResponse;
    private ItemSampleAdapter itemSampleAdapter;
    private ArrayList<ItemSampleItem> sampleItemArrayList = new ArrayList<>();

    public ItemLastValueFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sHostID = getArguments().getString("HostID");
        sAppName = getArguments().getString("AppName");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_last_value, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        rclLastValueList = view.findViewById(R.id.lastValueList);
        swRefreshLayout = view.findViewById(R.id.swipeLastValueRefresh);
        edtSearchLastValue = view.findViewById(R.id.textSearchLastValue);

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sToken = userData.getString("Token",null);
        String sURL = userData.getString("URL", null);

        itemSampleAdapter = new ItemSampleAdapter(sampleItemArrayList,getActivity());

        rclLastValueList.setHasFixedSize(true);
        rclLastValueList.setLayoutManager(new LinearLayoutManager(getContext()));
        rclLastValueList.addItemDecoration
                (new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        LoadData(sToken,sURL);

        swRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                LoadData(sToken,sURL);
                swRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void Filter(String text){
        ArrayList<ItemSampleItem> filteredList = new ArrayList<>();
        for (ItemSampleItem item : sampleItemArrayList){
            if (item.getsItemName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }
        itemSampleAdapter.filterList(filteredList);
    }

    void LoadData (String sToken, String sURL){
        lastValueHandler = new Handler(Looper.getMainLooper());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n" +
                        "   \"jsonrpc\": \"2.0\",\n" +
                        "   \"method\": \"item.get\",\n" +
                        "   \"params\": {\n" +
                        "       \"output\": [\"itemid\",\"type\"," +
                        "       \"hostid\",\"name\",\"value_type\", \"templateid\"," +
                        "       \"description\",\"lastvalue\",\"lastclock\",\"units\"," +
                        "       \"lastns\",\"logtimefmt\"],\n" +
                        "       \"hostids\": \""+ sHostID +"\",\n" +
                        "       \"application\": \""+ sAppName +"\"\n" +
                        "   },\n" +
                        "  \"id\": 1,\n" +
                        "  \"auth\": \""+ sToken +"\"\n" +
                        "}\n"
                , mediaType);
        Request request = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                .method("POST", body).addHeader("Content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sMessage = e.toString();
                lastValueHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(),sMessage,Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                sMessage = response.body().string();
                lastValueHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jsonResponse = new JSONObject(sMessage);
                        }catch (JSONException jsonException){
                            Log.e("Parsing","Could not parse malformed JSON: \"" + sMessage + "\"");
                        }
                        if(jsonResponse.has("result")){
                            JSONArray jsonResult = jsonResponse.optJSONArray("result");
                            rclLastValueList.setAdapter(itemSampleAdapter);
                            sampleItemArrayList.clear();
                            if(jsonResult.length()==0){
                                AlertDialog.Builder alertItemsNotFound = new AlertDialog.Builder(getContext());
                                alertItemsNotFound.setMessage("No hay ítems asignados al application seleccionado");
                                alertItemsNotFound.setNeutralButton("Regresar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().getSupportFragmentManager().popBackStack();
                                    }
                                });
                                alertItemsNotFound.show();
                            }else{
                                for (int i=0; i<jsonResult.length();i++){
                                    String sItemName = jsonResult.optJSONObject(i).optString("name");
                                    String sLastValue = jsonResult.optJSONObject(i).optString
                                            ("lastvalue");
                                    String sItemUnits = jsonResult.optJSONObject(i).optString("units");
                                    String sLastCheck = jsonResult.optJSONObject(i).optString
                                            ("lastclock");
                                    String sDescription = jsonResult.optJSONObject(i).optString
                                            ("description");
                                    sampleItemArrayList.add(new ItemSampleItem
                                            (sItemName,sLastValue,sItemUnits,sLastCheck,sDescription));
                                }
                                edtSearchLastValue.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                                    }

                                    @Override
                                    public void afterTextChanged(Editable s) {
                                        Filter(s.toString());
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }
}