package com.example.esqueletoapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.anychart.anychart.AnyChart;
import com.anychart.anychart.AnyChartView;
import com.anychart.anychart.Cartesian;
import com.anychart.anychart.DataEntry;
import com.anychart.anychart.Pie;
import com.anychart.anychart.Set;
import com.anychart.anychart.TooltipPositionMode;
import com.anychart.anychart.ValueDataEntry;
import com.example.esqueletoapp.Adapters.DeviceSampleAdapter;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceFragment extends Fragment {
    private AnyChartView chartDevice;
    private RecyclerView rclHostList;
    private SwipeRefreshLayout swRefreshLayout;
    private Handler hostHandler;
    private String sMessage;
    private JSONObject jsonResponse;
    private DeviceSampleAdapter deviceSampleAdapter;
    private ArrayList<DeviceSampleItem> sampleItemArrayList = new ArrayList<>();


    public DeviceFragment() {
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
        return inflater.inflate(R.layout.fragment_device, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rclHostList = view.findViewById(R.id.listDevices1);
        swRefreshLayout = view.findViewById(R.id.swipeRefreshLayout1);

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sToken = userData.getString("Token",null);

        rclHostList.setHasFixedSize(true);
        rclHostList.setLayoutManager(new LinearLayoutManager(getContext()));
        rclHostList.addItemDecoration
                (new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));

        hostHandler = new Handler(Looper.getMainLooper());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n   \"jsonrpc\": \"2.0\",\n   \"method\": \"host.get\"," +
                        "\n   \"params\": {\n       \"output\": [\n           \"hostid\",\n           \"host\"\n" +
                        "       ],\n       \"selectInterfaces\": [\n           \"interfaceid\",\n           \"ip\"\n" +
                        "       ]\n   },\n   \"id\": 1,\n   \"auth\": \"" +
                        sToken + "\"\n}\n"
                , mediaType);
        Request request = new Request.Builder().url("http://52.186.171.45/zabbix/api_jsonrpc.php")
                .method("POST", body).addHeader("Content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sMessage = e.toString();
                hostHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),sMessage,Toast.LENGTH_LONG);
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
                            deviceSampleAdapter = new DeviceSampleAdapter(sampleItemArrayList, getActivity());
                            rclHostList.setAdapter(deviceSampleAdapter);
                            for (int i=0; i<jsonResult.length(); i++){
                                String sHost = jsonResult.optJSONObject(i).optString("host");
                                sampleItemArrayList.add(new DeviceSampleItem(sHost));
                            }
                        }
                    }
                });

                swRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        swRefreshLayout.setRefreshing(false);
                        DeviceFragment deviceFragment = new DeviceFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .detach(deviceFragment).attach(deviceFragment).commit();
                    }
                });
            }
        });




        /*chartDevice = view.findViewById(R.id.myAnyChart);

        Cartesian cartesian = AnyChart.line();
        cartesian.setAnimation(true);
        cartesian.setPadding(10d,20d,5d,20d);
        cartesian.setCrosshair(true);
        cartesian.setYAxis(true);
        cartesian.getTooltip().setPositionMode(TooltipPositionMode.POINT);
        cartesian.setTitle("Trend of Sales of the Most Popular Products of ACME Corp.");
        cartesian.getYAxis().setTitle("Number of Bottles Sold (thousands)");
        cartesian.getXAxis().getLabels().setPadding(5d,5d,5d,5d);

        List<DataEntry> seriesData = new ArrayList<>();
        seriesData.add(new ValueDataEntry("1986",12));
        seriesData.add(new ValueDataEntry("1987",15));
        seriesData.add(new ValueDataEntry("1988",4));
        seriesData.add(new ValueDataEntry("1989",28));

        cartesian.setData(seriesData);

        chartDevice.setChart(cartesian);*/
    }
}