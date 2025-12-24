package com.projet.chatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.projet.chatonline.R;
import com.projet.chatonline.adapters.UserAdapter;
import com.projet.chatonline.models.User;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView rvUsers;
    private UserAdapter userAdapter;
    private final List<User> users = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 🔹 RecyclerView utilisateurs
        rvUsers = findViewById(R.id.rv_users);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, users, user -> {
            Intent i = new Intent(UsersActivity.this, ChatActivity.class);
            i.putExtra("otherUid", user.getUid());
            i.putExtra("otherName", user.getDisplayName());
            startActivity(i);
        });
        rvUsers.setAdapter(userAdapter);

        // 🔹 DrawerLayout
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        ImageButton btnMenu = findViewById(R.id.btn_menu);

        btnMenu.setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START)
        );

        // 🔹 Navigation menu
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_create_group) {
                startActivity(new Intent(this, CreateGroupActivity.class));
            } else if (id == R.id.nav_groups) {
                startActivity(new Intent(this, GroupsActivity.class));
            } else if (id == R.id.btn_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // 🔹 Header drawer (profil)
        View headerView = navigationView.getHeaderView(0);
        TextView tvUsernameHeader = headerView.findViewById(R.id.tv_username_header);
        ImageView imgUser = headerView.findViewById(R.id.img_user);

        db.collection("users").document(currentUid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("displayName");
                        String avatarUrl = doc.getString("avatarUrl");

                        tvUsernameHeader.setText(name != null ? name : "Utilisateur");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(this).load(avatarUrl).into(imgUser);
                        }
                    }
                });

        // 🔹 Charger utilisateurs
        loadUsers();
    }

    private void loadUsers() {
        db.collection("users")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) return;

                    users.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        User u = doc.toObject(User.class);
                        if (u != null && !u.getUid().equals(currentUid)) {
                            users.add(u);
                        }
                    }
                    userAdapter.notifyDataSetChanged();
                });
    }
}
