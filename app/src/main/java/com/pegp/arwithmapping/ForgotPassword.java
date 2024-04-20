package com.pegp.arwithmapping;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ForgotPassword extends AppCompatActivity {
    LinearLayout lnBack,lnSave;
    EditText etCode,etNewPassword,etRepeatNewPassword;

    Intent intent;

    Dialog dialog;

    String passedEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        lnBack = findViewById(R.id.lnBack);
        lnSave = findViewById(R.id.lnSave);

        etCode = findViewById(R.id.etCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        etRepeatNewPassword = findViewById(R.id.etRepeatNewPassword);

        intent  = getIntent();
        Bundle extras = intent.getExtras();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress);
        dialog = builder.create();
        dialog.setCancelable(false);

        passedEmail = extras.getString("email","");

        lnSave.setOnClickListener(view -> {
            if (etCode.getText().toString().equals("") || etNewPassword.getText().toString().equals("") || etRepeatNewPassword.getText().toString().equals("")) {
                Toast.makeText(this,"Please fill in all required fields",Toast.LENGTH_LONG).show();
            } else if (!etNewPassword.getText().toString().equals(etRepeatNewPassword.getText().toString())) {
                Toast.makeText(this,"Password does not match",Toast.LENGTH_LONG).show();
            } else if (etNewPassword.getText().toString().length() < 8 || etRepeatNewPassword.getText().toString().length() < 8) {
                Toast.makeText(this,"Password must be at least 8 characters",Toast.LENGTH_LONG).show();
            } else {
                changePassword();
            }
        });

        lnBack.setOnClickListener(
                view -> {
                    super.onBackPressed();
                }
        );
    }

    private void changePassword() {
        Links application = (Links) getApplication();
        String changePassAPI = application.changePassAPI;

        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, changePassAPI,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");


                            Toast.makeText(ForgotPassword.this, message, Toast.LENGTH_LONG).show();

                            dialog.dismiss();

                            if (!error) {
                                lnBack.performClick();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(ForgotPassword.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error.toString()

                        Toast.makeText(ForgotPassword.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("otp", etCode.getText().toString());
                params.put("password", etNewPassword.getText().toString());
                params.put("email", passedEmail);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}