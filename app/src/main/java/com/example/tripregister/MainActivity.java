package com.example.tripregister;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    static ArrayList<String> places=new ArrayList<>();
    static ArrayList<LatLng> latLngsArray=new ArrayList<>();
    static ArrayAdapter arrayAdapter;
    ArrayList<String> latitudes=new ArrayList<>();
    ArrayList<String> longitudes=new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView=findViewById(R.id.list_view);


        places.clear();
        latLngsArray.clear();
        latitudes.clear();
        longitudes.clear();


        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.tripregister", Context.MODE_PRIVATE);

        try {

            places=(ArrayList<String>)
                    ObjectSerializer.deserialize(
                            sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
            latitudes=(ArrayList<String>)
                    ObjectSerializer.deserialize(
                            sharedPreferences.getString("latitudes",ObjectSerializer.serialize(new ArrayList<String>())));
            longitudes=(ArrayList<String>)
                    ObjectSerializer.deserialize(
                            sharedPreferences.getString("longitudes",ObjectSerializer.serialize(new ArrayList<String>())));

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (places.size()>0 && longitudes.size()>0 && latitudes.size()>0){
            if(places.size()==longitudes.size()&&longitudes.size()==latitudes.size())
            {
                for(int i=0;i<longitudes.size();i++){
                    latLngsArray.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }}
        else {
            places.add("Add a new place....");
            latLngsArray.add(new LatLng(0,0));
        }
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,places);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getApplicationContext(),MapsActivity.class);
                intent.putExtra("place number",position);
                startActivity(intent);
            }
        });
    }
    /* @Override
    protected void onDestroy() {
        super.onDestroy();

        SharedPreferences sharedPreferences=this.getSharedPreferences("com.example.myapplication",Context.MODE_PRIVATE);
        try {
            latitudes.clear();
            longitudes.clear();

            for(LatLng coordinate:MainActivity.latLngsArray){
                latitudes.add(Double.toString(coordinate.latitude));
                longitudes.add(Double.toString(coordinate.longitude));
            }

            sharedPreferences.edit().putString("places",ObjectSerializer.serialize(places)).apply();
            sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitudes)).apply();
            sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitudes)).apply();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/
}
