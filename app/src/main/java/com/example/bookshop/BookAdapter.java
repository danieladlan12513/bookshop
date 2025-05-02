package com.example.bookshop;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookshop.model.Book;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private List<Book> bookList;

    public BookAdapter(List<Book> books) {
        this.bookList = books;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false);
        return new BookViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        Book book = bookList.get(position);
        holder.txtTitle.setText(book.getTitle());
        holder.txtAuthor.setText("Szerző: " + book.getAuthor());
        holder.txtPrice.setText("Ár: " + book.getPrice() + " Ft");
        holder.txtDescription.setText(book.getDescription());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), EditBookActivity.class);
            intent.putExtra("BOOK_ID", book.getId());
            ((Activity) v.getContext()).startActivityForResult(intent, 123);
        });

        holder.btnDelete.setOnClickListener(v -> {
            FirebaseFirestore.getInstance()
                    .collection("books")
                    .document(book.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(holder.itemView.getContext(), "Könyv törölve", Toast.LENGTH_SHORT).show();
                        bookList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, bookList.size());
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "Hiba: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        Animation animation = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.fade_in);
        holder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

    static class BookViewHolder extends RecyclerView.ViewHolder {

        Button btnDelete;
        TextView txtTitle, txtAuthor, txtPrice, txtDescription;

        public BookViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtAuthor = itemView.findViewById(R.id.txtAuthor);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
