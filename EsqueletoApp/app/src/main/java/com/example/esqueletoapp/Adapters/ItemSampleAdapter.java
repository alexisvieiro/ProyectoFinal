package com.example.esqueletoapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esqueletoapp.Models.ItemSampleItem;
import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ItemSampleAdapter extends RecyclerView.Adapter<ItemSampleAdapter.SampleHolder>{
    private ArrayList<ItemSampleItem> sampleDataList;
    private Context c;

    public ItemSampleAdapter(ArrayList<ItemSampleItem> sampleDataList, Context c){
        this.sampleDataList = sampleDataList;
        this.c = c;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View MainItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_last_value_layout, parent,false);
        return new SampleHolder(MainItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        final ItemSampleItem itemSampleItemData;
        itemSampleItemData = sampleDataList.get(position);
        String sItemName = itemSampleItemData.getsItemName();
        String sLastValue = itemSampleItemData.getsLastValue();
        String sItemUnits = itemSampleItemData.getsItemUnits();
        String sLastCheck = itemSampleItemData.getsLastCheck();
        String sDescription = itemSampleItemData.getsDescription();

        long lDate = Long.valueOf(sLastCheck)*1000;
        Date dd = new java.util.Date(lDate);
        String sDate = new SimpleDateFormat("dd MMM yyyy HH:mm z").format(dd).toUpperCase();

        holder.setItemName(sItemName);
        holder.setLastValue(sLastValue+" "+sItemUnits);
        holder.setLastCheck(sDate);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(v,sDescription,Snackbar.LENGTH_INDEFINITE).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleDataList.size();
    }

    public class SampleHolder extends RecyclerView.ViewHolder{
        private View view;
        private TextView txtItemName;
        private TextView txtLastValue;
        private TextView txtLastCheck;

        public SampleHolder(View itemView){
            super(itemView);
            this.view = itemView;
        }

        public void setItemName(String sItemName){
            txtItemName = view.findViewById(R.id.textLastValueItemName);
            txtItemName.setSelected(true);
            txtItemName.setText(sItemName);
        }

        public void setLastValue(String sLastValue){
            txtLastValue = view.findViewById(R.id.textLastValue);
            txtLastValue.setText(sLastValue);
        }

        public void setLastCheck(String sLastCheck){
            txtLastCheck = view.findViewById(R.id.textLastCheck);
            txtLastCheck.setText(sLastCheck);
        }
    }
}