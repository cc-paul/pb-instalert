package com.pegp.arwithmapping;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pegp.arwithmapping.EmergencyList.emergencyListAdapter;
import com.pegp.arwithmapping.EmergencyList.emergencyListData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EmergencyListing extends AppCompatActivity {
    LinearLayout lnBack,lnRefresh;
    RecyclerView rvHistory;
    TextView tvRecords;

    SharedPreferences sp;
    Dialog dialog;

    private RecyclerView.Adapter adapter;

    Integer id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_listing);

        sp = getSharedPreferences("key", Context.MODE_PRIVATE);
        id = sp.getInt("id",0);

        lnBack = findViewById(R.id.lnBack);
        lnRefresh = findViewById(R.id.lnRefresh);
        rvHistory = findViewById(R.id.rvHistory);
        tvRecords = findViewById(R.id.tvRecords);

        lnBack.setOnClickListener(view -> {
            super.onBackPressed();
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress);
        dialog = builder.create();
        dialog.setCancelable(false);

        lnRefresh.setOnClickListener(view -> {
            loadEmergencyList();
        });

        loadEmergencyList();
    }

    public void loadEmergencyList() {
        ArrayList<emergencyListData> list = new ArrayList<>();

        Links application = (Links) getApplication();
        String emergencyListApi = application.emergencyListApi;

        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, emergencyListApi,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Boolean error = obj.getBoolean("error");

                        Log.e("Response",response);

                        if (error){
                            Toast.makeText(EmergencyListing.this, "Unable to get data from server", Toast.LENGTH_LONG).show();
                        } else {
                            JSONArray arrHistory = obj.getJSONArray("result");
                            for (Integer i = 0; i < arrHistory.length(); i++) {
                                JSONObject current_obj = arrHistory.getJSONObject(i);
                                String details = "Mobile: " + current_obj.getString("mobileNumber") + " ● Requested: " + current_obj.getString("dateCreated") + " ● ";

                                list.add(new emergencyListData(
                                        current_obj.getInt("id"),
                                        current_obj.getString("originLatLong"),
                                        current_obj.getString("hospitalLatLong"),
                                        current_obj.getString("requestor"),
                                        "",
                                        details,
                                        current_obj.getString("status"),
                                        current_obj.getString("mobileNumber")
                                ));
                            }

                            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
                            rvHistory.setLayoutManager(mLayoutManager);

                            adapter = new emergencyListAdapter(list);
                            rvHistory.setAdapter(adapter);

                            tvRecords.setText("Total Records : " + list.size());
                            dialog.dismiss();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(EmergencyListing.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // error.toString()
                    dialog.dismiss();
                    Toast.makeText(EmergencyListing.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", "" + id);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void AcceptRequest (String rowID,String latLong) {
        ArrayList<emergencyListData> list = new ArrayList<>();

        Links application = (Links) getApplication();
        String acceptRequestApi = application.acceptRequestApi;

        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, acceptRequestApi,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        Boolean error = obj.getBoolean("error");
                        String message = obj.getString("message");

                        Log.e("Response",response);

                        Toast.makeText(this,message,Toast.LENGTH_LONG).show();

                        if (!error) {
                            loadEmergencyList();

                            Intent driverMapping = new Intent(EmergencyListing.this, DriverMapping.class);
                            driverMapping.putExtra("id",rowID);
                            driverMapping.putExtra("latLong",latLong);
                            startActivity(driverMapping);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        dialog.dismiss();
                        Toast.makeText(EmergencyListing.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    // error.toString()
                    dialog.dismiss();
                    Toast.makeText(EmergencyListing.this, "Something went wrong", Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("rowID", "" + rowID);
                params.put("respondedBy", "" + id);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}