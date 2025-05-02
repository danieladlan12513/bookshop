package com.example.bookshop;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookshop.model.Book;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditBookActivity extends AppCompatActivity {

    private EditText editTitle, editAuthor, editPrice, editDescription;
    private Button btnUpdateBook, btnBack;
    private FirebaseFirestore db;
    private String bookId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);

        db = FirebaseFirestore.getInstance();

        editTitle = findViewById(R.id.editTitle);
        editAuthor = findViewById(R.id.editAuthor);
        editPrice = findViewById(R.id.editPrice);
        editDescription = findViewById(R.id.editDescription);
        btnUpdateBook = findViewById(R.id.btnUpdateBook);
        btnBack = findViewById(R.id.btnBack);

        bookId = getIntent().getStringExtra("BOOK_ID");

        if (bookId == null || bookId.isEmpty()) {
            Toast.makeText(this, "Érvénytelen könyv ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadBookData(bookId);

        btnUpdateBook.setOnClickListener(v -> updateBook());
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

    }

    private void loadBookData(String id) {
        db.collection("books").document(id).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Book book = documentSnapshot.toObject(Book.class);
                if (book != null) {
                    editTitle.setText(book.getTitle());
                    editAuthor.setText(book.getAuthor());
                    editPrice.setText(String.valueOf(book.getPrice()));
                    editDescription.setText(book.getDescription());
                }
            } else {
                Toast.makeText(this, "A könyv nem található", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void updateBook() {
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
            Toast.makeText(this, "Az árnak számnak kell lennie!", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference bookRef = db.collection("books").document(bookId);
        bookRef.update(
                "title", title,
                "author", author,
                "description", description,
                "price", price
        ).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Könyv frissítve!", Toast.LENGTH_SHORT).show();
            setResult(Activity.RESULT_OK);
            finish();
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Frissítési hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

}
