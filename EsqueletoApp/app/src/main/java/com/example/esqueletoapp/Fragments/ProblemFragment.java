package com.example.esqueletoapp.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.esqueletoapp.Adapters.ProblemSampleAdapter;
import com.example.esqueletoapp.Models.ProblemSampleItem;
import com.example.esqueletoapp.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.snackbar.Snackbar;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProblemFragment extends Fragment {
    private Toolbar toolbar;
    private SearchableSpinner spinnerHost;
    private RecyclerView rclProblemList;
    private TextView txtNotClassified, txtInfo, txtWarning,
            txtAverage, txtHigh, txtDisaster;
    private ProblemSampleAdapter problemSampleAdapter;
    private ArrayList<ProblemSampleItem> sampleItemArrayList = new ArrayList<>();
    private Handler hostHandler;
    private Handler problemHandler;
    private String sMessageHost;
    private String sMessageProblem;
    private JSONObject jsonResponseHost;
    private JSONObject jsonResponseProblem;
    private String[] sHostList;
    private String[] sHostIDs;

    public ProblemFragment() {
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
        return inflater.inflate(R.layout.fragment_problem, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        txtNotClassified = view.findViewById(R.id.textProblemNotClass);
        txtInfo = view.findViewById(R.id.textProblemInfo);
        txtWarning = view.findViewById(R.id.textProblemWarning);
        txtAverage = view.findViewById(R.id.textProblemAverage);
        txtHigh = view.findViewById(R.id.textProblemHigh);
        txtDisaster = view.findViewById(R.id.textProblemDisaster);

        toolbar = view.findViewById(R.id.toolbarProblemMenu);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
                getActivity().findViewById(R.id.cardDashboard).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.cardHosts).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.cardProblems).setVisibility(View.VISIBLE);
            }
        });

        rclProblemList = view.findViewById(R.id.problemList);
        problemSampleAdapter = new ProblemSampleAdapter(sampleItemArrayList,getActivity());
        rclProblemList.setHasFixedSize(true);
        rclProblemList.setLayoutManager(new LinearLayoutManager(getContext()));
        rclProblemList.addItemDecoration
                (new DividerItemDecoration(getContext(),DividerItemDecoration.VERTICAL));


        spinnerHost = view.findViewById(R.id.spinnerGetProblemHost);
        spinnerHost.setTitle("Seleccione Host");
        spinnerHost.setPositiveButton("OK");

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
                            sHostList[0]="Todos los hosts";
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
                Calendar cTimeAgo = Calendar.getInstance();
                cTimeAgo.add(Calendar.DAY_OF_YEAR,-30);
                long pastTime = cTimeAgo.getTimeInMillis()/1000;
                String sHostIDQuery;
                if(position==0){
                    sHostIDQuery="";
                }else{
                    sHostIDQuery= "          \"hostids\": [\""+sHostIDs[position-1]+"\"],\n";
                }
                problemHandler = new Handler(Looper.getMainLooper());
                OkHttpClient clientProblem = new OkHttpClient().newBuilder().build();
                MediaType mediaTypeProblem = MediaType.parse("application/json");
                RequestBody bodyProblem = RequestBody.create("{\n" +
                                "  \"jsonrpc\": \"2.0\",\n" +
                                "  \"method\": \"problem.get\",\n" +
                                "  \"params\": {\n" +
                                      sHostIDQuery+
                                "       \"time_from\": \""+pastTime+"\",\n" +
                                "       \"sortfield\": [\"eventid\"],\n"+
                                "       \"sortorder\": \"DESC\", \n"+
                                "       \"limit\": 50\n" +
                                "  },\n" +
                                "  \"id\": 1,\n" +
                                "  \"auth\": \""+sToken+"\"\n" +
                                "}\n"
                        , mediaTypeProblem);
                Request requestProblem = new Request.Builder().url("http://"+sURL+"/zabbix/api_jsonrpc.php")
                        .method("POST", bodyProblem).addHeader("Content-Type","application/json")
                        .build();
                clientProblem.newCall(requestProblem).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        sMessageProblem = e.toString();
                        problemHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Snackbar.make(getView(),sMessageProblem,Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        sMessageProblem = response.body().string();
                        problemHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    jsonResponseProblem = new JSONObject(sMessageProblem);
                                }catch (JSONException jsonException){
                                    Log.e("Parsing","Could not parse malformed JSON: \"" + sMessageProblem + "\"");
                                }
                                if(jsonResponseProblem.has("result")){
                                    JSONArray jsonResultProblem = jsonResponseProblem.optJSONArray("result");
                                    rclProblemList.setAdapter(problemSampleAdapter);
                                    sampleItemArrayList.clear();
                                    if (jsonResultProblem.length()==0){
                                        Snackbar.make(getView(),
                                                "No hay datos para mostrar", Snackbar.LENGTH_LONG).show();
                                        Integer[] sSeverityClear = {0,0,0,0,0,0};
                                        SetSeverity(sSeverityClear);
                                    }else{
                                        Integer[] sSeverityCounter = {0,0,0,0,0,0};
                                        for (int i=0;i<jsonResultProblem.length();i++){
                                            String sClock = jsonResultProblem.optJSONObject(i).
                                                    optString("clock");
                                            String sProblemName = jsonResultProblem.optJSONObject(i).
                                                    optString("name");
                                            String sIsAck = jsonResultProblem.optJSONObject(i).
                                                    optString("acknowledged");
                                            String sSeverity = jsonResultProblem.optJSONObject(i).
                                                    optString("severity");
                                            sSeverityCounter[Integer.parseInt(sSeverity)]++;
                                            sampleItemArrayList.add(new ProblemSampleItem
                                                    (sClock,sProblemName,sIsAck,sSeverity));
                                        }
                                        SetSeverity(sSeverityCounter);
                                    }
                                }
                            }
                        });
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void SetSeverity(Integer[] sSeverityCounter){
        txtNotClassified.setText(String.valueOf(sSeverityCounter[0]));
        txtInfo.setText(String.valueOf(sSeverityCounter[1]));
        txtWarning.setText(String.valueOf(sSeverityCounter[2]));
        txtAverage.setText(String.valueOf(sSeverityCounter[3]));
        txtHigh.setText(String.valueOf(sSeverityCounter[4]));
        txtDisaster.setText(String.valueOf(sSeverityCounter[5]));

        txtNotClassified.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar.make(v,"Eventos sin clasificar: "
                        +txtNotClassified.getText(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
        txtInfo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar.make(v,"Eventos informativos: "
                        +txtInfo.getText(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
        txtWarning.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar.make(v,"Eventos de alerta: "
                        +txtWarning.getText(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
        txtAverage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar.make(v,"Eventos de riesgo promedio: "
                        +txtAverage.getText(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
        txtHigh.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar.make(v,"Eventos de alto riesgo: "
                        +txtHigh.getText(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
        txtDisaster.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Snackbar.make(v,"Eventos desastrosos: "
                        +txtDisaster.getText(),Snackbar.LENGTH_LONG).show();
                return true;
            }
        });
    }
}