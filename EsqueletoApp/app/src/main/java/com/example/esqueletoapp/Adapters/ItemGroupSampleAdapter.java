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
import com.example.esqueletoapp.Fragments.ItemLastValueFragment;
import com.example.esqueletoapp.Models.HostSampleItem;
import com.example.esqueletoapp.Models.ItemGroupSampleItem;
import com.example.esqueletoapp.R;

import java.util.ArrayList;

public class ItemGroupSampleAdapter extends
        RecyclerView.Adapter<ItemGroupSampleAdapter.SampleHolder> {
    private ArrayList<ItemGroupSampleItem> sampleDataList;
    private Context c;

    public ItemGroupSampleAdapter(ArrayList<ItemGroupSampleItem> sampleDataList, Context c){
        this.sampleDataList = sampleDataList;
        this.c = c;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View MainItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_item_layout, parent,false);
        return new SampleHolder(MainItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        final ItemGroupSampleItem itemGroupSampleItemData;
        itemGroupSampleItemData = sampleDataList.get(position);
        String sHostID = itemGroupSampleItemData.getsHostID();
        String sAppName = itemGroupSampleItemData.getsAppName();
        holder.setItemGroupName(sAppName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemLastValueFragment itemLastValueFragment = new ItemLastValueFragment();
                Bundle bundle = new Bundle();
                bundle.putString("HostID", sHostID);
                bundle.putString("AppName", sAppName);
                itemLastValueFragment.setArguments(bundle);
                ((MenuActivity)c).getSupportFragmentManager().beginTransaction()
                        .replace(R.id.constraintMenu,itemLastValueFragment)
                        .addToBackStack(null).commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleDataList.size();
    }

    public void filterList(ArrayList<ItemGroupSampleItem> filteredList){
        sampleDataList = filteredList;
        notifyDataSetChanged();
    }

    public class SampleHolder extends RecyclerView.ViewHolder{
        private View view;
        private TextView txtItemGroupName;

        public SampleHolder(View itemView){
            super(itemView);
            this.view = itemView;
        }

        public void setItemGroupName(String sItemGroupName){
            txtItemGroupName = view.findViewById(R.id.textItemGroupMenuName);
            txtItemGroupName.setSelected(true);
            txtItemGroupName.setText(sItemGroupName);
        }
    }
}
