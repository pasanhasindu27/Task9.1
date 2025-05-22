package com.example.lostandfoundapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class NewAdvertActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    
    private EditText nameEditText, phoneEditText, descriptionEditText, dateEditText, locationEditText;
    private RadioGroup typeRadioGroup;
    private RadioButton lostRadioButton, foundRadioButton;
    private Button getCurrentLocationButton, saveButton;
    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseHelper databaseHelper;
    private String currentLocation = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_advert);

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        dateEditText = findViewById(R.id.dateEditText);
        locationEditText = findViewById(R.id.locationEditText);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        lostRadioButton = findViewById(R.id.lostRadioButton);
        foundRadioButton = findViewById(R.id.foundRadioButton);
        getCurrentLocationButton = findViewById(R.id.getCurrentLocationButton);
        saveButton = findViewById(R.id.saveButton);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Set up get current location button click listener
        getCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        // Set up save button click listener
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAdvert();
            }
        });

        // Set up location edit text click listener to open place picker
        locationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewAdvertActivity.this, PlacePickerActivity.class);
                startActivityForResult(intent, 1);
            }
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get the location coordinates
                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();
                                currentLocation = latitude + "," + longitude;
                                
                                // Try to get the address from coordinates
                                try {
                                    Geocoder geocoder = new Geocoder(NewAdvertActivity.this, Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        Address address = addresses.get(0);
                                        String addressText = address.getAddressLine(0);
                                        locationEditText.setText(addressText);
                                    } else {
                                        locationEditText.setText(currentLocation);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    locationEditText.setText(currentLocation);
                                }
                            } else {
                                Toast.makeText(NewAdvertActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String selectedPlace = data.getStringExtra("SELECTED_PLACE");
            String selectedCoordinates = data.getStringExtra("SELECTED_COORDINATES");
            locationEditText.setText(selectedPlace);
            currentLocation = selectedCoordinates;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAdvert() {
        // Get values from form
        String name = nameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        
        // Get selected radio button
        int selectedRadioButtonId = typeRadioGroup.getCheckedRadioButtonId();
        String type = "";
        if (selectedRadioButtonId == R.id.lostRadioButton) {
            type = "Lost";
        } else if (selectedRadioButtonId == R.id.foundRadioButton) {
            type = "Found";
        }
        
        // Validate inputs
        if (name.isEmpty() || phone.isEmpty() || description.isEmpty() || date.isEmpty() || location.isEmpty() || type.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Use coordinates for database if available, otherwise use the text
        String locationToSave = currentLocation.isEmpty() ? location : currentLocation;
        
        // Save to database
        long result = databaseHelper.insertItem(type, name, phone, description, date, locationToSave);
        
        if (result != -1) {
            Toast.makeText(this, "Advertisement saved successfully", Toast.LENGTH_SHORT).show();
            // Return to main activity
            finish();
        } else {
            Toast.makeText(this, "Failed to save advertisement", Toast.LENGTH_SHORT).show();
        }
    }
}