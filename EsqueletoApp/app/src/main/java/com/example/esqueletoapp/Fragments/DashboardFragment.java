package com.example.esqueletoapp.Fragments;

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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.esqueletoapp.Adapters.DashboardSampleAdapter;
import com.example.esqueletoapp.Models.DashboardSampleItem;
import com.example.esqueletoapp.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    private Toolbar toolbar;
    private FloatingActionButton fabAdd;
    private RecyclerView rclDashboardList;
    private DashboardSampleAdapter dashboardSampleAdapter;
    private ArrayList<DashboardSampleItem> sampleItemArrayList = new ArrayList<>();

    public DashboardFragment() {
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
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        fabAdd = view.findViewById(R.id.buttonAddToDashboard);
        rclDashboardList = view.findViewById(R.id.listDashboardItems);

        toolbar = view.findViewById(R.id.toolbarDashboardMenu);
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

        rclDashboardList.setHasFixedSize(true);
        rclDashboardList.setLayoutManager(new LinearLayoutManager(getContext()));
        rclDashboardList.addItemDecoration
                (new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        SharedPreferences userData = getActivity().getSharedPreferences("UserData", Context.MODE_PRIVATE);
        String sDashboardHostName = userData.getString("HostNames", null);
        String sDashboardItemName = userData.getString("ItemNames", null);

        dashboardSampleAdapter = new DashboardSampleAdapter(sampleItemArrayList, getActivity());
        rclDashboardList.setAdapter(dashboardSampleAdapter);

        if ((sDashboardHostName!=null) &&(sDashboardItemName!=null)){
            sampleItemArrayList.clear();
            String[] arrayItems = sDashboardItemName.split(",");
            String[] arrayHosts = sDashboardHostName.split(",");
            for (int i=0; i<arrayItems.length; i++){
                sampleItemArrayList.add(new DashboardSampleItem(arrayItems[i],arrayHosts[i]));
            }
        }

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewDashboardItemFragment newDashboardItemFragment = new NewDashboardItemFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.constraintMenu,newDashboardItemFragment)
                        .addToBackStack(null).commit();
            }
        });
    }
}