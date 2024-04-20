package com.pegp.arwithmapping;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.log.Logger;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.CircleManager;
import com.mapbox.mapboxsdk.plugins.annotation.CircleOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.ColorUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EmergencyMapping extends AppCompatActivity implements LocationListener, PermissionsListener {
    LinearLayout lnBack;
    TextView tvTimer,tvNote,tvLat,tvLong,tvHeaderText;

    private MapView mapView;
    private final Random random = new Random();
    private CircleManager circleManager;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    LocationManager locationManager;
    String token = "pk.eyJ1IjoicGFnZW50ZSIsImEiOiJjbDc2eW52NTIwcDBlM3hrYWh0MWx2dnM2In0.k5b0sazc7NwabRj1SLiNAA";
    String provider;
    String currentLatLong = "";
    String hospitalName;
    boolean isLocationAlreadySet = false;
    Integer isRegularUser,userID;
    Dialog dialog;
    Float radiusMeter;

    ProgressBar pb;
    CountDownTimer Count;
    Long timer;

    Double currentLat;
    Double currentLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, token);
        setContentView(R.layout.activity_emergency_mapping);

        sp = getSharedPreferences("key", Context.MODE_PRIVATE);
        editor = sp.edit();

        mapView = findViewById(R.id.mapView);
        lnBack = findViewById(R.id.lnBack);
        tvTimer = findViewById(R.id.tvTimer);
        tvNote = findViewById(R.id.tvNote);
        tvLat = findViewById(R.id.tvLat);
        tvLong = findViewById(R.id.tvLong);
        tvHeaderText = findViewById(R.id.tvHeaderText);

        tvHeaderText.setSelected(true);

        mapView.onCreate(savedInstanceState);

        lnBack.setOnClickListener(view -> {
            super.onBackPressed();
        });

        isRegularUser = sp.getInt("isRegularUser",0);
        userID = sp.getInt("id",0);
        radiusMeter = 2000f;

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(R.layout.progress);
        dialog = builder.create();
        dialog.setCancelable(false);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map = mapboxMap;


                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        circleManager = new CircleManager(mapView, mapboxMap, style);

                        if (style.isFullyLoaded()) {
                            enableLocationComponent(style);
                        }

                        try {
                            new Handler().postDelayed(() -> getCurrentLocation(), 2000);
                        } catch (Exception e) {

                        }

                        try {
                            new Handler().postDelayed(() -> {
                                Log.e("Response",isRegularUser.toString());

                                try {
                                    dialog.show();
                                } catch (WindowManager.BadTokenException e) {

                                }

                                if (isRegularUser == 1) {
                                    displayHospitals();
                                }
                            }, 5000);
                        } catch (Exception e) {

                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            if (map != null) {
                Style style = map.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(20)
                                    .build()), 4000);
                }
            }
        }
    }

    public void displayHospitals() {
        Links application = (Links) getApplication();
        String hospitalAPI = application.getHospitalsAPI;

        try {
            dialog.show();
        }
        catch (WindowManager.BadTokenException e) {
            //use a log message
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, hospitalAPI,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");

                            Toast.makeText(EmergencyMapping.this, message, Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                            Log.e("Response",response);

                            if (!error) {
                                JSONArray arrHospital = obj.getJSONArray("result");
                                for (Integer i = 0; i < arrHospital.length(); i++) {
                                    JSONObject current_obj = arrHospital.getJSONObject(i);

                                    String hospitalName = current_obj.getString("hospitalName");
                                    String[] latLong = current_obj.getString("latLong").split(",");

                                    map.addMarker(new MarkerOptions()
                                            .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.small_hospital))
                                            .position(new LatLng(Double.parseDouble(latLong[0]),Double.parseDouble(latLong[1]))));
                                }
                            }

                            startConfirmationTimer();
                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(EmergencyMapping.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error.toString()

                        Toast.makeText(EmergencyMapping.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", "" + userID);
                params.put("range", "" + radiusMeter);
                params.put("usersLatLong", "" + tvLat.getText().toString() + "," + tvLong.getText().toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void startConfirmationTimer() {
        Count = new CountDownTimer(60 * 1000, 1000) {
            public void onTick(long timer) {
                tvTimer.setText("" + timer / 1000);

                boolean isDivisibleBy10 = timer / 1000 % 10 == 0;

                if (isDivisibleBy10) {
                    checkRespondents();
                }
            }

            public void onFinish() {
                IncreaseRangeOrMaintain();
            }
        }.start();
    }

    public void checkRespondents() {
        Links application = (Links) getApplication();
        String checkRespondentApi = application.checkRespondentApi;

        StringRequest stringRequest = new StringRequest(Request.Method.POST, checkRespondentApi,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");

                            if (!message.equals("")) {
                                Toast.makeText(EmergencyMapping.this, message, Toast.LENGTH_LONG).show();
                            }

                            if (!error) {
                                JSONArray arrHospital = obj.getJSONArray("result");
                                for (Integer i = 0; i < arrHospital.length(); i++) {
                                    JSONObject current_obj = arrHospital.getJSONObject(i);

                                    String fullName = "Driver: " + current_obj.getString("fullName");
                                    String hospitalName = "Hospital: " + current_obj.getString("hospitalName");
                                    String hospitalMobileNumber = "Mobile Number: " + current_obj.getString("hospitalMobileNumber");
                                    String address = "Address: " + current_obj.getString("address");
                                    String originLatLong = "" + current_obj.getString("originLatLong");
                                    String hospitalLatLong = "" + current_obj.getString("hospitalLatLong");

                                    Intent goToDriverDetails = new Intent(EmergencyMapping.this, DriverDetails.class);
                                    goToDriverDetails.putExtra("fullName",fullName);
                                    goToDriverDetails.putExtra("hospitalName",hospitalName);
                                    goToDriverDetails.putExtra("address",address);
                                    goToDriverDetails.putExtra("hospitalMobileNumber",hospitalMobileNumber);
                                    Count.cancel();
                                    finishAffinity();
                                    startActivity(goToDriverDetails);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(EmergencyMapping.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error.toString()

                        Toast.makeText(EmergencyMapping.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("id", "" + userID);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    public void IncreaseRangeOrMaintain() {
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(EmergencyMapping.this);
            alert.setTitle("Notice");
            alert.setMessage("Sorry. No emergency assistance received. Do you want repeat or double your range?");
            alert.setCancelable(false);
            alert.setPositiveButton("Repeat", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    map.removeAnnotations();

                    displayHospitals();
                    drawCircle(map,new LatLng(currentLat, currentLong), Color.parseColor("#888888"),radiusMeter);
                    dialog.dismiss();
                }
            });

            alert.setNegativeButton("Double my Range", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    radiusMeter *= 2;
                    Log.e("Radius","" + radiusMeter);
                    map.removeAnnotations();
                    tvNote.setText("Waiting for someone to confirm your emergency within " + radiusMeter.toString().replace(".0","") + " meters");

                    displayHospitals();
                    drawCircle(map,new LatLng(currentLat, currentLong), Color.parseColor("#888888"),radiusMeter);
                    dialog.dismiss();
                }
            });

            alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    lnBack.performClick();
                }
            });

            alert.show();
        } catch (Exception e) {

        }
    }

    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        statusCheck();

        locationManager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        // Creating an empty criteria object
        Criteria criteria = new Criteria();

        // Getting the name of the provider that meets the criteria
        provider = locationManager.getBestProvider(criteria, false);

        if (provider != null && !provider.equals("")) {
            if (!provider.contains("gps")) { // if gps is disabled
                final Intent poke = new Intent();
                poke.setClassName("com.android.settings",
                        "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                sendBroadcast(poke);
            }
            // Get the location from the given provider
            Location location = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 500, 0, this);

            if (location != null)
                onLocationChanged(location);
            else
                location = locationManager.getLastKnownLocation(provider);
            if (location != null)
                onLocationChanged(location);
            else

                Toast.makeText(getBaseContext(), "Location can't be retrieved",
                        Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getBaseContext(), "No Provider Found",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(
                Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                "Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false).setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        startActivity(new Intent(
                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog,
                                        final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();

        try {
            alert.show();
        }
        catch (WindowManager.BadTokenException e) {
            //use a log message
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLat = location.getLatitude();
        currentLong = location.getLatitude();

        if (!isLocationAlreadySet) {

            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(12)
                            .build()), 4000);



            drawCircle(map,new LatLng(location.getLatitude(), location.getLongitude()), Color.parseColor("#888888"),radiusMeter);

            isLocationAlreadySet = true;
        }
    }

    public void drawCircle(MapboxMap currentMap, LatLng position, int color, float currentRadiusMeter) {
        Double thisLat = null,thisLong = null;
        
//        polylineOptions.color(color);
//        polylineOptions.width(1f); // change the line width here
//        polylineOptions.addAll(getCirclePoints(position, radiusMeters));
//        currentMap.addPolyline(polylineOptions);
//


        String[] latLong = position.toString().split(",");

        if (circleManager != null) {
            Log.e("Circle Response","There is a circle");
            circleManager.deleteAll();
        }


        if (tvLat.getText().toString().equals("0.0") || tvLong.getText().toString().equals("0.0")) {
            tvLat.setText(position.getLatitude() + "");
            tvLong.setText(position.getLongitude() + "");
        }


        //Log.e("Draw Response","Drawn at " + clat + "," + clong + " with radius of " + currentRadiusMeter);

//        circleOptions.withLatLng(new LatLng(Double.parseDouble(tvLat.getText().toString()),Double.parseDouble(tvLong.getText().toString())))
//                .withCircleColor(ColorUtils.colorToRgbaString(Color.GRAY))
//                .withCircleRadius(currentRadiusMeter)
//                .withCircleOpacity(0.30f)
//                .withDraggable(false);
//
//
//        circleManager.create(circleOptions);

//        polylineOptions.color(color);
//        polylineOptions.width(1f); // change the line width here
//        polylineOptions.addAll(getCirclePoints(new LatLng(Double.parseDouble(tvLat.getText().toString()),Double.parseDouble(tvLong.getText().toString())), currentRadiusMeter));
//        map.addPolyline(polylineOptions);

        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(color);
        polylineOptions.width(1f); // change the line width here
        polylineOptions.addAll(getCirclePoints(new LatLng(Double.parseDouble(tvLat.getText().toString()),Double.parseDouble(tvLong.getText().toString())), currentRadiusMeter));
        map.addPolyline(polylineOptions);


//        List<CircleOptions> circleOptionsList = new ArrayList<>();
//        for (int i = 0; i < 1; i++) {
//            circleOptionsList.add(new CircleOptions()
//                    .withLatLng(new LatLng(position.getLatitude(), position.getLongitude()))
//                    .withCircleColor(ColorUtils.colorToRgbaString(Color.GRAY))
//                    .withCircleRadius(currentRadiusMeter)
//                    .withCircleOpacity(0.30f)
//                    .withDraggable(false)
//            );
//            circleManager.create(circleOptionsList);
//        }

    }


    private static ArrayList<LatLng> getCirclePoints(LatLng position, double radius) {
        int degreesBetweenPoints = 1; // change here for shape
        int numberOfPoints = (int) Math.floor(360 / degreesBetweenPoints);
        double distRadians = radius / 6371000.0; // earth radius in meters
        double centerLatRadians = position.getLatitude() * Math.PI / 180;
        double centerLonRadians = position.getLongitude() * Math.PI / 180;
        ArrayList<LatLng> polygons = new ArrayList<>(); // array to hold all the points
        for (int index = 0; index < numberOfPoints; index++) {
            double degrees = index * degreesBetweenPoints;
            double degreeRadians = degrees * Math.PI / 180;
            double pointLatRadians = Math.asin(sin(centerLatRadians) * cos(distRadians)
                    + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians));
            double pointLonRadians = centerLonRadians + Math.atan2(sin(degreeRadians)
                            * sin(distRadians) * cos(centerLatRadians),
                    cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians));
            double pointLat = pointLatRadians * 180 / Math.PI;
            double pointLon = pointLonRadians * 180 / Math.PI;
            LatLng point = new LatLng(pointLat, pointLon);
            polygons.add(point);
        }
        // add first point at end to close circle
        polygons.add(polygons.get(0));
        return polygons;
    }

    @Override
    public void onLocationChanged(@NonNull List<Location> locations) {
        LocationListener.super.onLocationChanged(locations);
    }

    @Override
    public void onFlushComplete(int requestCode) {
        LocationListener.super.onFlushComplete(requestCode);
    }

    @SuppressWarnings({"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            LocationComponent locationComponent = map.getLocationComponent();
            locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, loadedMapStyle).build());
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

        try {
            Count.cancel();
        } catch (Exception e) {

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}