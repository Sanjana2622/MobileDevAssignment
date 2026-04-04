package com.example.photogallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.photogallery.databinding.ActivityGalleryBinding;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private ActivityGalleryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String folderUriStr = getIntent().getStringExtra("FOLDER_URI");
        if (folderUriStr == null) {
            Toast.makeText(this, "No folder selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        loadImagesFromFolder(Uri.parse(folderUriStr));
    }

    /**
     * Loads all image URIs from the selected folder using
     * DocumentsContract (works with content:// URIs from folder picker).
     */
    private void loadImagesFromFolder(Uri folderUri) {
        List<Uri> imageUris = new ArrayList<>();

        try {
            String docId = DocumentsContract.getTreeDocumentId(folderUri);
            Uri childrenUri = DocumentsContract
                    .buildChildDocumentsUriUsingTree(folderUri, docId);

            String[] projection = {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE
            };

            android.database.Cursor cursor = getContentResolver()
                    .query(childrenUri, projection, null, null, null);

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String mimeType = cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                    DocumentsContract.Document.COLUMN_MIME_TYPE));
                    // Include only image files
                    if (mimeType != null && mimeType.startsWith("image/")) {
                        String id = cursor.getString(
                                cursor.getColumnIndexOrThrow(
                                        DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                        Uri imageUri = DocumentsContract
                                .buildDocumentUriUsingTree(folderUri, id);
                        imageUris.add(imageUri);
                    }
                }
                cursor.close();
            }
        } catch (Exception e) {
            Toast.makeText(this,
                    "Error reading folder: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (imageUris.isEmpty()) {
            binding.tvImageCount.setText("No images found in this folder");
            return;
        }

        binding.tvFolderName.setText("Folder: " + folderUri.getLastPathSegment());
        binding.tvImageCount.setText(imageUris.size() + " image(s) found");

        // Set up 3-column grid
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        binding.recyclerView.setAdapter(new ImageAdapter(imageUris, uri -> {
            Intent intent = new Intent(this, ImageDetailActivity.class);
            intent.putExtra("IMAGE_URI", uri.toString());
            startActivity(intent);
        }));
    }

    /** Refresh gallery when returning from ImageDetailActivity (e.g. after delete) */
    @Override
    protected void onResume() {
        super.onResume();
        String folderUriStr = getIntent().getStringExtra("FOLDER_URI");
        if (folderUriStr != null)
            loadImagesFromFolder(Uri.parse(folderUriStr));
    }
}