package com.pegp.arwithmapping;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.mapbox.mapboxsdk.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class Register extends AppCompatActivity {
    EditText etUsername,etFirstName,etMiddleName,etLastName,etMobileNumber,etEmailAddress,etHospitalName,etHospitalAddress,etHospitalMobileNumber,etPassword,etRepeatPassword;
    TextView tvPin;
    LinearLayout lnRegister,lnBack;

    Intent intent;
    Dialog  dialog;

    SharedPreferences sp;
    SharedPreferences.Editor editor;

    Boolean isRegularUser;
    Float isRegularAlpha;
    String latLong = "";
    Integer isRegularUserInt;
    String fcmKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFirstName = findViewById(R.id.etFirstName);
        etUsername = findViewById(R.id.etUsername);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etHospitalName = findViewById(R.id.etHospitalName);
        etHospitalAddress = findViewById(R.id.etHospitalAddress);
        etHospitalMobileNumber = findViewById(R.id.etHospitalMobileNumber);
        etPassword = findViewById(R.id.etPassword);
        etRepeatPassword = findViewById(R.id.etRepeatPassword);
        tvPin = findViewById(R.id.tvPin);
        lnRegister = findViewById(R.id.lnRegister);
        lnBack = findViewById(R.id.lnBack);

        intent  = getIntent();
        Bundle extras = intent.getExtras();

        sp = getSharedPreferences("key", Context.MODE_PRIVATE);
        editor = sp.edit();


        isRegularUser = extras.getInt("isRegularUser") == 1 ? true : false;
        isRegularUserInt = extras.getInt("isRegularUser");
        isRegularAlpha = isRegularUser ? 0.30f : 1.0f;
        fcmKey = extras.getString("fcmKey","--");

        Log.e("Key",fcmKey);

        editor.putString("currentLatLong", "");
        editor.commit();

        etHospitalName.setEnabled(!isRegularUser);
        etHospitalName.setAlpha(isRegularAlpha);
        etHospitalAddress.setEnabled(!isRegularUser);
        etHospitalAddress.setAlpha(isRegularAlpha);
        etHospitalMobileNumber.setEnabled(!isRegularUser);
        etHospitalMobileNumber.setAlpha(isRegularAlpha);
        tvPin.setEnabled(!isRegularUser);
        tvPin.setAlpha(isRegularAlpha);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress);
        dialog = builder.create();
        dialog.setCancelable(false);

        lnBack.setOnClickListener(
                view -> {
                    super.onBackPressed();
                }
        );

        lnRegister.setOnClickListener(view -> {
            String firstName = etFirstName.getText().toString();
            String middleName = etMiddleName.getText().toString();
            String lastName = etLastName.getText().toString();
            String mobileNumber = etMobileNumber.getText().toString();
            String username = etUsername.getText().toString();
            String emailAddress = etEmailAddress.getText().toString();
            String hospitalName = etHospitalName.getText().toString();
            String hospitalAddress = etHospitalAddress.getText().toString();
            String hospitalMobileNumber = etHospitalMobileNumber.getText().toString();
            String password = etPassword.getText().toString();
            String repeatPassword = etRepeatPassword.getText().toString();
            Boolean saveAll = false;

            latLong = sp.getString("currentLatLong","");

            if (firstName.equals("") || middleName.equals("") || lastName.equals("") || mobileNumber.equals("") || username.equals("") || emailAddress.equals("") || password.equals("") || repeatPassword.equals("")) {
                Toast.makeText(this, "All fields that are enabled must be filled up", Toast.LENGTH_LONG).show();
            } else if (password.length() < 8) {
                Toast.makeText(this, "Password is too short. Please provide at least 8 characters long", Toast.LENGTH_LONG).show();
            } else if (!password.equals(repeatPassword)) {
                Toast.makeText(this, "Password does not match", Toast.LENGTH_LONG).show();
            } else if (!validateEmail(emailAddress)) {
                Toast.makeText(this, "Please provide a proper Email Address", Toast.LENGTH_LONG).show();
            } else if (mobileNumber.length() < 11) {
                Toast.makeText(this, "Mobile Number must be 11 digit", Toast.LENGTH_LONG).show();
            } else {
                if (!isRegularUser) {
                    if (hospitalName.equals("") || hospitalAddress.equals("") || hospitalMobileNumber.equals("")) {
                        Toast.makeText(this, "All fields that are enabled must be filled up", Toast.LENGTH_LONG).show();
                    } else if (hospitalMobileNumber.length() < 11) {
                        Toast.makeText(this, "Hospital Mobile Number must be 11 digit", Toast.LENGTH_LONG).show();
                    } else if (latLong.equals("")) {
                        Toast.makeText(this, "Please provide your hospital location by pinning it", Toast.LENGTH_LONG).show();
                    } else {
                        saveAll = true;
                    }
                } else {
                    saveAll = true;
                }
            }

            if (saveAll) {
                saveAccount();
            }
        });

        tvPin.setOnClickListener(view -> {
            PermissionListener permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
                    Intent goToMapping = new Intent(Register.this, Mapping.class);
                    startActivity(goToMapping);
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(Register.this, "Some permissions were denied. Unable to use this function", Toast.LENGTH_LONG).show();
                }
            };

            new TedPermission(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("Storage is required.\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                    .check();
        });
    }

    public void saveAccount() {
        Links application = (Links) getApplication();
        String registrationAPI = application.registrationAPI;

        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, registrationAPI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            Boolean error = jsonResponse.getBoolean("error");
                            String message = jsonResponse.getString("message");

                            Toast.makeText(Register.this, message, Toast.LENGTH_LONG).show();
                            dialog.dismiss();

                            if (!error) {
                                lnBack.performClick();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(Register.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error.toString()

                        Toast.makeText(Register.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("isRegularUser", isRegularUserInt.toString());
                params.put("firstName", etFirstName.getText().toString());
                params.put("middleName", etMiddleName.getText().toString());
                params.put("lastName", etLastName.getText().toString());
                params.put("mobileNumber", etMobileNumber.getText().toString());
                params.put("username", etUsername.getText().toString());
                params.put("emailAddress", etEmailAddress.getText().toString());
                params.put("password", etPassword.getText().toString());
                params.put("hospitalName", etHospitalName.getText().toString());
                params.put("address", etHospitalAddress.getText().toString());
                params.put("hospitalMobileNumber", etHospitalMobileNumber.getText().toString());
                params.put("latLong", latLong);
                params.put("fcmKey",fcmKey);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private boolean validateEmail(String data) {
        Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher emailMatcher = emailPattern.matcher(data);
        return emailMatcher.matches();
    }
}