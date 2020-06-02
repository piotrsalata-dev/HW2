package com.example.hw2;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLoadedCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 101;
    private LocationRequest mLocationRequest;
    private LocationCallback locationCallback;
    private SensorManager sensorManager = null;
    private TextView accelerometer = null;
    Marker gpsMarker = null;
    List<Marker> markerList;
    List<MarkerOptions> markerOptList;
    private Animation fab_open,fab_close;
    private Boolean isFabOpen = false;
    protected boolean accelerationPOP = false;
    private boolean animecheck = false;
    private FloatingActionButton currentLocalisation = null;
    private FloatingActionButton pauseLocalisation = null;
    private Button clearMemory = null;

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapLongClickListener(this);
        // Add a marker in Sydney and move the camera
      //  LatLng sydney = new LatLng(-34, 151);
       // mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(mLocationRequest,locationCallback, null);
    }

    private void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult != null) {
                    if(gpsMarker != null) gpsMarker.remove();
                    Location location = locationResult.getLastLocation();
                    gpsMarker = mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(location.getLatitude(),location.getLongitude()))
                            .alpha(0.8f)
                            .title("Current Location")
                    );
                }
            }
        };
    }

    @Override
    public void onMapLoaded() {
        Log.i(MapsActivity.class.getSimpleName(), "MapLoaded");
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
        Task<Location> lastlocation = fusedLocationClient.getLastLocation();

        lastlocation.addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null && mMap != null) {
                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(getString(R.string.last_known_loc_msg)));
                }
            }
        });
        createLocationRequest();
        createLocationCallback();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        startLocationUpdates();
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        markerList = new ArrayList<>();
        markerOptList = new ArrayList<>();


        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        currentLocalisation = findViewById(R.id. floatingActionButton2);
        pauseLocalisation = findViewById(R.id.floatingActionButton);
        accelerometer = findViewById(R.id. acceleration_info);
        clearMemory = findViewById(R.id. clear_memory);

        currentLocalisation.setVisibility(View.INVISIBLE);
        pauseLocalisation.setVisibility(View.INVISIBLE);
        accelerometer.setVisibility(View.INVISIBLE);

        currentLocalisation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean hidingShield = false;
                if (accelerationPOP == true && hidingShield == false){
                    accelerometer.setVisibility(View.INVISIBLE);
                    accelerationPOP = false;
                    hidingShield = true;
                }
                if (accelerationPOP == false && hidingShield == false) {
                    accelerometer.setVisibility(View.VISIBLE);
                    accelerationPOP = true;
                    hidingShield = true;
                }
            }
        });

        pauseLocalisation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocalisation.setVisibility(View.INVISIBLE);
                pauseLocalisation.setVisibility(View.INVISIBLE);
                accelerometer.setVisibility(View.INVISIBLE);
                getMarkers();
                animateFABout();
                accelerationPOP = false;
            }
        });
        clearMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(Marker marker: markerList) marker.remove();
                markerList.clear();
                markerOptList.clear();
                animateFABout();
                currentLocalisation.setVisibility(View.INVISIBLE);
                pauseLocalisation.setVisibility(View.INVISIBLE);
                accelerometer.setVisibility(View.INVISIBLE);
                accelerationPOP = false;
           }
        });

        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (accelerometer != null)
                    accelerometer.setText(String.format("Acceleration: \n x: %.4f, y: %.4f", event.values[0], event.values[1]));
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
    }

    public void animateFABin(){

        if (isFabOpen == false && animecheck == false) {
                currentLocalisation.startAnimation(fab_open);
                pauseLocalisation.startAnimation(fab_open);
                isFabOpen = true;
                animecheck = true;
            }
    }

    public void animateFABout(){
        if (isFabOpen == true){
            currentLocalisation.startAnimation(fab_close);
            pauseLocalisation.startAnimation(fab_close);
            isFabOpen = false;
            animecheck = false;
        }
    }

    public void zoomInClick(View v) {
        mMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    public void zoomOutClick(View v) {
        mMap. moveCamera(CameraUpdateFactory.zoomOut());
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions marker = new MarkerOptions()
                .position(new LatLng(latLng.latitude, latLng.longitude))
                .alpha(0.8f)
                .title(String.format("Position:(%.2f, %.2f)", latLng.latitude, latLng.longitude));
        Marker mark = mMap.addMarker(marker);
        markerList.add(mark);
        markerOptList.add(marker);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
       // CameraPosition cameraPos = mMap.getCameraPosition();
       // if (cameraPos.zoom < 14f) mMap.moveCamera(CameraUpdateFactory.zoomTo(14f));
       if(currentLocalisation != null && pauseLocalisation != null && accelerometer != null) {
           animateFABin();
           currentLocalisation.setVisibility(View.VISIBLE);
           pauseLocalisation.setVisibility(View.VISIBLE);
       }
        return false;
    }

    private void saveMarkers(List<MarkerOptions> markers){
        Gson gson = new Gson();
        String to_save = gson.toJson(markers);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = openFileOutput("locations.json", MODE_PRIVATE);
            FileWriter writer = new FileWriter(fileOutputStream.getFD());
            writer.write(to_save);
            writer.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void getMarkers(){
        FileInputStream fileInputStream;
        int DEFAULT_BUFFER_SIZE = 10000;
        Gson gson = new Gson();
        String ReadJson;

        try{
            fileInputStream = openFileInput("locations.json");
            FileReader reader = new FileReader((fileInputStream.getFD()));
            char[] buf = new char[DEFAULT_BUFFER_SIZE];
            int n;
            StringBuilder builder = new StringBuilder();
            while ((n = reader.read(buf)) >= 0){
                String tmp = String.valueOf(buf);
                String substring = (n<DEFAULT_BUFFER_SIZE) ? tmp.substring(0, n) : tmp;
                builder.append(substring);
            }
            reader.close();
            ReadJson = builder.toString();
            Type collectionType = new TypeToken<List<Marker>>(){}.getType();
            List<Marker> o = gson.fromJson(ReadJson, collectionType);
            if(o != null){
                markerList.clear();
                markerList.addAll(o);
                for(MarkerOptions i: markerOptList){
                    mMap.addMarker(i);
                }
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        saveMarkers(markerOptList);
        super.onDestroy();
    }

}
