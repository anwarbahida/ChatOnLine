package com.projet.chatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.projet.chatonline.R;
import com.projet.chatonline.models.*;
import com.projet.chatonline.utils.*;


public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnCreate;
    private TextView tvGoLogin;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        FirebaseApp.initializeApp(this);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email_reg);
        etPassword = findViewById(R.id.et_password_reg);
        btnCreate = findViewById(R.id.btn_register);
        tvGoLogin = findViewById(R.id.tv_go_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Bouton pour créer un compte
        btnCreate.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
                Toast.makeText(this, "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Création compte Firebase Auth
            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String uid = auth.getCurrentUser().getUid();

                    // Créer le profil utilisateur
                    User user = new User(uid, name, email, "", "", System.currentTimeMillis());

                    // Enregistrer dans Firestore
                    FirebaseUtils.saveUserProfile(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Erreur Firestore : " + e.getMessage(), Toast.LENGTH_LONG).show());

                } else {
                    Toast.makeText(this, "Erreur Auth : " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });

        // Bouton pour aller à la page login
        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }
}
