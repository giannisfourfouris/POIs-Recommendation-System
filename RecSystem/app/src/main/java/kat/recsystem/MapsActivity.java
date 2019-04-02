package kat.recsystem;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.Serializable;
import java.util.ArrayList;

import static kat.recsystem.AndroidClient.pois;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, Serializable {

    private GoogleMap mMap;
    private ArrayList<Marker> markers = new ArrayList<>();
    private MarkerOptions options = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
       // Log.d("msg1",String.valueOf(i));
        for(Poi poi:pois){
            Log.d("msg",poi.toString());
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        mMap = googleMap;


        for (Poi poi : pois) {
            options.position(new LatLng(poi.getLatitude(),poi.getLongitude()));
            options.title(poi.getName());
            options.snippet(poi.getCategory());
            markers.add(mMap.addMarker(options));
            Log.d("msg2",poi.toString());
        }


        if(pois.isEmpty()){
            Toast.makeText(getApplicationContext(), "Did not find any Pois", Toast.LENGTH_SHORT).show();
            Intent intent = getIntent();
            double lat = intent.getDoubleExtra(HomeScreen.LATITUDE, 0);
            double longitude = intent.getDoubleExtra(HomeScreen.LONGITUDE, 0);
            LatLng focus = new LatLng(lat,  longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(focus));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
            mMap.setTrafficEnabled(true);
        } else {
            LatLng focus = new LatLng(pois.get(0).getLatitude(),  pois.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(focus));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12), 2000, null);
            mMap.setTrafficEnabled(true);
        }





        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        finish();

    }

    @Override
    public void onBackPressed() {
        pois.clear();
        mMap.clear();
        markers.clear();
        finish();
        super.onBackPressed();

        Intent myIntent = new Intent(this,HomeScreen.class);
        //myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myIntent);
    }
}
