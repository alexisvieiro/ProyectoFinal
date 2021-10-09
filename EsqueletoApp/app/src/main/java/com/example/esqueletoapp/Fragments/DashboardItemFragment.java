package com.example.esqueletoapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.anychart.AnyChart;
import com.anychart.anychart.AnyChartView;
import com.anychart.anychart.Cartesian;
import com.anychart.anychart.DataEntry;
import com.anychart.anychart.TooltipPositionMode;
import com.anychart.anychart.ValueDataEntry;
import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class DashboardItemFragment extends Fragment {
    private String sItemName;
    private Integer iPosition;
    private AnyChartView chartDashboard;
    private Handler mHandler;
    private String sMessage;
    private JSONObject jsonResponse;
    private List<DataEntry> seriesData = new ArrayList<>();

    public DashboardItemFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sItemName = getArguments().getString("ItemName");
        iPosition = getArguments().getInt("Position");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chartDashboard = view.findViewById(R.id.anyChartDashboard);

        Cartesian cartesian = AnyChart.line();
        cartesian.setAnimation(true);
        cartesian.setPadding(10d,20d,5d,20d);
        cartesian.setCrosshair(true);
        cartesian.setYAxis(true);
        cartesian.getTooltip().setPositionMode(TooltipPositionMode.POINT);
        cartesian.setTitle(sItemName);

        //https://www.oodlestechnologies.com/blogs/drawing-graphs-in-android-using-achartengine-charting-library-(line-charts)/

        cartesian.getYAxis().getLabels().setFormat("{%Value}{numDecimals:2}");

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sToken = userData.getString("Token",null);
        String sURL = userData.getString("URL", null);
        String sDashboardItemID = userData.getString("ItemIDs", null);
        String[] arrayIDs = sDashboardItemID.split(",");
        String sItemID = arrayIDs[iPosition];

        mHandler = new Handler(Looper.getMainLooper());
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\n   \"jsonrpc\": \"2.0\",\n   " +
                        "\"method\": \"history.get\",\n   \"params\": {\n       \"output\": " +
                        "\"extend\",\n       \"history\": 0,\n       \"itemids\": \"" +
                        sItemID +
                        "\",\n       \"sortfield\": \"clock\",\n       \"sortorder\": \"DESC\"" +
                        ",\n       \"limit\": 10\n   },\n   \"id\": 1,\n   \"auth\": \"" +
                        sToken +
                        "\"\n}\n"
                , mediaType);
        Request request = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                .method("POST", body).addHeader("Content-Type","application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                sMessage = e.toString();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Snackbar.make(getView(),sMessage,Snackbar.LENGTH_LONG).show();
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
                        }if(jsonResponse.has("result")){
                            JSONArray jsonResult = jsonResponse.optJSONArray("result");
                            for (int i=0;i<jsonResult.length();i++){
                                String sValue = jsonResult.optJSONObject(i).optString("value");
                                String sTime = jsonResult.optJSONObject(i).optString("clock");
                                Integer iValue = Math.round(Float.valueOf(sValue));
                                seriesData.add(new ValueDataEntry(sTime,iValue));
                                cartesian.setData(seriesData);
                                chartDashboard.setChart(cartesian);
                            }
                        }
                    }
                });
            }
        });
    }
}