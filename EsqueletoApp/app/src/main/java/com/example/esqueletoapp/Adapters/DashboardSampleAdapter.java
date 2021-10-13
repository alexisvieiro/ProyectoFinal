package com.example.esqueletoapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esqueletoapp.Activities.MenuActivity;
import com.example.esqueletoapp.Fragments.DashboardItemFragment;
import com.example.esqueletoapp.Models.DashboardSampleItem;
import com.example.esqueletoapp.R;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class DashboardSampleAdapter extends RecyclerView.Adapter<DashboardSampleAdapter.SampleHolder>{

    private ArrayList<DashboardSampleItem> sampleDataList;
    private Context c;

    public DashboardSampleAdapter(ArrayList<DashboardSampleItem> sampleDataList, Context c){
        this.sampleDataList = sampleDataList;
        this.c = c;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View MainItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_item_layout, parent, false);
        return new SampleHolder(MainItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        final DashboardSampleItem dashboardSampleItemData;
        dashboardSampleItemData = sampleDataList.get(position);
        holder.setItemName(dashboardSampleItemData.getsItemName());
        holder.setHostName(dashboardSampleItemData.getsHostname());

        holder.getBtnDelete().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDelete = new AlertDialog.Builder(c);
                alertDelete.setTitle("Borrar ítem seleccionado");
                alertDelete.setMessage("¿Desea borrar el ítem?");
                alertDelete.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences userData = c.getSharedPreferences("UserData", Context.MODE_PRIVATE);
                        String sDashboardHostName = userData.getString("HostNames", null);
                        String sDashboardItemName = userData.getString("ItemNames", null);
                        String sDashboardItemID = userData.getString("ItemIDs", null);

                        sDashboardItemName = DeleteSection(sDashboardItemName,holder.getAdapterPosition(),
                                getItemCount()-1);
                        sDashboardHostName = DeleteSection(sDashboardHostName,holder.getAdapterPosition(),
                                getItemCount()-1);
                        sDashboardItemID = DeleteSection(sDashboardItemID,holder.getAdapterPosition(),
                                getItemCount()-1);

                        SharedPreferences.Editor editor = userData.edit();
                        editor.putString("HostNames", sDashboardHostName);
                        editor.putString("ItemNames", sDashboardItemName);
                        editor.putString("ItemIDs", sDashboardItemID);
                        editor.commit();

                        sampleDataList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        notifyItemRangeChanged(holder.getAdapterPosition(),
                                getItemCount());
                    }
                });
                alertDelete.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });alertDelete.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DashboardItemFragment dashboardItemFragment = new DashboardItemFragment();
                Bundle bundle = new Bundle();
                bundle.putString("ItemName", dashboardSampleItemData.getsItemName());
                bundle.putInt("Position", holder.getAdapterPosition());
                dashboardItemFragment.setArguments(bundle);
                ((MenuActivity)c).getSupportFragmentManager().beginTransaction().
                        replace(R.id.constraintMenu,dashboardItemFragment).
                        addToBackStack(null).commit();
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
        private TextView txtHostName;
        private ImageButton btnDelete;

        public SampleHolder (View itemView){
            super(itemView);
            this.view = itemView;
        }

        public void setItemName (String sItemName){
            txtItemName = view.findViewById(R.id.textItemName);
            txtItemName.setText(sItemName);
        }

        public void setHostName (String sHostName){
            txtHostName = view.findViewById(R.id.textHostName);
            txtHostName.setText(sHostName);
        }

        public ImageButton getBtnDelete() {
            btnDelete = view.findViewById(R.id.buttonDeleteDashboardItem);
            return btnDelete;
        }
    }

    String DeleteSection (String str, Integer pos, Integer lastpos){
        Integer index = str.indexOf(",");
        Integer lastindex = str.lastIndexOf(",");
        Integer end = str.length();
        if (index>-1){
            if (pos==0){
                str = str.substring(index+1,end);
            }else if(pos==lastpos){
                str = str.substring(0,lastindex);
            }else{
                for (int i=index, c=1;i>=0;i=str.indexOf(",",i+1),c++){
                    if (c==pos){
                        str = str.substring(0,i+1)+""+str.substring(str.indexOf(",",i+1)+1,end);
                        break;
                    }
                }
            }
        }else{
            str=null;
        }
        return str;
    }
}
