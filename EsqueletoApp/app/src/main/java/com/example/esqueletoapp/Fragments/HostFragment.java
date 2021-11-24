package com.example.esqueletoapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.example.esqueletoapp.Adapters.DeviceSampleAdapter;
import com.example.esqueletoapp.Adapters.HostSampleAdapter;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.Models.HostSampleItem;
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

public class HostFragment extends Fragment {
    private RecyclerView rclHostList;
    private SwipeRefreshLayout swRefreshLayout;
    private Handler hostHandler;
    private String sMessage;
    private JSONObject jsonResponse;
    private HostSampleAdapter hostSampleAdapter;
    private ArrayList<HostSampleItem> sampleItemArrayList = new ArrayList<>();
    private EditText edtSearchHost;

    public HostFragment() {
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
        return inflater.inflate(R.layout.fragment_host, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        rclHostList = view.findViewById(R.id.listHosts);
        swRefreshLayout = view.findViewById(R.id.swipeHostRefresh);
        edtSearchHost = view.findViewById(R.id.textSearchHost);

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sToken = userData.getString("Token",null);
        String sURL = userData.getString("URL", null);

        hostSampleAdapter = new HostSampleAdapter(sampleItemArrayList, getActivity());

        rclHostList.setHasFixedSize(true);
        rclHostList.setLayoutManager(new LinearLayoutManager(getContext()));
        rclHostList.addItemDecoration
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
        ArrayList<HostSampleItem> filteredList = new ArrayList<>();
        for (HostSampleItem item : sampleItemArrayList){
            if (item.getsHostName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }
        hostSampleAdapter.filterList(filteredList);
    }

    void LoadData(String sToken, String sURL){
        hostHandler = new Handler(Looper.getMainLooper());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n   \"jsonrpc\": \"2.0\",\n   \"method\": \"host.get\"," +
                        "\n   \"params\": {\n       \"output\": [\n           \"hostid\",\n           \"host\"\n" +
                        "       ], \"sortfield\": \"host\",\"sortorder\": \"ASC\"" +
                        "\n   },\n   \"id\": 1,\n   \"auth\": \"" +
                        sToken + "\"\n}\n"
                , mediaType);
        Request request = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                .method("POST", body).addHeader("Content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sMessage = e.toString();
                hostHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(),sMessage,Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                sMessage = response.body().string();
                hostHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jsonResponse = new JSONObject(sMessage);
                        }catch (JSONException jsonException){
                            Log.e("Parsing","Could not parse malformed JSON: \"" + sMessage + "\"");
                        }
                        if(jsonResponse.has("result")){
                            JSONArray jsonResult = jsonResponse.optJSONArray("result");
                            rclHostList.setAdapter(hostSampleAdapter);
                            sampleItemArrayList.clear();
                            for (int i=0; i<jsonResult.length(); i++){
                                String sHost = jsonResult.optJSONObject(i).optString("host");
                                String sHostID = jsonResult.optJSONObject(i).optString("hostid");
                                sampleItemArrayList.add(new HostSampleItem(sHost,sHostID));
                            }
                            edtSearchHost.addTextChangedListener(new TextWatcher() {
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
                });
            }
        });
    }
}