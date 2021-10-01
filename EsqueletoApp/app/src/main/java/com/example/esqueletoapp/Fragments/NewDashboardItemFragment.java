package com.example.esqueletoapp.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.esqueletoapp.R;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

public class NewDashboardItemFragment extends Fragment {
    private SearchableSpinner spinnerHost;
    private SearchableSpinner spinnerItem;
    private Button btnConfirm;

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

        spinnerHost = view.findViewById(R.id.spinnerGetHost);
        spinnerItem = view.findViewById(R.id.spinnerGetItem);
        btnConfirm = view.findViewById(R.id.buttonCommitNewItem);

        spinnerHost.setTitle("Seleccione Host");
        spinnerHost.setPositiveButton("OK");
        spinnerItem.setTitle("Seleccione Item");
        spinnerItem.setPositiveButton("OK");

        //https://stackoverflow.com/questions/32501119/load-data-from-sqlite-in-spinner

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*DashboardFragment dashboardFragment = new DashboardFragment();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.constraintMenu,dashboardFragment)
                        .addToBackStack(null).commit();*/
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }
}