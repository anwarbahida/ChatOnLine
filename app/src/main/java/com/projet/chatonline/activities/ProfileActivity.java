package com.projet.chatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.projet.chatonline.R;
import com.projet.chatonline.models.User;

public class ProfileActivity extends AppCompatActivity {

    private EditText etProfileName;
    private TextView tvProfileEmail;
    private ImageView ivProfileAvatar;
    private Button btnSave;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // 🔹 Firebase
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // 🔹 Views Profil
        etProfileName = findViewById(R.id.et_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        ivProfileAvatar = findViewById(R.id.iv_profile_avatar);
        btnSave = findViewById(R.id.btn_profile_save);

        // 🔹 Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        btnMenu = findViewById(R.id.btn_menu);

        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        setupDrawerMenu();
        loadDrawerHeader();
        loadUserProfile();

        btnSave.setOnClickListener(v -> updateProfile());
    }

    // 🔹 Charger les infos profil
    private void loadUserProfile() {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user == null) return;

                        etProfileName.setText(user.getDisplayName());
                        tvProfileEmail.setText(user.getEmail());

                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(user.getAvatarUrl())
                                    .into(ivProfileAvatar);
                        }
                    }
                });

    }

    // 🔹 Mettre à jour le nom
    private void updateProfile() {
        String name = etProfileName.getText().toString().trim();

        if (name.isEmpty()) {
            etProfileName.setError("Nom obligatoire");
            return;
        }

        db.collection("users").document(uid)
                .update("displayName", name)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Profil mis à jour", Toast.LENGTH_SHORT).show()
                );
        loadUserProfile();
    }

    // 🔹 Menu latéral
    private void setupDrawerMenu() {
        navigationView.setCheckedItem(R.id.nav_profile);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_main) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }

            if (id == R.id.nav_profile) {
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }


            if (id == R.id.nav_users) {
                startActivity(new Intent(this, UsersActivity.class));
            }
             if (id == R.id.nav_groups) {
                 startActivity(new Intent(this, GroupsActivity.class));
             }

            if (id == R.id.nav_create_group) {
                Toast.makeText(this, "Créer un groupe", Toast.LENGTH_SHORT).show();
            }

            if (id == R.id.btn_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // 🔹 Header du Drawer
    private void loadDrawerHeader() {
        TextView tvUsernameHeader =
                navigationView.getHeaderView(0).findViewById(R.id.tv_username_header);
        ImageView imgUser =
                navigationView.getHeaderView(0).findViewById(R.id.img_user);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvUsernameHeader.setText(doc.getString("displayName"));

                        String avatarUrl = doc.getString("avatarUrl");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).into(imgUser);
                        }
                    }
                });
    }
}
