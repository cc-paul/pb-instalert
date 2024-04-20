package com.pegp.arwithmapping;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;

public class DriverDetails extends AppCompatActivity {
    Intent intent;
    TextView tvDriver,tvHospital,tvAddress,tvMobileNumber;
    LinearLayout lnMapping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_details);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        tvDriver = findViewById(R.id.tvDriver);
        tvHospital = findViewById(R.id.tvHospital);
        tvAddress = findViewById(R.id.tvAddress);
        tvMobileNumber = findViewById(R.id.tvMobileNumber);
        lnMapping = findViewById(R.id.lnMapping);

        intent  = getIntent();
        Bundle extras = intent.getExtras();

        tvDriver.setText(extras.getString("fullName"));
        tvHospital.setText(extras.getString("hospitalName"));
        tvAddress.setText(extras.getString("address"));
        tvMobileNumber.setText(extras.getString("hospitalMobileNumber"));

        lnMapping.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        buildAlertMessageNoGps();
                    } else {
                        Intent patientMapping = new Intent(DriverDetails.this, PatientMap.class);
                        patientMapping.putExtra("isDriveMode",0);
                        patientMapping.putExtra("isForViewing",0);
                        startActivity(patientMapping);
                        finishAffinity();
                    }
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(DriverDetails.this, "Some permissions were denied. Unable to use this function", Toast.LENGTH_LONG).show();
                }
            };

            new TedPermission(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("Storage is required.\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();
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