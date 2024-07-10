package com.example.scannerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.scannerapp.UserModel.User_detail_model;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;

public class morining_check_in extends AppCompatActivity {
    private Button take_photo,submit;
    private TextView opening_stock;
    private Bitmap morning_check_in_image;
    private ProgressBar progressBar;
    private String phone;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_morining_check_in);
        take_photo = findViewById(R.id.take_photo);
        submit = findViewById(R.id.submit);
        opening_stock = findViewById(R.id.opening_stock);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(morining_check_in.this, loginActivity.class);
            Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
        phone = mAuth.getCurrentUser().getPhoneNumber();
        storage = FirebaseStorage.getInstance();
        take_photo.setOnClickListener(v -> {
            takePhotoIntent(take_photo_intent);
        });
        submit.setOnClickListener(v->{
            if(opening_stock.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Please enter opening stock", Toast.LENGTH_SHORT).show();
                return;
            }
            if(morning_check_in_image == null){
                Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_SHORT).show();
                return;
            }
            handle_morning_check_in();
        });
    }
    private void takePhotoIntent(ActivityResultLauncher<Intent> resultLauncher){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            resultLauncher.launch(intent);
        }
    }
    private final ActivityResultLauncher<Intent> take_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    morning_check_in_image = (Bitmap) result.getData().getExtras().get("data");
                    Toast.makeText(getApplicationContext(), "Photo taken", Toast.LENGTH_LONG).show();
                }
            }
    );
    private void handle_morning_check_in() {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(date).document(phone);
        String path = "Daily_Info/" + phone + "/" + date + "/" + "morning_check_in" + ".jpg";

        uploadPhoto(path, morning_check_in_image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        Toast.makeText(getApplicationContext(), "Already signed in for today", Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        User_detail_model user = new User_detail_model(
                                LocalTime.now().toString(), "null", "null", "null",
                                opening_stock.getText().toString(), "null", "null", "null", "null", "null"
                        );
                        ref.set(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                                Log.e("FirestoreError", "Error setting document", task1.getException());
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                    Log.e("FirestoreError", "Error getting document", task.getException());
                }
            });
        }, e -> {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_LONG).show();
            Log.e("UploadError", "Error uploading photo", e);
        });
    }
    private void uploadPhoto(String path, Bitmap image, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        if (path.isEmpty() || image == null) {
            Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference ref = storage.getReference().child(path);
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, arr);
        UploadTask task = ref.putBytes(arr.toByteArray());
        task.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }
}