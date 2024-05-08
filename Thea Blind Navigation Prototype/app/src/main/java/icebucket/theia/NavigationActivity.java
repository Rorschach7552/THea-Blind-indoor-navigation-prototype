package icebucket.theia;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener {

    FusedLocationProviderClient mFusedLocationClient;
    private String locationStr = "";
    int PERMISSION_ID = 44;
    private static final float CHANGE_THRESHOLD = 10.0f;
    private static final String TAG = "FallDetectionSensor";
    private SensorManager sensorManager;
    Vibrator vibrator;
    private boolean moIsMin = false;
    private boolean moIsMax = false;
    private long mlPreviousTime;
    private int i = 0;
    private TextView coordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Activity.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        MainActivity.speak("Tap the top of the screen to speak destination. Tap the middle of the screen for destination suggestions. Tap the bottom of the screen to cancel");


        // fill text field
        coordText = findViewById(R.id.coordinateView);
        coordText.setText(locationStr);

        // init get destination button
        Button getDestinationButton = findViewById(R.id.buttonSayDestination);
        getDestinationButton.setOnClickListener(this::getDestinationEvent);

        // init speak suggestions button
        Button speakSuggestionsButton = findViewById(R.id.buttonSuggestDestination);
        speakSuggestionsButton.setOnClickListener(this::speakSuggestionsEvent);

        // init end route button
        Button endButton = findViewById(R.id.endRouteButton);
        endButton.setOnClickListener(this::endRouteEvent);
    }

    private void endRouteEvent(View view) {
        MainActivity.prioritySpeak("Ending navigation");
        startActivity(new Intent(NavigationActivity.this, MainActivity.class));
    }

    private void getDestinationEvent(View view) {
        MainActivity.prioritySpeak("Your location is " + locationStr);

        sleep(6);
        promptSpeechInput();
    }

    private void speakSuggestionsEvent(View view) {
        MainActivity.speak("Your location is " + locationStr);
        sleep(5);
        MainActivity.speak("Location Suggestions: Bathroom, Kitchen, Bedroom");

        sleep(3);
        promptSpeechInput();
    }

    private void promptSpeechInput() {
        MainActivity.speak("Speak your desired destination");

        sleep(4);
        MainActivity.speak("Bathroom selected");

        speakBathroomNavigation();
    }

    private void speakBathroomNavigation() {
        MainActivity.speak("Calculating route");
        sleep(3);

        String[] lines = {"Move forward 10 steps", "Turn right", "Stairs detected, proceed with caution", "Move forward 6 steps", "Destination Reached"};

        for (int i = 0; i < lines.length; i++) {

            if (i == 2) {
                vibrator.vibrate(1000);
            }

            getLastLocation();
            MainActivity.speak(lines[i]);
            sleep((int) (lines[i].length() / 15 + 2)); // This isn't based on anything specific. I just fiddled with it until it sounded okay
        }
    }

    private void sleep(int i) {
        try {TimeUnit.SECONDS.sleep(i);} catch (InterruptedException e) {}
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        // check if permissions are given
        if (checkPermissions()) {

            // check if location is enabled
            if (isLocationEnabled()) {

                // getting last
                // location from
                // FusedLocationClient
                // object
                mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location == null) {
                        requestNewLocationData();
                    } else {
                        locationStr = String.format("%.2f", location.getLatitude()) + " " + String.format("%.2f", location.getLongitude()); //here
                        coordText.setText(locationStr);
                    }
                });
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            // if permissions aren't available,
            // request for permissions
            requestPermissions();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        // setting LocationRequest
        // on FusedLocationClient
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            locationStr = String.format("%.2f", mLastLocation.getLatitude()) + " " + String.format("%.2f", mLastLocation.getLongitude()); //here
            coordText.setText(locationStr);
        }
    };

    // method to check for permissions
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // method to request for permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // If everything is alright then
    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mGravity = null;
        float[] mGeomagnetic = null;
        float lastAzimuth = -1;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
            double loX = event.values[0];
            double loY = event.values[1];
            double loZ = event.values[2];

            double loAccelerationReader = Math.sqrt(Math.pow(loX, 2) + Math.pow(loY, 2) + Math.pow(loZ, 2));
            mlPreviousTime = System.currentTimeMillis();

            Log.i(TAG, "Acceleration readings: X=" + loX + ", Y=" + loY + ", Z=" + loZ);

            if (loAccelerationReader <= 6.0) {
                moIsMin = true;
                Log.i(TAG, "Minimum threshold reached");
            }

            if (moIsMin) {
                i++;
                Log.i(TAG, "Acceleration: " + loAccelerationReader);
                if (loAccelerationReader >= 7.0) {
                    long llCurrentTime = System.currentTimeMillis();
                    long llTimeDiff = llCurrentTime - mlPreviousTime;
                    if (llTimeDiff >= 1) {
                        moIsMax = true;
                        Log.i(TAG, "Maximum threshold reached");
                    }
                }
            }

            if (moIsMin && moIsMax) {
                Log.i(TAG, "FALL DETECTED");
                startActivity(new Intent(NavigationActivity.this, CountdownActivity.class));
                unRegister();
                resetDetection();
            }

            if (i > 5) {
                resetDetection();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
            if (mGravity != null) {
                float[] R = new float[9];
                float[] I = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
                if (success) {
                    float[] orientation = new float[3];
                    SensorManager.getOrientation(R, orientation);

                    float azimuthInDegrees = (float)(Math.toDegrees(orientation[0])+360)%360;

                    if (Math.abs(azimuthInDegrees - lastAzimuth) > CHANGE_THRESHOLD) {
                        lastAzimuth = azimuthInDegrees;
                        String direction = getCardinalDirection(azimuthInDegrees);
                        MainActivity.speak(direction);
                    }
                }
            }
        }
    }
    private String getCardinalDirection(float azimuthDegrees) {
        String[] directions = {"North", "North-East", "East", "South-East", "South", "South-West", "West", "North-West", "North"};
        return directions[(int)Math.round(((azimuthDegrees % 360) / 45))];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void resetDetection() {
        i = 0;
        moIsMin = false;
        moIsMax = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    private void unRegister() {
        sensorManager.unregisterListener(this);
    }
}
