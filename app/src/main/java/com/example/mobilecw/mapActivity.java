//Edward Davidson
//S1604249

package com.example.mobilecw;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.math.BigDecimal;


public class mapActivity extends AppCompatActivity implements OnMapReadyCallback{

    private String geoPnts;
    private String title;
    private double lat;
    private double lng;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            geoPnts = extras.getString("geoPnts");
            title = extras.getString("title");

            //split string at space and parse to double
            lat = Double.parseDouble(geoPnts.replaceAll(" .*", ""));
            lng = Double.parseDouble(geoPnts.replaceAll(".+ ", ""));

        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng marker = new LatLng(lat, lng);
        googleMap.addMarker(new MarkerOptions().position(marker).title(title));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        googleMap.setMaxZoomPreference(18f); //1 - 21

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 16.0f));
    }

}
