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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class checkIncheckOut extends AppCompatActivity {

    private Button morning_check_in, morning_check_out, evening_check_in, evening_check_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_incheck_out);
        morning_check_in = findViewById(R.id.Morning_check_in);
        morning_check_out = findViewById(R.id.Morning_check_out);
        evening_check_in = findViewById(R.id.Evening_check_in);
        evening_check_out = findViewById(R.id.Evening_check_out);


        morning_check_in.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, morining_check_in.class);
            startActivity(intent);
        });
        morning_check_out.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, morning_check_out.class);
            startActivity(intent);
        });
        evening_check_in.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, evening_check_in.class);
            startActivity(intent);
        });
        evening_check_out.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, evening_check_out.class);
            startActivity(intent);
        });
    }
}