package com.projet.chatonline.models;

import com.google.firebase.Timestamp;

public class Message {
    private String senderId;
    private String id;
    private String chatId;
    private String receiverId;
    private String text;
    private String imageUrl;
    private String type;
    private Timestamp timestamp;

    // 🔹 Constructeur vide requis pour Firestore
    public Message() {}

    // 🔹 Constructeur utile pour créer manuellement un message
    public Message(String senderId, String receiverId, String text, String type, Timestamp timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.type = type;
        this.timestamp = timestamp;
    }

    // === Getters & Setters ===
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }


    // 🔹 Getter / Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }
}
