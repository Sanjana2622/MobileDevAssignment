package com.example.currencyconverter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.example.currencyconverter.databinding.ActivityMainBinding;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SharedPreferences prefs;

    // Exchange rates with USD as base currency (1 USD = X of currency)
    private final Map<String, Double> exchangeRates = new HashMap<String, Double>() {{
        put("USD", 1.0);
        put("INR", 83.50);
        put("JPY", 149.80);
        put("EUR", 0.92);
    }};

    // Currency symbols for display
    private final Map<String, String> symbols = new HashMap<String, String>() {{
        put("USD", "$");
        put("INR", "₹");
        put("JPY", "¥");
        put("EUR", "€");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Apply saved theme BEFORE super.onCreate to avoid flicker
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        applyTheme(prefs.getBoolean("dark_mode", false));

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupSpinners();
        setupButtons();
    }

    /** Populate both currency spinners with supported currencies */
    private void setupSpinners() {
        String[] currencies = {"INR", "USD", "JPY", "EUR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                currencies
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.spinnerFrom.setAdapter(adapter);
        binding.spinnerTo.setAdapter(adapter);

        // Default: FROM = INR, TO = USD
        binding.spinnerFrom.setSelection(0);
        binding.spinnerTo.setSelection(1);
    }

    /** Wire up Convert, Swap, and Settings buttons */
    private void setupButtons() {

        // CONVERT button
        binding.btnConvert.setOnClickListener(v -> {
            String amountStr = binding.etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                return;
            }
            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Enter a valid positive number", Toast.LENGTH_SHORT).show();
                return;
            }
            convertCurrency(amount);
        });

        // SWAP button — swap spinner selections
        binding.btnSwap.setOnClickListener(v -> {
            int fromPos = binding.spinnerFrom.getSelectedItemPosition();
            int toPos   = binding.spinnerTo.getSelectedItemPosition();
            binding.spinnerFrom.setSelection(toPos);
            binding.spinnerTo.setSelection(fromPos);
        });

        // SETTINGS button
        binding.btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }

    /**
     * Converts amount from spinnerFrom currency to spinnerTo currency.
     * Uses USD as intermediate/pivot currency.
     */
    private void convertCurrency(double amount) {
        String fromCurrency = binding.spinnerFrom.getSelectedItem().toString();
        String toCurrency   = binding.spinnerTo.getSelectedItem().toString();

        double fromRate = exchangeRates.containsKey(fromCurrency) ? exchangeRates.get(fromCurrency) : 1.0;
        double toRate   = exchangeRates.containsKey(toCurrency)   ? exchangeRates.get(toCurrency)   : 1.0;

        // Convert: source → USD → target
        double inUSD  = amount / fromRate;
        double result = inUSD * toRate;

        String toSymbol = symbols.containsKey(toCurrency) ? symbols.get(toCurrency) : "";

        binding.tvResult.setText(String.format("%s %.2f", toSymbol, result));
        binding.tvRate.setText(String.format("1 %s = %.4f %s",
                fromCurrency, toRate / fromRate, toCurrency));
    }

    /** Apply dark or light mode based on saved preference */
    private void applyTheme(boolean isDark) {
        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES
                        : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Re-apply theme in case user changed it in Settings
        applyTheme(prefs.getBoolean("dark_mode", false));
    }
}