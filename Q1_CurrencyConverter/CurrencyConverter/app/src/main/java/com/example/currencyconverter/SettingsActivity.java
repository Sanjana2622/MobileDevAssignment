package com.example.currencyconverter;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.currencyconverter.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Load saved dark mode preference
        boolean isDark = PreferenceManager
                .getDefaultSharedPreferences(this)
                .getBoolean("dark_mode", false);

        binding.switchDarkMode.setChecked(isDark);

        // Toggle theme and save preference when switch is changed
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean("dark_mode", isChecked)
                    .apply();

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO
            );
        });
    }
}
