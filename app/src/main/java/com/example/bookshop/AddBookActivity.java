package com.example.bookshop;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.bookshop.model.Book;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddBookActivity extends AppCompatActivity {

    private EditText editTitle, editAuthor, editPrice, editDescription;
    private Button btnSaveBook, btnTakePhoto, btnBack;
    private ImageView imgBookPhoto;
    private FirebaseFirestore db;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_STORAGE_PERMISSION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        initViews();
        db = FirebaseFirestore.getInstance();

        btnSaveBook.setOnClickListener(v -> saveBook());
        btnTakePhoto.setOnClickListener(v -> {
            checkCameraPermissionAndOpen();
            checkStoragePermission();
        });
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (imgBookPhoto.getDrawable() != null) { // csak ha van kép
            imgBookPhoto.setAlpha(0f);
            imgBookPhoto.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .start();
        }
    }

    private void initViews() {
        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editPrice = findViewById(R.id.editPrice);
        editDescription = findViewById(R.id.editDescription);
        imgBookPhoto = findViewById(R.id.imgBookPhoto);
        btnSaveBook = findViewById(R.id.btnSaveBook);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnBack = findViewById(R.id.btnBack);
    }

    private void saveBook() {
        String title = editTitle.getText().toString().trim();
        String author = editAuthor.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String priceStr = editPrice.getText().toString().trim();

        if (title.isEmpty() || author.isEmpty() || description.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Minden mezőt tölts ki!", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Az árnak érvényes számnak kell lennie!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference docRef = db.collection("books").document();
        String id = docRef.getId();

        Book book = new Book(id, title, author, price, description);

        docRef.set(book)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Könyv mentve!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        } else {
            Toast.makeText(this, "Tároló elérés engedélyezve", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Nincs elérhető kamera alkalmazás", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                if (imageBitmap != null) {
                    imgBookPhoto.setImageBitmap(imageBitmap);
                } else {
                    Toast.makeText(this, "A kép nem elérhető", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Nem érkezett vissza adat", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Kamera engedély szükséges", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Tároló elérés engedélyezve", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Tároló elérés megtagadva", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
