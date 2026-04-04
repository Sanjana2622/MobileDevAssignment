package com.example.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.photogallery.databinding.ActivityImageDetailBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageDetailActivity extends AppCompatActivity {

    private ActivityImageDetailBinding binding;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImageDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String uriStr = getIntent().getStringExtra("IMAGE_URI");
        if (uriStr == null) { finish(); return; }
        imageUri = Uri.parse(uriStr);

        loadImageDetails();
        setupDeleteButton();
    }

    /** Load the image and display all its metadata */
    private void loadImageDetails() {
        // Display full image using Glide
        Glide.with(this)
                .load(imageUri)
                .fitCenter()
                .into(binding.ivDetail);

        String name      = "Unknown";
        String size      = "Unknown";
        String dateTaken = "Unknown";

        try {
            // Fetch display name and file size
            android.database.Cursor cursor = getContentResolver().query(
                    imageUri,
                    new String[]{OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE},
                    null, null, null
            );
            if (cursor != null && cursor.moveToFirst()) {
                name = cursor.getString(
                        cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                long sizeBytes = cursor.getLong(
                        cursor.getColumnIndexOrThrow(OpenableColumns.SIZE));
                size = formatSize(sizeBytes);
                cursor.close();
            }

            // Fetch last modified date using DocumentsContract
            android.database.Cursor dateCursor = getContentResolver().query(
                    imageUri,
                    new String[]{DocumentsContract.Document.COLUMN_LAST_MODIFIED},
                    null, null, null
            );
            if (dateCursor != null && dateCursor.moveToFirst()) {
                long millis = dateCursor.getLong(0);
                if (millis > 0) {
                    dateTaken = new SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a", Locale.getDefault())
                            .format(new Date(millis));
                }
                dateCursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Could not read full metadata", Toast.LENGTH_SHORT).show();
        }

        binding.tvName.setText("📄 Name: " + name);
        binding.tvPath.setText("📁 Path: " + imageUri.toString());
        binding.tvSize.setText("💾 Size: " + size);
        binding.tvDate.setText("📅 Date: " + dateTaken);
    }

    /** Show confirmation dialog before deleting */
    private void setupDeleteButton() {
        binding.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Delete Image")
                        .setMessage("Are you sure you want to permanently delete this image? This cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> deleteImage())
                        .setNegativeButton("Cancel", null)
                        .show()
        );
    }

    /** Delete the image using DocumentsContract and return to gallery */
    private void deleteImage() {
        try {
            boolean deleted = DocumentsContract.deleteDocument(
                    getContentResolver(), imageUri);
            if (deleted) {
                Toast.makeText(this, "Image deleted", Toast.LENGTH_SHORT).show();
                finish(); // GalleryActivity.onResume() will auto-refresh the grid
            } else {
                Toast.makeText(this,
                        "Could not delete image", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error deleting: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Format bytes into KB or MB string */
    private String formatSize(long bytes) {
        if (bytes < 1024)             return bytes + " B";
        if (bytes < 1024 * 1024)      return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }
}