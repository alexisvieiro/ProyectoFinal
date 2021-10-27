package com.example.esqueletoapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esqueletoapp.Models.HostSampleItem;
import com.example.esqueletoapp.R;

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
        holder.setHostName(hostSampleItemData.getsHostName());
    }

    @Override
    public int getItemCount() {
        return sampleDataList.size();
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
            txtHostName.setText(sHostName);
        }
    }
}
