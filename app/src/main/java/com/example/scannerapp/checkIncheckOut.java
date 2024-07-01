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
    private Bitmap morning_check_in_image, morning_check_out_image, evening_check_in_image, evening_check_out_image;
    private ProgressBar progressBar;
    private Button morning_check_in, morning_check_out, evening_check_in, evening_check_out;
    private TextView eggs_left, money_collected, opening_stock;
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
        morning_check_in.setOnClickListener(v -> {
            if (opening_stock.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter opening stock", Toast.LENGTH_SHORT).show();
                return;
            }
            takePhoto("morning_check_in");
        });
        morning_check_out.setOnClickListener(v -> {
            if (eggs_left.getText().toString().isEmpty() || money_collected.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter closing stock and money collected", Toast.LENGTH_SHORT).show();
                return;
            }
            takePhoto("morning_check_out");
        });
        evening_check_in.setOnClickListener(v -> {
            if (opening_stock.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter opening stock", Toast.LENGTH_SHORT).show();
                return;
            }
            takePhoto("evening_check_in");
        });
        evening_check_out.setOnClickListener(v -> {
            if (eggs_left.getText().toString().isEmpty() || money_collected.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter closing stock and money collected", Toast.LENGTH_SHORT).show();
                return;
            }
            takePhoto("evening_check_out");
        });
    }

    private void takePhoto(String info) {
        switch (info) {
            case "morning_check_in" -> takePhotoIntent(morning_check_in_photo_intent);
            case "morning_check_out" -> takePhotoIntent(morning_check_out_photo_intent);
            case "evening_check_in" -> takePhotoIntent(evening_check_in_photo_intent);
            case "evening_check_out" -> takePhotoIntent(evening_check_out_photo_intent);
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
                    handle_morning_check_in();
                }
            }
    );

    private final ActivityResultLauncher<Intent> morning_check_out_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    morning_check_out_image = (Bitmap) result.getData().getExtras().get("data");
                    handle_morning_check_out();
                }
            }
    );
    private final ActivityResultLauncher<Intent> evening_check_in_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    evening_check_in_image = (Bitmap) result.getData().getExtras().get("data");
                    handle_evening_check_in();
                }
            }
    );
    private final ActivityResultLauncher<Intent> evening_check_out_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    evening_check_out_image = (Bitmap) result.getData().getExtras().get("data");
                    handle_evening_check_out();
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
                        Toast.makeText(getApplicationContext(), "Already signed in for today", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        User_detail_model user = new User_detail_model(
                                LocalTime.now().toString(), "null", "null", "null",
                                opening_stock.getText().toString(), "null", "null", "null", "null", "null"
                        );
                        ref.set(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                                Log.e("FirestoreError", "Error setting document", task1.getException());
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", "Error getting document", task.getException());
                }
            });
        }, e -> {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_SHORT).show();
            Log.e("UploadError", "Error uploading photo", e);
        });
    }


    private void handle_morning_check_out() {
        progressBar.setVisibility(View.VISIBLE);
        String money = money_collected.getText().toString();
        String eggs = eggs_left.getText().toString();

        String date = LocalDate.now().toString();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(date).document(phone);
        String path = "Daily_Info/" + phone + "/" + date + "/" + "morning_check_out" + ".jpg";

        uploadPhoto(path, morning_check_out_image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (Objects.equals(task.getResult().getString("morning_check_in_time"), "null")) {
                            Toast.makeText(getApplicationContext(), "Not checked in", Toast.LENGTH_SHORT).show();
                        } else if (!Objects.equals(task.getResult().getString("morning_check_out_time"), "null")) {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(), "Already checked out", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("morning_check_out_time", LocalTime.now().toString());
                            map.put("morning_money_collected", money);
                            map.put("morning_closing_stock", eggs);
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

    private void handle_evening_check_in() {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(date).document(phone);
        String path = "Daily_Info/" + phone + "/" + date + "/" + "evening_check_in" + ".jpg";
        uploadPhoto(path, evening_check_in_image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (!Objects.equals(task.getResult().getString("evening_check_in_time"), "null")) {
                            Toast.makeText(getApplicationContext(), "Already checked in", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("evening_check_in_time", LocalTime.now().toString());
                            map.put("evening_opening_stock", opening_stock.getText().toString());
                            ref.update(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        //Create new only for evening check in
                        User_detail_model user = new User_detail_model(
                                "null", "null", LocalTime.now().toString(), "null", "null", opening_stock.getText().toString(), "null", "null", "null",
                                "null"
                        );
                        ref.set(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            });
        }, e -> {
            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_SHORT).show();
        });
    }

    private void handle_evening_check_out() {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(date).document(phone);
        String path = "Daily_Info/" + phone + "/" + date + "/" + "evening_check_out" + ".jpg";
        uploadPhoto(path, evening_check_out_image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (Objects.equals(task.getResult().getString("evening_check_in_time"), "null")) {
                            Toast.makeText(getApplicationContext(), "Not checked in", Toast.LENGTH_SHORT).show();
                        } else if (!Objects.equals(task.getResult().getString("evening_check_out_time"), "null")) {
                            Toast.makeText(getApplicationContext(), "Already checked out", Toast.LENGTH_SHORT).show();
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("evening_check_out_time", LocalTime.now().toString());
                            map.put("evening_closing_stock", eggs_left.getText().toString());
                            map.put("evening_money_collected", money_collected.getText().toString());
                            ref.update(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Checked out", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to check out", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Not checked in", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            });
        }, e -> {
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
}
