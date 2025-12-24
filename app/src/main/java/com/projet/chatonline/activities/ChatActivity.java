package com.projet.chatonline.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.projet.chatonline.R;
import com.projet.chatonline.adapters.ChatAdapter;
import com.projet.chatonline.models.Message;

import java.util.*;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSend, btnBack;
    private ChatAdapter adapter;
    private List<Message> messages = new ArrayList<>();

    private String currentUid, otherUid, chatId;
    private FirebaseFirestore db;

    private static final String TAG = "ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // --- UI ---
        rvChat = findViewById(R.id.rv_chat);
        etMessage = findViewById(R.id.et_message);
        btnSend = findViewById(R.id.btn_send);
        btnBack = findViewById(R.id.btn_back);

        // Vérifier utilisateur connecté
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Utilisateur non connecté", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        otherUid = getIntent().getStringExtra("otherUid");

        if (TextUtils.isEmpty(otherUid)) {
            Toast.makeText(this, "Erreur : UID utilisateur manquant", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        chatId = getChatId(currentUid, otherUid);

        // --- RecyclerView ---
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rvChat.setLayoutManager(lm);

        adapter = new ChatAdapter(this, messages, currentUid);
        rvChat.setAdapter(adapter);

        // --- Load user + messages ---
        loadUserInfo(otherUid);
        loadMessages();

        // --- Actions ---
        btnSend.setOnClickListener(v -> sendTextMessage());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    // ============================================================
    // Charger informations utilisateur
    // ============================================================
    private void loadUserInfo(String uid) {
        TextView tvUsername = findViewById(R.id.tv_username);
        TextView tvStatus = findViewById(R.id.tv_status);
        ImageView imgProfile = findViewById(R.id.img_profile);

        db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
            if (doc == null || !doc.exists()) return;

            tvUsername.setText(doc.getString("displayName"));

            String avatar = doc.getString("avatarUrl");
            if (avatar != null && !avatar.isEmpty()) {
                Glide.with(this).load(avatar).circleCrop().into(imgProfile);
            }

            Long lastSeen = doc.getLong("lastSeen");
            if (lastSeen == null) return;

            long diff = System.currentTimeMillis() - lastSeen;

            if (diff < 120000) {
                tvStatus.setText("En ligne");
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            } else {
                tvStatus.setText("Hors ligne");
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        });
    }

    // ============================================================
    // Envoi message texte
    // ============================================================
    private void sendTextMessage() {
        String txt = etMessage.getText().toString().trim();
        if (txt.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", currentUid);
        msg.put("receiverId", otherUid);
        msg.put("text", txt);
        msg.put("timestamp", FieldValue.serverTimestamp());
        msg.put("type", "text");

        db.collection("chats").document(chatId).collection("messages")
                .add(msg)
                .addOnSuccessListener(ref -> {
                    updateChatMeta(txt);
                    etMessage.setText("");
                })
                .addOnFailureListener(e -> Log.e(TAG, "Erreur envoi message : " + e.getMessage()));
    }

    // ============================================================
    // Mise à jour des métadonnées du chat (dernier message)
    // ============================================================
    private void updateChatMeta(String lastMsg) {
        Map<String, Object> meta = new HashMap<>();
        meta.put("lastMessage", lastMsg);
        meta.put("lastTimestamp", FieldValue.serverTimestamp());
        meta.put("participants", Arrays.asList(currentUid, otherUid));

        db.collection("chats").document(chatId).set(meta, SetOptions.merge());
    }

    // ============================================================
    // Charger messages Firestore en temps réel
    // ============================================================
    private void loadMessages() {
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (snap == null) return;

                    messages.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Message m = doc.toObject(Message.class);
                        if (m != null) {
                            m.setId(doc.getId());
                            m.setChatId(chatId);

                            if (m.getTimestamp() == null) {
                                m.setTimestamp(new com.google.firebase.Timestamp(System.currentTimeMillis() / 1000, 0));
                            }

                            messages.add(m);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    rvChat.scrollToPosition(messages.size() - 1);
                });
    }

    // ============================================================
    // Génère l'identifiant unique du chat
    // ============================================================
    private String getChatId(String a, String b) {
        return (a.compareTo(b) < 0) ? a + "_" + b : b + "_" + a;
    }

    // ============================================================
    // Mise à jour du statut "lastSeen"
    // ============================================================

    @Override
    protected void onResume() {
        super.onResume();
        db.collection("users").document(currentUid).update("lastSeen", System.currentTimeMillis());
        startOnlineStatusUpdater();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.collection("users").document(currentUid).update("lastSeen", System.currentTimeMillis());
        stopOnlineStatusUpdater();
    }

    private android.os.Handler handler = new android.os.Handler();
    private Runnable runnable;

    private void startOnlineStatusUpdater() {
        if (runnable != null) return;

        runnable = () -> {
            db.collection("users").document(currentUid).update("lastSeen", System.currentTimeMillis());
            handler.postDelayed(runnable, 30000);
        };
        handler.post(runnable);
    }

    private void stopOnlineStatusUpdater() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }
}
