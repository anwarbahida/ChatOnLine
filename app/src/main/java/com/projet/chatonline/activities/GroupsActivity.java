package com.projet.chatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.projet.chatonline.adapters.GroupAdapter;
import com.projet.chatonline.models.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupsActivity extends AppCompatActivity {

    private static final String TAG = "GroupsActivity";

    private RecyclerView rvGroups;
    private GroupAdapter groupAdapter;
    private List<Group> groupsList = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenu;

    private FirebaseFirestore db;
    private String uid;
    private List<String> groupIds = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_groups);

            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db = FirebaseFirestore.getInstance();

            // 🔹 UI
            rvGroups = findViewById(R.id.rv_groups);
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.navigation_view);
            btnMenu = findViewById(R.id.btn_menu);

            rvGroups.setLayoutManager(new LinearLayoutManager(this));

            // 🔹 Adapter
            groupAdapter = new GroupAdapter(this, groupsList, (group, groupId) -> {
                Intent intent = new Intent(GroupsActivity.this, ChatGroupActivity.class);
                intent.putExtra("groupId", groupId);
                intent.putExtra("groupName", group.getName());
                startActivity(intent);
            });
            rvGroups.setAdapter(groupAdapter);

            // 🔹 Drawer
            btnMenu.setOnClickListener(v ->
                    drawerLayout.openDrawer(GravityCompat.START)
            );

            setupDrawerMenu();
            loadDrawerHeader();
            loadGroups();

        } catch (Exception e) {
            Log.e(TAG, "Erreur onCreate", e);
            Toast.makeText(this, "Erreur chargement groupes", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 🔹 Charger les groupes où l'utilisateur est membre
     */
    private void loadGroups() {
        db.collection("groups")
                .addSnapshotListener((snap, e) -> {
                    if (e != null || snap == null) {
                        Log.e(TAG, "Erreur chargement groupes", e);
                        return;
                    }

                    groupsList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Group group = doc.toObject(Group.class);
                        if (group != null && group.getMembers().contains(uid)) {
                            groupsList.add(group);
                            groupIds.add(doc.getId());
                        }
                    }
                    groupAdapter.notifyDataSetChanged();
                });
    }

    /**
     * 🔹 Menu latéral
     */
    private void setupDrawerMenu() {
        navigationView.setCheckedItem(R.id.nav_groups);
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();

            if (id == R.id.nav_main)
                startActivity(new Intent(this, MainActivity.class));

            else if (id == R.id.nav_profile)
                startActivity(new Intent(this, ProfileActivity.class));

            else if (id == R.id.nav_create_group)
                startActivity(new Intent(this, CreateGroupActivity.class));

            else if (id == R.id.nav_users)
                startActivity(new Intent(this, UsersActivity.class));

            else if (id == R.id.btn_logout) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    /**
     * 🔹 Header du drawer
     */
    private void loadDrawerHeader() {
        TextView tvUsernameHeader = navigationView.getHeaderView(0)
                .findViewById(R.id.tv_username_header);
        ImageView imgUser = navigationView.getHeaderView(0)
                .findViewById(R.id.img_user);

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        tvUsernameHeader.setText(doc.getString("username"));
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Erreur header drawer", e)
                );
    }
}
