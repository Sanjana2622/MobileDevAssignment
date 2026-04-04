package com.example.photogallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.photogallery.databinding.ActivityMainBinding;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    // Stores the URI where camera photo will be saved
    private Uri currentPhotoUri;
    private String currentPhotoPath;

    // ── Camera launcher ──────────────────────────────────────────────────────
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(),
                    success -> {
                        if (success) {
                            binding.tvPhotoSaved.setText(
                                    "✅ Photo saved to:\n" + currentPhotoPath);
                            Toast.makeText(this, "Photo saved!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this,
                                    "Photo capture cancelled", Toast.LENGTH_SHORT).show();
                        }
                    });

    // ── Permission launcher for camera ───────────────────────────────────────
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {
                        boolean cameraOk  = Boolean.TRUE.equals(results.get(Manifest.permission.CAMERA));
                        boolean storageOk;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            storageOk = Boolean.TRUE.equals(results.get(Manifest.permission.READ_MEDIA_IMAGES));
                        else
                            storageOk = Boolean.TRUE.equals(results.get(Manifest.permission.READ_EXTERNAL_STORAGE));

                        if (cameraOk && storageOk) launchCamera();
                        else Toast.makeText(this,
                                "Camera & storage permissions are required",
                                Toast.LENGTH_LONG).show();
                    });

    // ── Permission launcher for storage ──────────────────────────────────────
    private final ActivityResultLauncher<String[]> storagePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                    results -> {
                        boolean granted;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            granted = Boolean.TRUE.equals(results.get(Manifest.permission.READ_MEDIA_IMAGES));
                        else
                            granted = Boolean.TRUE.equals(results.get(Manifest.permission.READ_EXTERNAL_STORAGE));

                        if (granted) openFolderPicker();
                        else Toast.makeText(this,
                                "Storage permission required to browse folders",
                                Toast.LENGTH_LONG).show();
                    });

    // ── Folder picker launcher ───────────────────────────────────────────────
    private final ActivityResultLauncher<Uri> folderPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(),
                    uri -> {
                        if (uri != null) {
                            Intent intent = new Intent(this, GalleryActivity.class);
                            intent.putExtra("FOLDER_URI", uri.toString());
                            startActivity(intent);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnTakePhoto.setOnClickListener(v -> checkAndRequestCameraPermissions());
        binding.btnBrowseFolder.setOnClickListener(v -> checkAndRequestStoragePermissions());
    }

    // ── Permission helpers ───────────────────────────────────────────────────

    private void checkAndRequestCameraPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.CAMERA);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
        else
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        if (allGranted) launchCamera();
        else cameraPermissionLauncher.launch(permissions.toArray(new String[0]));
    }

    private void checkAndRequestStoragePermissions() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_GRANTED)
            openFolderPicker();
        else
            storagePermissionLauncher.launch(new String[]{permission});
    }

    // ── Camera ───────────────────────────────────────────────────────────────

    /**
     * Creates a unique timestamped image file in Pictures/PhotoGallery
     * then opens the camera to capture a photo directly into that file.
     */
    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            Uri uri = FileProvider.getUriForFile(
                    this,
                    "com.example.photogallery.fileprovider",
                    photoFile
            );
            currentPhotoUri  = uri;
            currentPhotoPath = photoFile.getAbsolutePath();
            cameraLauncher.launch(uri);
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error creating file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /** Creates a uniquely timestamped .jpg file in Pictures/PhotoGallery */
    private File createImageFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                .format(new Date());
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "PhotoGallery"
        );
        if (!storageDir.exists()) storageDir.mkdirs();
        return new File(storageDir, "IMG_" + timestamp + ".jpg");
    }

    private void openFolderPicker() {
        folderPickerLauncher.launch(null);
    }
}