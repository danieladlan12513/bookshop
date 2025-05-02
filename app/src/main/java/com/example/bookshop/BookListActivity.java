package com.example.bookshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class BookListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter adapter;
    private ArrayList<Book> bookList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        initViews();
        setupRecyclerView();
        loadBooksFromFirestore();

        findViewById(R.id.btnCheapBooks).setOnClickListener(v -> loadBooksByPriceLimit());
        findViewById(R.id.btnSortPrice).setOnClickListener(v -> loadTopBooks());
        findViewById(R.id.btnJokai).setOnClickListener(v -> loadBooksByAuthorJokai());
        findViewById(R.id.btnSortPriceAsc).setOnClickListener(v -> loadBooksByPriceAscending()); // ÚJ GOMB!
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK) {
            loadBooksFromFirestore();
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewBooks);
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        bookList = new ArrayList<>();
        adapter = new BookAdapter(bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadBooksFromFirestore() {
        db = FirebaseFirestore.getInstance();
        db.collection("books").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    bookList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Book book = doc.toObject(Book.class);
                            bookList.add(book);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Nem sikerült betölteni a könyveket: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadBooksByPriceLimit() {
        db.collection("books")
                .whereLessThan("price", 3000)
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(this::handleQueryFailure);
    }

    private void loadTopBooks() {
        db.collection("books")
                .orderBy("price", Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(this::handleQueryFailure);
    }

    private void loadBooksByAuthorJokai() {
        db.collection("books")
                .whereEqualTo("author", "Jókai Mór")
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(this::handleQueryFailure);
    }

    private void loadBooksByPriceAscending() {
        db.collection("books")
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(this::handleQuerySuccess)
                .addOnFailureListener(this::handleQueryFailure);
    }

    private void handleQuerySuccess(QuerySnapshot snapshots) {
        bookList.clear();
        for (QueryDocumentSnapshot doc : snapshots) {
            Book book = doc.toObject(Book.class);
            bookList.add(book);
        }
        adapter.notifyDataSetChanged();
    }

    private void handleQueryFailure(@NonNull Exception e) {
        Toast.makeText(this, "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

