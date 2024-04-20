package com.pegp.arwithmapping;

import static com.mapbox.core.constants.Constants.PRECISION_6;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.pegp.arwithmapping.Routing.LiveRouting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class PatientMap  extends AppCompatActivity implements LocationListener, PermissionsListener {
    private MapView mapView;
    private MapboxMap map;
    private PermissionsManager permissionsManager;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    LocationManager locationManager;
    String token = "pk.eyJ1IjoicGFnZW50ZSIsImEiOiJjbDc2eW52NTIwcDBlM3hrYWh0MWx2dnM2In0.k5b0sazc7NwabRj1SLiNAA";
    String provider;
    boolean isLocationAlreadySet = false;
    boolean isDrivingStarted = false;

    Point patientLocation,oldPatientLocation,ambulanceLocation,hospitalLocation;
    Integer userID,isDriveMode,routeID,isForViewing;
    FloatingActionButton fbRoadOverview,fbMyLocation;
    LinearLayout lnBack;

    private MapboxDirections client;
    private static final String TAG = "Mapping";
    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private Marker ambulanceMarker;
    private DirectionsRoute currentRoute;
    FirebaseFirestore db;

    SymbolManager symbolManager;

    Dialog dialog;
    Intent intent;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    LiveRouting liveRouting;
    Style mapboxStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(PatientMap.this, token);
        setContentView(R.layout.activity_patient_map);

        intent  = getIntent();
        Bundle extras = intent.getExtras();
        db = FirebaseFirestore.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("LiveRouting");
        liveRouting = new LiveRouting();

        try {
            isDriveMode = extras.getInt("isDriveMode",0);
        } catch (Exception e) {
            isDriveMode = 1;
        }

        try {
            isForViewing = extras.getInt("isForViewing",0);
        } catch (Exception e) {
            isForViewing = 0;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress);
        dialog = builder.create();
        dialog.setCancelable(false);

        sp = getSharedPreferences("key", Context.MODE_PRIVATE);
        editor = sp.edit();

        userID = sp.getInt("id",0);

        mapView = findViewById(R.id.mapView);
        fbRoadOverview = findViewById(R.id.fbRoadOverview);
        fbMyLocation = findViewById(R.id.fbMyLocation);
        lnBack = findViewById(R.id.lnBack);

        if (isDriveMode == 1) {
            fbMyLocation.setVisibility(View.VISIBLE);
        }

        mapView.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("WrongConstant")
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                map = mapboxMap;

                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        mapboxStyle = style;
                        map.removeAnnotations();
                        getOriginAndDestination();
                    }
                });
            }
        });

        fbRoadOverview.setOnClickListener(view -> {
            if (isDriveMode == 1) {

                try {
                    map.removeMarker(ambulanceMarker);
                } catch (Exception e) {

                }

                ambulanceMarker = map.addMarker(new MarkerOptions()
                        .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.ambulance__1_))
                        .position(new LatLng(ambulanceLocation.latitude(),ambulanceLocation.longitude())));

                getRoute();
                showBoundary();
            } else {
                showBoundary();
            }
        });

        fbMyLocation.setOnClickListener(view -> {
            isDrivingStarted = true;

            ambulanceMarker = map.addMarker(new MarkerOptions()
                    .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.ambulance__1_))
                    .position(new LatLng(ambulanceLocation.latitude(),ambulanceLocation.longitude())));

            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(ambulanceLocation.latitude(), ambulanceLocation.longitude(), 10))
                            .zoom(15)
                            .tilt(60)
                            .build()), 4000);

            //enableLocationComponent(mapboxStyle);

            getRoute();
        });

        lnBack.setOnClickListener(view -> {
            mapView.onDestroy();
            super.onBackPressed();
        });

        final Handler handler = new Handler();
        final int delay = 2000;

        handler.postDelayed(new Runnable() {
            public void run() {

                if (isDriveMode == 0 && isForViewing == 0) {
//                    databaseReference = FirebaseDatabase.getInstance().getReference("LiveRouting");
//                    databaseReference.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                            if (dataSnapshot.exists()) {
//                                for (DataSnapshot ds : dataSnapshot.getChildren()) {
//
//
//                                    String key = ds.getKey();
//                                    Log.e("Current KEY",key);
//                                    Log.e("Current Value",ds.getValue(String.class).);
//
//                                    if (key.equals("LiveRouting-" + routeID)) {
//                                        Log.e("LatLong",ds.toString());
//                                        for (DataSnapshot data : ds.getChildren()) {
//
//                                            String[] latLong = data.child("latLong").getValue(String.class).split(",");
//
//                                            try {
//                                                map.removeMarker(ambulanceMarker);
//                                            } catch (Exception e) {
//
//                                            }
//
//                                            ambulanceMarker = map.addMarker(new MarkerOptions()
//                                                    .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.ambulance))
//                                                    .position(new LatLng(Double.parseDouble(latLong[1]), Double.parseDouble(latLong[0]))));
//                                        }
//                                    }
//
//
//                                }
//                            }
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//                            throw databaseError.toException();
//                        }
//                    });

                    DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("LiveRouting").child("LiveRouting-" + routeID);
                    dbRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String[] latLong = dataSnapshot.child("latLong").getValue(String.class).split(",");

//                            try {
//                                map.removeMarker(ambulanceMarker);
//                            } catch (Exception e) {
//
//                            }
//
//                            ambulanceMarker = map.addMarker(new MarkerOptions()
//                                    .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.ambulance__1_))
//                                    .position(new LatLng(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]))));



                            if (ambulanceMarker == null) {
                                ambulanceMarker = map.addMarker(new MarkerOptions()
                                    .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.ambulance__1_))
                                    .position(new LatLng(Double.parseDouble(latLong[0]), Double.parseDouble(latLong[1]))));
                            } else {
                                ambulanceMarker.setPosition(new LatLng(Double.parseDouble(latLong[0]),Double.parseDouble(latLong[1])));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    public void saveToFirebase(String routeID,String latLong,boolean isSave) {
        liveRouting.setRouteID(routeID);
        liveRouting.setLatLong(latLong);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("Realtime Database","Succeed");

                if (isSave) {
                    databaseReference.child("LiveRouting-" + routeID).setValue(liveRouting);
                } else {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("LiveRouting").child("LiveRouting-" + routeID);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("latLong", latLong);
                    ref.updateChildren(updates);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Realtime Database","Failed");
            }
        });
    }

    public void showBoundary() {
        LatLngBounds.Builder b = new LatLngBounds.Builder();

        if (isDriveMode == 1) {
            hospitalLocation = ambulanceLocation;
        }

        b.include(new LatLng(patientLocation.latitude(), patientLocation.longitude()));
        b.include(new LatLng(hospitalLocation.latitude(), hospitalLocation.longitude()));
        LatLngBounds bounds = b.build();

        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

        CameraPosition camPos = new CameraPosition.Builder(map.getCameraPosition())
                .target(bounds.getCenter())
                .bearing(0).tilt(0).build();

        map.animateCamera(CameraUpdateFactory
                .newCameraPosition(camPos));

        new CountDownTimer(500, 500) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200));

                CameraPosition camPos = new CameraPosition.Builder(map.getCameraPosition())
                        .target(bounds.getCenter())
                        .bearing(0).tilt(0).zoom(14).build();

                map.animateCamera(CameraUpdateFactory
                        .newCameraPosition(camPos));
            }
        }.start();
    }

    private void getRoute() {
        if (isDriveMode == 1) {
            client = MapboxDirections.builder()
                    .origin(ambulanceLocation)
                    .destination(patientLocation)
                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .accessToken(token)
                    .build();
        } else {
            client = MapboxDirections.builder()
                    .origin(patientLocation)
                    .destination(hospitalLocation)
                    .overview(DirectionsCriteria.OVERVIEW_FULL)
                    .profile(DirectionsCriteria.PROFILE_DRIVING)
                    .accessToken(token)
                    .build();
        }

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, retrofit2.Response<DirectionsResponse> response) {
                Log.e(TAG, "Response code: " + response.code());
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.e(TAG, "No routes found");
                    return;
                }

                currentRoute = response.body().routes().get(0);
                Log.d(TAG, "Distance: " + currentRoute.distance());

                drawRoute(currentRoute);

                if (map != null) {
                    map.getStyle(style -> {
                        GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

                        if (source != null) {
                            Log.e(TAG, "onResponse: source != null");
                            source.setGeoJson(FeatureCollection.fromFeature(
                                    Feature.fromGeometry(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6))));
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Log.e(TAG, "Error: " + throwable.getMessage());
                Toast.makeText(PatientMap.this, "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        LineString lineString = LineString.fromPolyline(route.geometry(), PRECISION_6);
        List<Point> coordinates = lineString.coordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        Log.e("Points", "" + points);

        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).latitude(),
                    coordinates.get(i).longitude());
        }

        map.addPolyline(new PolylineOptions()
                .add(points)
                .color(Color.parseColor("#009688"))
                .width(5));
    }

    public void getOriginAndDestination() {
        Links application = (Links) getApplication();
        String emergencyLatLongAPI = application.emergencyLatLongAPI;

        dialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, emergencyLatLongAPI,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            Boolean error = obj.getBoolean("error");
                            String message = obj.getString("message");

                            if (error) {
                                Toast.makeText(PatientMap.this, message, Toast.LENGTH_LONG).show();
                            }

                            dialog.dismiss();
                            Log.e("Response",response);

                            if (!error) {
                                JSONArray arrRoadEmergency = obj.getJSONArray("result");
                                for (Integer i = 0; i < arrRoadEmergency.length(); i++) {
                                    JSONObject current_obj = arrRoadEmergency.getJSONObject(i);

                                    routeID = current_obj.getInt("id");
                                    String[] patient = current_obj.getString("originLatLong").split(",");
                                    String[] ambulance = current_obj.getString("hospitalLatLong").split(",");

                                    patientLocation = Point.fromLngLat(Double.parseDouble(patient[1]), Double.parseDouble(patient[0]));
                                    hospitalLocation = Point.fromLngLat(Double.parseDouble(ambulance[1]), Double.parseDouble(ambulance[0]));

                                    if (isDriveMode.equals(1)) {
                                        map.addMarker(new MarkerOptions()
                                                .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.sos))
                                                .position(new LatLng(patientLocation.latitude(),patientLocation.longitude())));

                                        map.animateCamera(CameraUpdateFactory.newCameraPosition(
                                                new CameraPosition.Builder()
                                                        .target(new LatLng(patientLocation.latitude(), patientLocation.longitude(), 10))
                                                        .zoom(15)
                                                        .build()), 4000);

                                        saveToFirebase(routeID + "",hospitalLocation.latitude() + "," + hospitalLocation.latitude(),true);
                                    } else {
                                        map.addMarker(new MarkerOptions()
                                                .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.sos))
                                                .position(new LatLng(patientLocation.latitude(),patientLocation.longitude())));

                                        map.addMarker(new MarkerOptions()
                                                .icon(IconFactory.getInstance(getApplicationContext()).fromResource(R.drawable.small_hospital))
                                                .position(new LatLng(hospitalLocation.latitude(),hospitalLocation.longitude())));

                                        getRoute();
                                        showBoundary();
                                    }

                                    getCurrentLocation();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            Toast.makeText(PatientMap.this, "Something went wrong", Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error.toString()

                        Toast.makeText(PatientMap.this, "Something went wrong", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("userID", userID.toString());
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
        } else {
//            map.animateCamera(CameraUpdateFactory.newCameraPosition(
//                    new CameraPosition.Builder()
//                            .target(new LatLng(16.93756479332367, 121.6610002019549))
//                            .zoom(9)
//                            .build()), 4000);
        }
    }

    private void buildAlertMessageNoGps() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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
        final android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onLocationChanged(Location location) {
//        if (!isLocationAlreadySet) {
//            map.animateCamera(CameraUpdateFactory.newCameraPosition(
//                    new CameraPosition.Builder()
//                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
//                            .zoom(9)
//                            .build()), 4000);
//
//            origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
//            origin2 = origin;
//
//            isLocationAlreadySet = true;
//        }
//
//        origin2 = Point.fromLngLat(location.getLongitude(), location.getLatitude());
//
//        if (isDrivingStarted) {
////            Toast.makeText(this,"1",Toast.LENGTH_SHORT).show();
//
//            map.animateCamera(CameraUpdateFactory.newCameraPosition(
//                    new CameraPosition.Builder()
//                            .target(new LatLng(location.getLatitude(), location.getLongitude(), 10))
//                            .zoom(15)
//                            .tilt(60)
//                            .build()), 4000);
//        }


        if (isDriveMode == 1) {
            ambulanceLocation = Point.fromLngLat(location.getLongitude(),location.getLatitude());
            //ambulanceLocation = Point.fromLngLat(121.1388042795701,14.160415702850116 );
            Log.e("Route ID",routeID.toString());
            saveToFirebase(routeID + "",ambulanceLocation.latitude() + "," + ambulanceLocation.longitude(),false);
        }
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