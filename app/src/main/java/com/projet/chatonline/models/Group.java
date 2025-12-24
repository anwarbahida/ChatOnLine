package com.projet.chatonline.models;

import java.util.List;

public class Group {

    private String id;
    private String name;
    private String description;
    private String creatorUid;
    private List<String> members;
    private long createdAt;

    public Group() {}

    public Group(String id, String name, String description,
                 String creatorUid, List<String> members, long createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.creatorUid = creatorUid;
        this.members = members;
        this.createdAt = createdAt;
    }


    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCreatorUid() { return creatorUid; }
    public void setCreatorUid(String creatorUid) { this.creatorUid = creatorUid; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
