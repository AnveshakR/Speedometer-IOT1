package com.example.speedometer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    Location myLocation; // making Location object
    int timeDelay = 10000; // time delay between readings

    LocationManager locationManager; // making LocationManager object
    TextView textCurrentSpeed, textOdometer, textAvgSpeed;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        textCurrentSpeed = findViewById(R.id.dispCurrentSpeed); // attaching current speed textview to variable
        textOdometer = findViewById(R.id.dispOdometer); // attaching odometer textview to variable
        textAvgSpeed = findViewById(R.id.dispAvgSpeed); //attaching avg speed textview to variable

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "Location permission not granted. Please grant permission.", Toast.LENGTH_LONG).show();
            alertbox(); // asking for Fine/Coarse location permissions
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeDelay, 0, locationListenerGPS);
    }

    protected void alertbox()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    public void updateLocation() {
        Location location = null;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permission to gps not granted. Grant permissions and try again or restart the application.", Toast.LENGTH_LONG).show();
            alertbox();
        }
        else {
            location = locationManager.getLastKnownLocation(Context.LOCATION_SERVICE);
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeDelay, 0, locationListenerGPS);
    }

    protected double earthDistance(double lat1, double lon1, double lon2, double lat2)
    { // calculates distance between two points on the earth using Haversine formula

        final double R = 6371e3; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public void buttonStart(View MainActivity)
    {
        updateLocation();
    }

    LocationListener locationListenerGPS = new LocationListener() { // Locationlistener methods

        double templat = 404, templong = 404; // initializing the temp variables for lat and long to a dummy value
        double odometer = 0, totalTime = 0, currentDist, currentSpeed, avgSpeed;

        @SuppressLint("SetTextI18n")
        @Override
        public void onLocationChanged(Location location) {

            myLocation = location;

            if (templat == 404 && templong == 404) { // if the dummy values havent been changed then they are set to the current location
                templat = location.getLatitude();
                templong = location.getLongitude();
            } else {
                // if there is a location in the temp variables, then they are the old locations before the location update
                // so we get another set of coordinates and calculate distance between them

                currentDist = earthDistance(location.getLatitude(), location.getLongitude(), templong, templat);
                currentDist = (double)(Math.round(currentDist*1000d)/1000d); //distance between two points
                templat = location.getLatitude(); // setting the current lat/long to the temp variables as they have become old
                templong = location.getLongitude();

                odometer += currentDist; // adding current distance to the overall distance
                odometer = (double)(Math.round(odometer*1000d)/1000d);
                totalTime += (timeDelay/1000d); // incrementing the total time
                currentSpeed = currentDist / (timeDelay/1000d);
                currentSpeed = (double)(Math.round(currentSpeed*1000d)/1000d);
                avgSpeed = odometer / totalTime; // average speed is total distance/total time
                avgSpeed = (double)(Math.round(avgSpeed*1000d)/1000d);

//                System.out.println();
//                System.out.println("dist: "+currentDist);
//                System.out.println("odo: "+odometer);
//                System.out.println("currspeed: "+currentSpeed);
//                System.out.println("avgspeed: "+avgSpeed);

                textCurrentSpeed.setText(currentSpeed + " m/s");
                textOdometer.setText(odometer + "m");
                textAvgSpeed.setText(avgSpeed + "m/s"); // setting the textviews to their values

            }
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
}

