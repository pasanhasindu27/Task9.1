package com.example.lostandfoundapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ItemAdapter extends ArrayAdapter<Item> {
    private Context mContext;
    private int mResource;

    public ItemAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Item> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }

        // Get the item at this position
        Item currentItem = getItem(position);
        
        // Set the item type
        TextView itemTypeTextView = convertView.findViewById(R.id.itemTypeTextView);
        itemTypeTextView.setText(currentItem.getType());
        
        // Set the item name
        TextView itemNameTextView = convertView.findViewById(R.id.itemNameTextView);
        itemNameTextView.setText(currentItem.getName());
        
        // Set the item date
        TextView itemDateTextView = convertView.findViewById(R.id.itemDateTextView);
        itemDateTextView.setText(currentItem.getDate());
        
        // Format and set the location
        TextView itemLocationTextView = convertView.findViewById(R.id.itemLocationTextView);
        String locationText = formatLocation(currentItem.getLocation());
        itemLocationTextView.setText("Location: " + locationText);
        
        return convertView;
    }

    // Helper method to format location string
    private String formatLocation(String location) {
        if (location == null || location.isEmpty()) {
            return "Unknown location";
        }
        
        // Check if location contains address part
        if (location.contains("|")) {
            // Format: "address|lat,lng"
            String[] parts = location.split("\\|");
            if (parts.length > 0) {
                return parts[0]; // Return just the address part
            }
        }
        
        // If it's just coordinates or couldn't parse address
        if (location.contains(",")) {
            return "Map coordinates: " + location;
        }
        
        return location;
    }
}