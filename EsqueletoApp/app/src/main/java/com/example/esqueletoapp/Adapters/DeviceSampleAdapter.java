package com.example.esqueletoapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esqueletoapp.Activities.MenuActivity;
import com.example.esqueletoapp.Fragments.DashboardFragment;
import com.example.esqueletoapp.Fragments.DeviceFragment;
import com.example.esqueletoapp.Fragments.HostFragment;
import com.example.esqueletoapp.Models.DeviceSampleItem;
import com.example.esqueletoapp.R;

import java.util.ArrayList;

public class DeviceSampleAdapter extends RecyclerView.Adapter<DeviceSampleAdapter.SampleHolder> {
    private ArrayList<DeviceSampleItem> sampleDataList;
    private Context c;

    public DeviceSampleAdapter(ArrayList<DeviceSampleItem> sampleDataList, Context c){
        this.sampleDataList = sampleDataList;
        this.c = c;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View MainItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_layout, parent, false);
        return new SampleHolder(MainItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        final DeviceSampleItem deviceSampleItemData;
        deviceSampleItemData =sampleDataList.get(position);
        holder.setDeviceName(deviceSampleItemData.getsDeviceName());

        holder.getBtnSelect().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppCompatActivity activity = (AppCompatActivity)v.getContext();
                if(deviceSampleItemData.getsDeviceName().equals("Hosts")){
                    HostFragment hostFragment = new HostFragment();
                    activity.getSupportFragmentManager().beginTransaction().
                            replace(R.id.constraintMenu,hostFragment).
                            addToBackStack(null).commit();
                }if(deviceSampleItemData.getsDeviceName().equals("Tablero")){
                    DashboardFragment dashboardFragment = new DashboardFragment();
                    activity.getSupportFragmentManager().beginTransaction().
                            replace(R.id.constraintMenu,dashboardFragment).
                            addToBackStack(null).commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleDataList.size();
    }

    public class SampleHolder extends RecyclerView.ViewHolder{
        private View view;
        private TextView txtDeviceName;
        private ImageButton btnSelect;

        public SampleHolder(View itemView){
            super(itemView);
            this.view = itemView;
        }

        public void setDeviceName (String sDeviceName){
            txtDeviceName = view.findViewById(R.id.textDeviceName);
            txtDeviceName.setText(sDeviceName);
        }

        public ImageButton getBtnSelect(){
            btnSelect = view.findViewById(R.id.buttonSelect);
            return btnSelect;
        }
    }
}
