package com.example.m.recipebook;

public class Comments {

    public String comment, date, time, username, profileImage, uid, nodeID;
    public static final int COMMENTS_HEADER_TYPE = 0;
    public int type, data;

    public Comments(){

    }

    public Comments(int type, int data, String comment, String date, String time, String username, String profileImage, String uid, String nodeID) {
        this.comment = comment;
        this.date = date;
        this.time = time;
        this.username = username;
        this.type = type;
        this.data = data;
        this.profileImage = profileImage;
        this.uid = uid;
        this.nodeID = nodeID;

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNodeID() {
        return nodeID;
    }

    public void setNodeID(String nodeID) {
        this.nodeID = nodeID;
    }
}
