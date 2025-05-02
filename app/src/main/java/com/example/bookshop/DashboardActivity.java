package com.example.bookshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class DashboardActivity extends AppCompatActivity {

    private TextView txtEmail;
    private Button btnLogout, btnAddBook, btnBookList;
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "bookshop_channel";
    private static final int REQUEST_NOTIFICATION_PERMISSION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupListeners();
        showUserEmail();
        createNotificationChannel();
        checkNotificationPermissionAndSend();
    }

    private void checkNotificationPermissionAndSend() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION_PERMISSION);
            } else {
                scheduleNotification();
            }
        } else {
            scheduleNotification();
        }
    }

    private void scheduleNotification() {
        Handler handler = new Handler();
        handler.postDelayed(this::sendNotification, 5000);
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Üdv a BookShop-ban!")
                .setContentText("Böngéssz most az új könyveink között 📚!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "BookShop Channel";
            String description = "Channel for BookShop notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scheduleNotification();
            }
        }
    }

    private void initViews() {
        txtEmail = findViewById(R.id.txtEmail);
        btnLogout = findViewById(R.id.btnLogout);
        btnAddBook = findViewById(R.id.btnAddBook);
        btnBookList = findViewById(R.id.btnBookList);
    }

    private void setupListeners() {
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        btnAddBook.setOnClickListener(v ->
                startActivity(new Intent(this, AddBookActivity.class))
        );

        btnBookList.setOnClickListener(v ->
                startActivity(new Intent(this, BookListActivity.class))
        );
    }

    private void showUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            txtEmail.setText("Helló, " + user.getEmail());
        } else {
            txtEmail.setText("Nem bejelentkezett felhasználó");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        showUserEmail();
    }
}
