package com.pegp.arwithmapping.EmergencyList;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.pegp.arwithmapping.DriverDetails;
import com.pegp.arwithmapping.EmergencyListing;
import com.pegp.arwithmapping.PatientMap;
import com.pegp.arwithmapping.R;

import java.util.ArrayList;

public class emergencyListAdapter extends RecyclerView.Adapter<emergencyListAdapter.MyViewHolder>{
    private ArrayList<emergencyListData> emergencyListDataList;
    private int selectedPosition = 0;

    public emergencyListAdapter(ArrayList<emergencyListData> emergencyListData) {
        this.emergencyListDataList = emergencyListData;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_history, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        emergencyListData emergencyListData = emergencyListDataList.get(position);

        if(position == selectedPosition){
            holder.lnBorder.setBackgroundColor(Color.parseColor("#E91E63"));

//            Animation anim = new AlphaAnimation(0.0f, 1.0f);
//            anim.setDuration(50);
//            anim.setStartOffset(20);
//            anim.setRepeatMode(Animation.REVERSE);
//            anim.setRepeatCount(Animation.INFINITE);
//
//            holder.lnBorder.startAnimation(anim);
        }

        holder.tvName.setText(emergencyListData.getRequestor());
        holder.tvDetails.setText(emergencyListData.getDetails());
        holder.tvStatus.setText(emergencyListData.getStatus());

        holder.imgMore.setOnClickListener(view -> {
            openOptionMenu(view,holder.view.getContext(),emergencyListData.getId().toString(),emergencyListData.getStatus(),emergencyListData.originLatLong,emergencyListData.getMobileNumber());
        });
    }

    @Override
    public int getItemCount() {
        return emergencyListDataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView tvName,tvDetails,tvStatus;
        public final ImageView imgMore;
        public final LinearLayout lnBorder;

        public MyViewHolder(View view) {
            super(view);
            this.view = view;

            tvName = view.findViewById(R.id.tvName);
            tvDetails = view.findViewById(R.id.tvDetails);
            imgMore = view.findViewById(R.id.imgMore);
            tvStatus = view.findViewById(R.id.tvStatus);
            lnBorder = view.findViewById(R.id.lnBorder);
        }
    }

    public void openOptionMenu(View v, Context context, String id, String status,String latLong,String mobileNumber){
        PopupMenu popup = new PopupMenu(v.getContext(), v, Gravity.RIGHT);
        popup.getMenuInflater().inflate(R.menu.nav_emergency, popup.getMenu());

        if (!status.equals("Pending")) {
            popup.getMenu().findItem(R.id.navAccept).setVisible(false);
        }

        popup.setOnMenuItemClickListener(item -> {

            switch (item.getTitle().toString()) {
                case "Accept Request" :
                    if(context instanceof EmergencyListing){
                        ((EmergencyListing)context).AcceptRequest(id,latLong);
                    }
                    break;

                case "View Location" :

                    Intent patientMapping = new Intent(context, PatientMap.class);
                    patientMapping.putExtra("isDriveMode",0);
                    patientMapping.putExtra("isForViewing",1);
                    context.startActivity(patientMapping);

                    break;

                case "Call" :

                    PermissionListener permissionlistener = new PermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mobileNumber));
                            context.startActivity(intent);
                        }

                        @Override
                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                            Toast.makeText(context, "Some permissions were denied. Unable to use this function", Toast.LENGTH_LONG).show();
                        }
                    };

                    new TedPermission(context)
                            .setPermissionListener(permissionlistener)
                            .setDeniedMessage("Phone Call is required.\n\nPlease turn on permissions at [Setting] > [Permission]")
                            .setPermissions(Manifest.permission.CALL_PHONE)
                            .check();


                    break;
            }

            return true;
        });

        popup.show();
    }
}
