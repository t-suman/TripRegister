package com.example.tripregister;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapClickListener {


    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Boolean b=true;
    private Button add;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }
            }
        }
    }

    private void centerLocation(Location location, String title) {
        LatLng user = new LatLng(location.getLatitude(), location.getLongitude());

        //mMap.clear();
        if(title!="Your Location")
            mMap.addMarker(new MarkerOptions().position(user).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 7));
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        add=findViewById(R.id.button);
        mMap.setOnMapClickListener(this);
        intent=getIntent();

        if(intent.getIntExtra("place number",0)==0) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng user=new LatLng(location.getLatitude(),location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 7));

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                @Override
                public void onProviderEnabled(String provider) {
                }

                @Override
                public void onProviderDisabled(String provider) {
                }
            };

                if (Build.VERSION.SDK_INT >= 23) {

                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);



                } else {
                    if(b){
                        Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        centerLocation(lastKnown,"Your Location");

                        b=false;
                    }

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }

            } else {
                if (b) {
                    Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    centerLocation(lastKnown,"Your Location");

                    b=false;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }

        }
        else {
            Location placeLocation=new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.latLngsArray.get(intent.getIntExtra("place number",0)).latitude);
            placeLocation.setLongitude(MainActivity.latLngsArray.get(intent.getIntExtra("place number",0)).longitude);

            centerLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("place number",0)));

        }

    }

    private void initilize() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            centerLocation(lastKnown, "Your Location");
            b = false;
        }
    }


    public void save(View view, String address, LatLng latLng){
        MainActivity.places.add(address);
        MainActivity.latLngsArray.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.tripregister",Context.MODE_PRIVATE);
        try {
            ArrayList<String> latitudes=new ArrayList<>();
            ArrayList<String> longitudes=new ArrayList<>();

            for(LatLng coordinate:MainActivity.latLngsArray){
                latitudes.add(Double.toString(coordinate.latitude));
                longitudes.add(Double.toString(coordinate.longitude));
            }

            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
            sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitudes)).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }


        Toast.makeText(this,"Location Saved",Toast.LENGTH_SHORT).show();
        super.finish();
    }

    @Override
    public void onMapClick(final LatLng latLng) {
        if (intent.getIntExtra("place number", 0) == 0) {

            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String address = "";
            try {
                List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (!listAddress.isEmpty() && listAddress.size() > 0) {
                    if (listAddress.get(0).getThoroughfare() != null) {
                        if (listAddress.get(0).getSubThoroughfare() != null) {
                            address += listAddress.get(0).getSubThoroughfare();
                        }
                        address += listAddress.get(0).getThoroughfare();

                    }
                    if(listAddress.get(0).getLocality()!=null){
                        address+=", "+listAddress.get(0).getLocality();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (address.isEmpty()) {
                Date d = new Date();
                address += DateFormat.format("hh : mm : ss   dd-MM-yyyy ", d.getTime());
            }
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            add.setVisibility(View.VISIBLE);
            final String finalAddress = address;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save(v, finalAddress, latLng);
                }
            });
        }
    }
}
