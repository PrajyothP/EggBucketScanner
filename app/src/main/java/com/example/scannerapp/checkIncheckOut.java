package com.example.scannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.scannerapp.UserModel.User_detail_model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.Date;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class checkIncheckOut extends AppCompatActivity {
    private ProgressBar progressBar;
    private Button check_in , check_out , logout;
    TextView eggs_left , money_collected;
    private FirebaseAuth mAuth;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_incheck_out);
        check_in = findViewById(R.id.check_in_btn);
        check_out = findViewById(R.id.check_out_btn);
        logout = findViewById(R.id.logout_btn);
        eggs_left = findViewById(R.id.eggs_left);
        money_collected = findViewById(R.id.money_collected);
        progressBar = findViewById(R.id.progressBar);
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()==null){
            Intent intent = new Intent(checkIncheckOut.this,loginActivity.class);
            Toast.makeText(getApplicationContext(),"Please login",Toast.LENGTH_SHORT).show();
        }
        phone = mAuth.getCurrentUser().getPhoneNumber();
        check_enable_inputs();
        logout.setOnClickListener(v ->{
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(checkIncheckOut.this, loginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        check_in.setOnClickListener(v -> {
            handle_check_in();
        });
        check_out.setOnClickListener(v->{
            handle_check_out();
        });
    }
    private void check_enable_inputs(){
        progressBar.setVisibility(View.VISIBLE);
        DocumentReference ref = FirebaseFirestore.getInstance().collection(phone).document(LocalDate.now().toString());
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful() && task.getResult()!=null && task.getResult().exists()){
                    eggs_left.setVisibility(View.VISIBLE);
                    eggs_left.setEnabled(true);
                    money_collected.setVisibility(View.VISIBLE);
                    money_collected.setEnabled(true);
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }
    private void handle_check_in(){
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(phone).document(date);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    progressBar.setVisibility(View.INVISIBLE);
                    if(task.getResult()!=null && task.getResult().exists()){
                        Toast.makeText(getApplicationContext(),"Already signed in for today",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        User_detail_model user = new User_detail_model(LocalTime.now().toString(),"null","null","null","null");
                        ref.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(),"Checked in",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(),"Failed to check in",Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),"Network error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void handle_check_out(){
        progressBar.setVisibility(View.VISIBLE);
        String money = money_collected.getText().toString();
        String eggs = eggs_left.getText().toString();
        if(money.isEmpty() || eggs.isEmpty()){
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"Please enter required data",Toast.LENGTH_SHORT).show();
            return;
        }
        String regex = "^[0-9]+$";
        if(!money.matches(regex) || !eggs.matches(regex)){
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"Please enter valid data",Toast.LENGTH_SHORT).show();
            return;
        }
        String date = LocalDate.now().toString();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(phone).document(date);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult()!=null && task.getResult().exists()){
                        if(!Objects.equals(task.getResult().getString("check_out_time"), "null")){
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),"Already checked out",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Map<String,Object> map = new HashMap<>();
                            map.put("check_out_time",LocalTime.now().toString());
                            map.put("money_collected",money);
                            map.put("eggs_left",eggs);
                            map.put("outlet","placeHolder");
                            ref.update(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(),"Checked out",Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        progressBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(getApplicationContext(),"Failed to check out",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(),"Not signed in",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(),"Network error",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}