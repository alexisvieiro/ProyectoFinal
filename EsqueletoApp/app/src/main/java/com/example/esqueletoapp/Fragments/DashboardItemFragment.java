package com.example.esqueletoapp.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private Handler mHandler;
    private String sMessage;
    private JSONObject jsonResponse;
    private GraphView graphView;
    private String[] sTime;
    private LineGraphSeries<DataPoint> series;

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

        graphView = view.findViewById(R.id.graphItem);

        graphView.setTitle(sItemName);
        graphView.setTitleTextSize(35);

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
                        "\",\n       \"sortfield\": \"clock\",\n       \"sortorder\": \"ASC\"" +
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
                            sTime = new String[jsonResult.length()];
                            Float[] fValue = new Float[jsonResult.length()];
                            DataPoint[] dataPoints= new DataPoint[jsonResult.length()];
                            for (int i=0;i<jsonResult.length();i++){
                                sTime[i] = jsonResult.optJSONObject(i).optString("clock");
                                fValue[i] = Float.valueOf(jsonResult.optJSONObject(i).optString("value"));
                                dataPoints[i]= new DataPoint(i,fValue[i]);
                            }series = new LineGraphSeries<>(dataPoints);
                            series.setDrawDataPoints(true);
                            graphView.addSeries(series);
                            sTime=DateParser(sTime);
                            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graphView);
                            staticLabelsFormatter.setHorizontalLabels(sTime);
                            graphView.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                            graphView.getGridLabelRenderer().setNumHorizontalLabels(2);
                            graphView.getGridLabelRenderer().setLabelHorizontalHeight(50);
                            graphView.getGridLabelRenderer().setHorizontalLabelsAngle(15);
                            graphView.getViewport().setScrollable(true);
                            graphView.getViewport().setScalable(true);

                            series.setOnDataPointTapListener(new OnDataPointTapListener() {
                                @Override
                                public void onTap(Series series, DataPointInterface dataPoint) {
                                    String msg = dataPoint.getY()+" at "+sTime[(int)dataPoint.getX()];
                                    Toast toast = Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT);
                                    toast.setGravity(Gravity.CENTER,0,0);
                                    toast.show();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    String[] DateParser(String[] str){
        for (int i=0;i<str.length;i++){
            long dl = Long.valueOf(str[i])*1000; //Debe estar en milisegundos
            Date dd = new java.util.Date(dl);
            str[i] = new SimpleDateFormat("dd MMM yyyy HH:mm z").format(dd).toUpperCase();
        }
        return str;
    }
}