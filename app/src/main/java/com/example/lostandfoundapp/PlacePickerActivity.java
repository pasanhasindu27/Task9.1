package com.example.lostandfoundapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PlacePickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String selectedPlace = "";
    private String selectedCoordinates = "";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private Button confirmButton;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_picker);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyDk4aXK5khZC808S32KRlGir6k0H2RTqsE");
        }

        // Add confirm button
        confirmButton = findViewById(R.id.confirm_button);
        confirmButton.setVisibility(View.GONE);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedLatLng != null) {
                    // Return the selected place to the calling activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("SELECTED_PLACE", selectedPlace);
                    resultIntent.putExtra("SELECTED_COORDINATES", selectedCoordinates);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(PlacePickerActivity.this, "Please select a location first", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize the AutocompleteSupportFragment
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        if (autocompleteFragment != null) {
            // Specify the types of place data to return
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID, 
                    Place.Field.NAME, 
                    Place.Field.ADDRESS, 
                    Place.Field.LAT_LNG));

            // Set up a PlaceSelectionListener to handle the response
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    // Get the place details
                    selectedPlace = place.getAddress() != null ? place.getAddress().toString() : "";
                    if (place.getLatLng() != null) {
                        selectedLatLng = place.getLatLng();
                        selectedCoordinates = selectedLatLng.latitude + "," + selectedLatLng.longitude;
                        
                        // Update map with selected location
                        updateMapLocation(selectedLatLng, place.getName());
                        confirmButton.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(Status status) {
                    Toast.makeText(PlacePickerActivity.this, "Place selection failed: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        
        // Enable my location button if permission is granted
        enableMyLocation();
        
        // Set default location (e.g., a central location in your target area)
        LatLng defaultLocation = new LatLng(37.7749, -122.4194); // San Francisco
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        
        // Set up map click listener
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                selectedLatLng = latLng;
                selectedCoordinates = latLng.latitude + "," + latLng.longitude;
                
                // Get address from coordinates using Geocoder
                getAddressFromLocation(latLng);
                
                // Update map with marker
                updateMapLocation(latLng, "Selected Location");
                confirmButton.setVisibility(View.VISIBLE);
            }
        });
    }
    
    private void getAddressFromLocation(LatLng latLng) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                
                // Get address lines
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i));
                    if (i < address.getMaxAddressLineIndex()) {
                        sb.append(", ");
                    }
                }
                
                selectedPlace = sb.toString();
                Toast.makeText(this, "Location selected: " + selectedPlace, Toast.LENGTH_SHORT).show();
            } else {
                selectedPlace = "Unknown location";
                Toast.makeText(this, "Could not find address for this location", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            selectedPlace = "Unknown location";
            Toast.makeText(this, "Geocoder service not available", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateMapLocation(LatLng latLng, String placeName) {
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(placeName));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }
    
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}