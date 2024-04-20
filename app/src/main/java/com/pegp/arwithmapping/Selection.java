package com.pegp.arwithmapping;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class Selection extends AppCompatActivity {
    LinearLayout lnBack,lnEmergency,lnFirstAid;
    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        lnBack = findViewById(R.id.lnBack);
        lnEmergency = findViewById(R.id.lnEmergency);
        lnFirstAid = findViewById(R.id.lnFirstAid);

        sp = getSharedPreferences("key", Context.MODE_PRIVATE);

        lnBack.setOnClickListener(
                view -> {
                    super.onBackPressed();
                }
        );

        lnEmergency.setOnClickListener(
                view -> {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                    builder1.setMessage("Are you sure you want to proceed?.");
                    builder1.setCancelable(true);

                    builder1.setPositiveButton(
                            "Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();

                                    PermissionListener permissionlistener = new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted() {
                                            if (sp.getInt("isRegularUser",0) == 1) {
                                                if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                                                    buildAlertMessageNoGps();
                                                } else {
                                                    Intent emergencyMapping = new Intent(Selection.this, EmergencyMapping.class);
                                                    startActivity(emergencyMapping);
                                                }
                                            } else {
                                                Intent emergencyListing = new Intent(Selection.this, EmergencyListing.class);
                                                startActivity(emergencyListing);
                                            }
                                        }

                                        @Override
                                        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                                            Toast.makeText(Selection.this, "Some permissions were denied. Unable to use this function", Toast.LENGTH_LONG).show();
                                        }
                                    };

                                    new TedPermission(Selection.this)
                                            .setPermissionListener(permissionlistener)
                                            .setDeniedMessage("Storage is required.\n\nPlease turn on permissions at [Setting] > [Permission]")
                                            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                                            .check();
                                }
                            });

                    builder1.setNegativeButton(
                            "No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog alert11 = builder1.create();
                    alert11.show();
                }
        );

        lnFirstAid.setOnClickListener(view -> {
            Intent goto1stAid = new Intent(Selection.this, MainActivity.class);
            startActivity(goto1stAid);
        });
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}