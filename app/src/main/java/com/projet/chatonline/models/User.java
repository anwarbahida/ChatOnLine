package com.projet.chatonline.models;

public class User {
    private String uid;
    private String displayName;
    private String email;
    private String avatarUrl;
    private String fcmToken;
    private long lastSeen;

    public User() {}

    public User(String uid, String displayName, String email, String avatarUrl, String fcmToken, long lastSeen) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.fcmToken = fcmToken;
        this.lastSeen = lastSeen;
    }

    // getters & setters
    public String getUid(){ return uid; }
    public void setUid(String uid){ this.uid = uid; }
    public String getDisplayName(){ return displayName; }
    public void setDisplayName(String displayName){ this.displayName = displayName; }
    public String getEmail(){ return email; }
    public void setEmail(String email){ this.email = email; }
    public String getAvatarUrl(){ return avatarUrl; }
    public void setAvatarUrl(String avatarUrl){ this.avatarUrl = avatarUrl; }
    public String getFcmToken(){ return fcmToken; }
    public void setFcmToken(String fcmToken){ this.fcmToken = fcmToken; }
    public long getLastSeen(){ return lastSeen; }
    public void setLastSeen(long lastSeen){ this.lastSeen = lastSeen; }
}
