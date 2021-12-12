package com.example.esqueletoapp.Adapters;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esqueletoapp.Activities.MenuActivity;
import com.example.esqueletoapp.Fragments.ItemGroupFragment;
import com.example.esqueletoapp.Models.HostSampleItem;
import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class HostSampleAdapter extends RecyclerView.Adapter<HostSampleAdapter.SampleHolder>{
    private ArrayList<HostSampleItem> sampleDataList;
    private Context c;

    public HostSampleAdapter(ArrayList<HostSampleItem> sampleDataList, Context c){
        this.sampleDataList = sampleDataList;
        this.c = c;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View MainItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.host_item_layout, parent, false);
        return new SampleHolder(MainItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        final HostSampleItem hostSampleItemData;
        hostSampleItemData = sampleDataList.get(position);
        String sHostID = hostSampleItemData.getsHostID();
        holder.setHostName(hostSampleItemData.getsHostName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemGroupFragment itemGroupFragment = new ItemGroupFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ItemGroupID", sHostID);
                itemGroupFragment.setArguments(bundle);
                ((MenuActivity)c).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.constraintMenu, itemGroupFragment)
                        .addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleDataList.size();
    }

    public void filterList(ArrayList<HostSampleItem> filteredList){
        sampleDataList = filteredList;
        notifyDataSetChanged();
    }

    public class SampleHolder extends RecyclerView.ViewHolder{
        private View view;
        private TextView txtHostName;

        public SampleHolder(View itemView){
            super(itemView);
            this.view = itemView;
        }

        public void setHostName(String sHostName){
            txtHostName = view.findViewById(R.id.textHostMenuName);
            txtHostName.setSelected(true);
            txtHostName.setText(sHostName);
        }
    }
}
