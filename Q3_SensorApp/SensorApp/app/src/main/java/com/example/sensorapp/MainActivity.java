package com.example.sensorapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sensorapp.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private ActivityMainBinding binding;
    private SensorManager sensorManager;

    // Sensor references (null if not available on this device)
    private Sensor accelerometer;
    private Sensor lightSensor;
    private Sensor proximitySensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get default sensor for each type; null if not available on device
        accelerometer   = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor     = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // Warn user about any missing sensors
        List<String> missing = new ArrayList<>();
        if (accelerometer   == null) missing.add("Accelerometer");
        if (lightSensor     == null) missing.add("Light");
        if (proximitySensor == null) missing.add("Proximity");

        if (!missing.isEmpty()) {
            binding.tvSensorWarning.setText(
                    "⚠ Not available on this device: " + String.join(", ", missing));
        }
    }

    /** Register all sensor listeners when activity becomes visible */
    @Override
    protected void onResume() {
        super.onResume();
        // SENSOR_DELAY_UI is suitable for updating UI (not for gaming)
        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (lightSensor != null)
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_UI);
        if (proximitySensor != null)
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_UI);
    }

    /** Unregister listeners to save battery when activity is not visible */
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    /** Called whenever a registered sensor reports new data */
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {

            case Sensor.TYPE_ACCELEROMETER:
                // values[0]=X, values[1]=Y, values[2]=Z  in m/s²
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                binding.tvAccelX.setText(String.format("X: %.3f m/s²", x));
                binding.tvAccelY.setText(String.format("Y: %.3f m/s²", y));
                binding.tvAccelZ.setText(String.format("Z: %.3f m/s²", z));
                break;

            case Sensor.TYPE_LIGHT:
                // values[0] = illuminance in lux
                float lux = event.values[0];
                binding.tvLight.setText(String.format("Illuminance: %.1f lux", lux));
                binding.tvLightDesc.setText(describeLux(lux));
                break;

            case Sensor.TYPE_PROXIMITY:
                // values[0] = distance in cm
                float dist = event.values[0];
                float maxRange = proximitySensor != null ? proximitySensor.getMaximumRange() : 5f;
                binding.tvProximity.setText(String.format("Distance: %.1f cm", dist));
                binding.tvProximityDesc.setText(
                        dist < maxRange ? "🔴 Object NEAR" : "🟢 Object FAR / Clear");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not needed for this app
    }

    /** Returns a human-readable ambient light description */
    private String describeLux(float lux) {
        if (lux < 1)    return "Very Dark (night)";
        if (lux < 50)   return "Dim (indoor, night)";
        if (lux < 500)  return "Indoor lighting";
        if (lux < 1000) return "Overcast day";
        return "Bright sunlight";
    }
}