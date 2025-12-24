package com.projet.chatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.projet.chatonline.R;
import com.projet.chatonline.adapters.FriendAdapter;
import com.projet.chatonline.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    private static final String TAG = "CreateGroupActivity";

    private EditText etGroupName, etGroupDesc;
    private Button btnCreateGroup;
    private RecyclerView rvFriends;
    private FriendAdapter friendAdapter;
    private List<User> friendsList = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_create_group);

            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db = FirebaseFirestore.getInstance();

            etGroupName = findViewById(R.id.et_group_name);
            etGroupDesc = findViewById(R.id.et_group_desc);
            btnCreateGroup = findViewById(R.id.btn_create_group);
            rvFriends = findViewById(R.id.rv_friends);

            // ⚡ Important : donner une hauteur fixe ou wrap_content dans XML pour RecyclerView
            rvFriends.setLayoutManager(new LinearLayoutManager(this));
            friendAdapter = new FriendAdapter(this, friendsList);
            rvFriends.setAdapter(friendAdapter);

            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.navigation_view);
            btnMenu = findViewById(R.id.btn_menu);

            btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

            setupDrawerMenu();
            loadDrawerHeader();
            loadFriends();

            btnCreateGroup.setOnClickListener(v -> createGroup());

        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreate", e);
            Toast.makeText(this, "Erreur lors du chargement de l'activité", Toast.LENGTH_LONG).show();
        }
    }

    private void loadFriends() {
        try {
            db.collection("users").get()
                    .addOnSuccessListener(query -> {
                        friendsList.clear();
                        for (DocumentSnapshot doc : query.getDocuments()) {
                            User u = doc.toObject(User.class);
                            if (u != null && !u.getUid().equals(uid)) {
                                friendsList.add(u);
                            }
                        }
                        friendAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur chargement amis", e);
                        Toast.makeText(this, "Impossible de charger les amis", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur loadFriends", e);
        }
    }

    private void createGroup() {
        try {
            String name = etGroupName.getText().toString().trim();
            String desc = etGroupDesc.getText().toString().trim();

            if (name.isEmpty()) {
                etGroupName.setError("Le nom du groupe est obligatoire");
                return;
            }

            List<String> selectedUids = new ArrayList<>(friendAdapter.getSelectedUids());
            selectedUids.add(uid); // ajouter le créateur

            Map<String, Object> group = new HashMap<>();
            group.put("name", name);
            group.put("description", desc);
            group.put("creatorUid", uid);
            group.put("members", selectedUids);
            group.put("createdAt", System.currentTimeMillis());

            db.collection("groups").add(group)
                    .addOnSuccessListener(docRef -> {
                        Toast.makeText(this, "Groupe créé avec succès", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur création groupe", e);
                        Toast.makeText(this, "Erreur lors de la création du groupe", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur createGroup", e);
        }
    }

    private void setupDrawerMenu() {
        try {
            navigationView.setCheckedItem(R.id.nav_create_group);
            navigationView.setNavigationItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.nav_main) startActivity(new Intent(this, MainActivity.class));
                if (id == R.id.nav_profile) startActivity(new Intent(this, ProfileActivity.class));
                if (id == R.id.btn_logout) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            });
        } catch (Exception e) {
            Log.e(TAG, "Erreur setupDrawerMenu", e);
        }
    }

    private void loadDrawerHeader() {
        try {
            TextView tvUsernameHeader = navigationView.getHeaderView(0).findViewById(R.id.tv_username_header);
            ImageView imgUser = navigationView.getHeaderView(0).findViewById(R.id.img_user);

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            tvUsernameHeader.setText(doc.getString("username"));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Erreur loadDrawerHeader", e));
        } catch (Exception e) {
            Log.e(TAG, "Erreur loadDrawerHeader", e);
        }
    }
}
