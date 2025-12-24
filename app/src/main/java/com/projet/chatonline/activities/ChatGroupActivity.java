package com.projet.chatonline.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.projet.chatonline.R;
import com.projet.chatonline.adapters.ChatGroupAdapter;
import com.projet.chatonline.models.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatGroupActivity extends AppCompatActivity {

    // UI
    private TextView tvGroupName, tvGroupInfo;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend, btnBack;

    // Data
    private ChatGroupAdapter messageAdapter;
    private final List<Message> messageList = new ArrayList<>();

    private FirebaseFirestore db;
    private String groupId;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_group);

        // 🔹 Intent
        groupId = getIntent().getStringExtra("groupId");
        String groupName = getIntent().getStringExtra("groupName");

        // 🔹 Vérifications critiques
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Veuillez vous connecter pour accéder au chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (groupId == null) {
            Toast.makeText(this, "Groupe introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUid = auth.getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // 🔹 Bind UI
        tvGroupName = findViewById(R.id.tv_group_name);
        tvGroupInfo = findViewById(R.id.tv_group_info);
        rvMessages = findViewById(R.id.rv_group_messages);
        etMessage = findViewById(R.id.et_group_message);
        btnSend = findViewById(R.id.btn_send_group);
        btnBack = findViewById(R.id.btn_back_group);

        // 🔹 Header
        tvGroupName.setText(groupName != null ? groupName : "Groupe");
        tvGroupInfo.setText("Discussion de groupe");

        // 🔹 Bouton retour
        btnBack.setOnClickListener(v -> finish());

        // 🔹 RecyclerView
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // messages en bas
        rvMessages.setLayoutManager(lm);

        messageAdapter = new ChatGroupAdapter(this, messageList, currentUid);
        rvMessages.setAdapter(messageAdapter);

        // 🔹 Charger messages
        loadMessages();

        // 🔹 Envoyer message
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        if (groupId == null) return;

        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(this, (value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    messageList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Message msg = doc.toObject(Message.class);
                            messageList.add(msg);
                        }
                    }

                    messageAdapter.notifyDataSetChanged();
                    if (messageList.size() > 0)
                        rvMessages.scrollToPosition(messageList.size() - 1);
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();

        if (TextUtils.isEmpty(text)) return;
        if (currentUid == null || groupId == null) {
            Toast.makeText(this, "Impossible d'envoyer le message", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUid);
        message.put("text", text);
        message.put("timestamp", System.currentTimeMillis());

        db.collection("groups")
                .document(groupId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(docRef -> etMessage.setText(""))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erreur réseau", Toast.LENGTH_SHORT).show()
                );
    }
}
