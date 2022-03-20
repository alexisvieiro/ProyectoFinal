package com.example.zabbixMobileApp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
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

import com.example.zabbixMobileApp.Adapters.ItemGroupSampleAdapter;
import com.example.zabbixMobileApp.Models.ItemGroupSampleItem;
import com.example.zabbixMobileApp.R;
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

public class ItemGroupFragment extends Fragment {
    private Toolbar toolbar;
    private String sItemGroupID;
    private RecyclerView rclItemGroupList;
    private EditText edtSearchItemGroup;
    private SwipeRefreshLayout swRefreshLayout;
    private Handler itemGroupHandler;
    private String sMessage;
    private JSONObject jsonResponse;
    private ItemGroupSampleAdapter itemGroupSampleAdapter;
    private ArrayList<ItemGroupSampleItem> sampleItemArrayList = new ArrayList<>();

    public ItemGroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sItemGroupID = getArguments().getString("ItemGroupID");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        rclItemGroupList = view.findViewById(R.id.listItemGroups);
        swRefreshLayout = view.findViewById(R.id.swipeItemGroupRefresh);
        edtSearchItemGroup = view.findViewById(R.id.textSearchItemGroup);

        toolbar = view.findViewById(R.id.toolbarItemGroupMenu);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sToken = userData.getString("Token",null);
        String sURL = userData.getString("URL", null);

        itemGroupSampleAdapter = new ItemGroupSampleAdapter(sampleItemArrayList,getActivity());

        rclItemGroupList.setHasFixedSize(true);
        rclItemGroupList.setLayoutManager(new LinearLayoutManager(getContext()));
        rclItemGroupList.addItemDecoration
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

    private void Filter (String text){
        ArrayList<ItemGroupSampleItem> filteredList = new ArrayList<>();
        for (ItemGroupSampleItem item : sampleItemArrayList){
            if (item.getsAppName().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }
        itemGroupSampleAdapter.filterList(filteredList);
    }

    void LoadData(String sToken, String sURL){
        itemGroupHandler = new Handler(Looper.getMainLooper());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n" +
                        "   \"jsonrpc\": \"2.0\",\n" +
                        "   \"method\": \"application.get\",\n" +
                        "   \"params\": {\n" +
                        "       \"output\": \"extend\",\n" +
                        "       \"hostids\": \""+ sItemGroupID +"\",\n" +
                        "       \"sortfield\": \"name\"\n" +
                        "  },\n" +
                        "  \"id\": 1,\n" +
                        "  \"auth\": \""+ sToken +"\"\n" +
                        "}"
                , mediaType);
        Request request = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                .method("POST", body).addHeader("Content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sMessage = e.toString();
                itemGroupHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(),sMessage,Snackbar.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                sMessage = response.body().string();
                itemGroupHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            jsonResponse = new JSONObject(sMessage);
                        }catch (JSONException jsonException){
                            Log.e("Parsing","Could not parse malformed JSON: \"" + sMessage + "\"");
                        }
                        if(jsonResponse.has("result")){
                            JSONArray jsonResult = jsonResponse.optJSONArray("result");
                            sampleItemArrayList.clear();
                            rclItemGroupList.setAdapter(itemGroupSampleAdapter);
                            for (int i=0;i<jsonResult.length();i++){
                                String sHostID = jsonResult.optJSONObject(i).optString("hostid");
                                String sAppName = jsonResult.optJSONObject(i).optString("name");
                                sampleItemArrayList.add(new ItemGroupSampleItem(sAppName,sHostID));
                            }
                            edtSearchItemGroup.addTextChangedListener(new TextWatcher() {
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