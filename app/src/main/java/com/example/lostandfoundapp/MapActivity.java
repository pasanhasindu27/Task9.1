package com.example.lostandfoundapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    
        // Set default location (Australia)
        LatLng australia = new LatLng(-25.2744, 133.7751);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(australia, 4));
    
        // Load items from database
        loadItemsOnMap();
    
        // Add debug info
        Toast.makeText(this, "Map is ready. If no markers appear, check database items.", Toast.LENGTH_LONG).show();
    }

    private void loadItemsOnMap() {
        List<Item> items = databaseHelper.getAllItems();
        
        if (items.isEmpty()) {
            Toast.makeText(this, "No items to display", Toast.LENGTH_SHORT).show();
            return;
        }
        
        for (Item item : items) {
            String location = item.getLocation();
            if (location != null && !location.isEmpty()) {
                try {
                    LatLng itemLocation = null;
                    
                    // Try to parse location in format "address|lat,lng"
                    if (location.contains("|")) {
                        String[] parts = location.split("\\|");
                        if (parts.length > 1) {
                            String[] coords = parts[1].split(",");
                            if (coords.length == 2) {
                                double lat = Double.parseDouble(coords[0]);
                                double lng = Double.parseDouble(coords[1]);
                                itemLocation = new LatLng(lat, lng);
                            }
                        }
                    } 
                    // Try to parse location in format "lat,lng"
                    else if (location.contains(",")) {
                        String[] coords = location.split(",");
                        if (coords.length == 2) {
                            double lat = Double.parseDouble(coords[0]);
                            double lng = Double.parseDouble(coords[1]);
                            itemLocation = new LatLng(lat, lng);
                        }
                    }
                    
                    if (itemLocation != null) {
                        // Add marker with different color based on type
                        float markerColor = item.getType().equalsIgnoreCase("Lost") ? 
                                BitmapDescriptorFactory.HUE_RED : BitmapDescriptorFactory.HUE_GREEN;
                        
                        mMap.addMarker(new MarkerOptions()
                                .position(itemLocation)
                                .title(item.getName())
                                .snippet(item.getDescription())
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error parsing location: " + location, Toast.LENGTH_SHORT).show();
                }
            }
        }
        
        // If we have items with locations, zoom to fit all markers
        if (mMap.getProjection() != null && !items.isEmpty()) {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                boolean hasValidMarkers = false;
                
                for (Item item : items) {
                    String location = item.getLocation();
                    if (location != null && !location.isEmpty()) {
                        LatLng itemLocation = null;
                        
                        // Try to parse location in format "address|lat,lng"
                        if (location.contains("|")) {
                            String[] parts = location.split("\\|");
                            if (parts.length > 1) {
                                String[] coords = parts[1].split(",");
                                if (coords.length == 2) {
                                    double lat = Double.parseDouble(coords[0]);
                                    double lng = Double.parseDouble(coords[1]);
                                    itemLocation = new LatLng(lat, lng);
                                }
                            }
                        } 
                        // Try to parse location in format "lat,lng"
                        else if (location.contains(",")) {
                            String[] coords = location.split(",");
                            if (coords.length == 2) {
                                double lat = Double.parseDouble(coords[0]);
                                double lng = Double.parseDouble(coords[1]);
                                itemLocation = new LatLng(lat, lng);
                            }
                        }
                        
                        if (itemLocation != null) {
                            builder.include(itemLocation);
                            hasValidMarkers = true;
                        }
                    }
                }
                
                if (hasValidMarkers) {
                    // Add padding to the bounds
                    int padding = 100; // in pixels
                    LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}