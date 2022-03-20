package com.example.zabbixMobileApp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zabbixMobileApp.Models.ProblemSampleItem;
import com.example.zabbixMobileApp.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ProblemSampleAdapter extends RecyclerView.Adapter<ProblemSampleAdapter.SampleHolder> {
    private ArrayList<ProblemSampleItem> sampleDataList;
    private Context c;

    public ProblemSampleAdapter(ArrayList<ProblemSampleItem> sampleDataList, Context c){
        this.sampleDataList = sampleDataList;
        this.c = c;
    }

    @NonNull
    @Override
    public SampleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View MainItemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.problem_item_layout, parent,false);
        return new SampleHolder(MainItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SampleHolder holder, int position) {
        final ProblemSampleItem problemSampleItemData;
        problemSampleItemData = sampleDataList.get(position);
        String sProblemName = problemSampleItemData.getsProblemName();
        String sSeverity = problemSampleItemData.getsSeverity();
        String sIsAck = problemSampleItemData.getsIsAck();

        String sClock = problemSampleItemData.getsClock();
        long lDate = Long.parseLong(sClock)*1000;
        Date date = new java.util.Date(lDate);
        String sDate = new SimpleDateFormat("dd MMM yyyy HH:mm z").format(date).toUpperCase();

        if (sIsAck.equals("0")){
            sIsAck = "Problema no atendido";
        }else{
            sIsAck = "Problema atendido";
        }

        String[] sSeverityType = {"No clasificada","Información","Alerta",
                "Media","Alta","Desastrosa"};

        holder.setProblemName(sProblemName);
        holder.setImage(sSeverity);

        String finalsIsAck = sIsAck;
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dataDialog = new AlertDialog.Builder(v.getContext());
                dataDialog.setTitle(sProblemName);
                dataDialog.setMessage("Severidad: "+sSeverityType[Integer.parseInt(sSeverity)]+"\n"+
                                "Tiempo de detección: "+sDate+"\n"+ "Estado: "+ finalsIsAck);
                dataDialog.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dataDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return sampleDataList.size();
    }

    public class SampleHolder extends RecyclerView.ViewHolder{
        private View view;
        private TextView txtProblemName;
        private ImageView imgSeverity;

        public SampleHolder(View itemView){
            super(itemView);
            this.view = itemView;
        }

        public void setProblemName(String sProblemName){
            txtProblemName = view.findViewById(R.id.textProblemName);
            txtProblemName.setSelected(true);
            txtProblemName.setText(sProblemName);
        }

        public void setImage(String sSeverity){
            imgSeverity = view.findViewById(R.id.imageSeverity);
            Drawable imageDrawable = imgSeverity.getBackground();
            imageDrawable = DrawableCompat.wrap(imageDrawable);
            switch (sSeverity){
                case "0":
                    imgSeverity.setImageResource(R.drawable.not_classified_icon);
                    DrawableCompat.setTint(imageDrawable,Color.rgb(0x97,0xAA,0xB3));
                    break;
                case "1":
                    imgSeverity.setImageResource(R.drawable.information_icon);
                    DrawableCompat.setTint(imageDrawable,Color.rgb(0x74,0x99,0xFF));
                    break;
                case "2":
                    imgSeverity.setImageResource(R.drawable.warning_icon);
                    DrawableCompat.setTint(imageDrawable,Color.rgb(0xFF,0xC8,0x59));
                    break;
                case "3":
                    imgSeverity.setImageResource(R.drawable.warning_icon);
                    DrawableCompat.setTint(imageDrawable,Color.rgb(0xFF,0xA0,0x59));
                    break;
                case "4":
                    imgSeverity.setImageResource(R.drawable.warning_icon);
                    DrawableCompat.setTint(imageDrawable,Color.rgb(0xE9,0x76,0x59));
                    break;
                case "5":
                    imgSeverity.setImageResource(R.drawable.warning_icon);
                    DrawableCompat.setTint(imageDrawable,Color.rgb(0xE4,0x59,0x59));
                    break;
            }
            imgSeverity.setBackground(imageDrawable);
            imgSeverity.setColorFilter(Color.rgb(255,255,255));
        }
    }
}
