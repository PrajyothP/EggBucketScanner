package com.example.scannerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scannerapp.UserModel.User_detail_model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private Bitmap morning_check_in_image , morning_check_out_image , evening_check_in_image , evening_check_out_image;
    private ProgressBar progressBar;
    private Button morning_check_in , morning_check_out , evening_check_in , evening_check_out;
    private TextView eggs_left , money_collected , opening_stock;
    private FirebaseAuth mAuth;
    private String phone;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_incheck_out);
        morning_check_in = findViewById(R.id.Morning_check_in);
        morning_check_out = findViewById(R.id.Morning_check_out);
        evening_check_in = findViewById(R.id.Evening_check_in);
        evening_check_out = findViewById(R.id.Evening_check_out);
        opening_stock = findViewById(R.id.Opening_stock);
        eggs_left = findViewById(R.id.eggs_left);
        money_collected = findViewById(R.id.money_collected);
        //disable_inputs();
        progressBar = findViewById(R.id.progressBar);
        storage = FirebaseStorage.getInstance();
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(checkIncheckOut.this, loginActivity.class);
            Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
        phone = mAuth.getCurrentUser().getPhoneNumber();
        //check_enable_inputs();
        morning_check_in.setOnClickListener(v -> {
            takePhoto("check_in");
        });
        morning_check_out.setOnClickListener(v -> {
            takePhoto("check_out");
        });

    }
    private void takePhoto(String info) {
        switch (info) {
            case "morning_check_in" -> takePhotoIntent(morning_check_in_photo_intent);
            case "morning_check_out" -> takePhotoIntent(morning_check_out_photo_intent);
            //case "evening_check_in" -> takePhotoIntent(check_in_photo_intent);
            //case "evening_check_out" -> takePhotoIntent(check_out_photo_intent);
            default -> {
                Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void takePhotoIntent(ActivityResultLauncher<Intent> resultLauncher) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            resultLauncher.launch(intent);
        }
    }

    private final ActivityResultLauncher<Intent> morning_check_in_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    morning_check_in_image = (Bitmap) result.getData().getExtras().get("data");
                    handle_check_in("morning_check_in",morning_check_in_image);
                }
            }
    );

    private final ActivityResultLauncher<Intent> morning_check_out_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    morning_check_out_image = (Bitmap) result.getData().getExtras().get("data");
                    handle_check_out("morning_check_out",morning_check_out_image);
                }
            }
    );
    private void handle_check_in(String time,Bitmap image) {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(phone).document(date);
        String path = "Daily_Info/" + phone + "/" + date + time + ".jpg";

        uploadPhoto(path, image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (!Objects.equals(task.getResult().getString("morning_check_in_time"), "null")) {
                            Toast.makeText(getApplicationContext(), "Already checked in for morning", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(!Objects.equals(task.getResult().getString("morning_check_out_time"), "null")){
                            Toast.makeText(getApplicationContext(), "Already checked in for evening", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else if(Objects.equals(task.getResult().getString("evening_check_in_time"), "null")){
                            Map<String, Object> map = new HashMap<>();
                            map.put("evening_check_in_time", LocalTime.now().toString());
                            map.put("evening_opening_stock",opening_stock.getText());
                            ref.update(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Checked in for evening", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                }
            });
        }, err -> {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_SHORT).show();
        });
    }

    private void handle_check_out(String time,Bitmap image) {
        progressBar.setVisibility(View.VISIBLE);
        String money = money_collected.getText().toString();
        String eggs = eggs_left.getText().toString();
        if (money.isEmpty() || eggs.isEmpty()) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Please enter required data", Toast.LENGTH_SHORT).show();
            return;
        }
        String regex = "^[0-9]+$";
        if (!money.matches(regex) || !eggs.matches(regex)) {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Please enter valid data", Toast.LENGTH_SHORT).show();
            return;
        }
        String date = LocalDate.now().toString();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(phone).document(date);
        String path = "Daily_Info/" + phone + "/" + date + time + ".jpg";

        uploadPhoto(path, image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (!Objects.equals(task.getResult().getString("check_out_time"), "null")) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Already checked out", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("check_out_time", LocalTime.now().toString());
                            map.put("money_collected", money);
                            map.put("eggs_left", eggs);
                            map.put("outlet", "placeHolder");
                            ref.update(map).addOnCompleteListener(task1 -> {
                                progressBar.setVisibility(View.INVISIBLE);
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Checked out", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to check out", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Not signed in", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
            });
        }, e -> {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_SHORT).show();
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
    private void disable_inputs(){
        eggs_left.setEnabled(false);
        money_collected.setEnabled(false);
        opening_stock.setEnabled(false);
        eggs_left.setText("");
        money_collected.setText("");
        opening_stock.setText("");
    }
    private void check_enable_inputs() {
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference ref = FirebaseFirestore.getInstance().collection(phone).document(LocalDate.now().toString());
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                    eggs_left.setEnabled(true);
                    money_collected.setEnabled(true);
                }
                else{

                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
}
