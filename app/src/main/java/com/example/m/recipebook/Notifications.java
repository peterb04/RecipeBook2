package com.example.m.recipebook;

public class Notifications {

    public int type, data;
    public static final int COMMENT_TYPE = 0, FRIEND_REQUEST_TYPE = 1;
    public String date;
    public String description;
    public String profileImage;
    public String recipeMainImage;
    public String time;
    public String title;
    public String uid;
    public String username;
    public String postKey;
    public String commenterID;

    public Notifications(){

    }

    public Notifications(int type, int data, String date, String description, String profileImage, String recipeMainImage, String time, String title, String uid, String username, String postKey, String commenterID) {
        this.type = type;
        this.data = data;
        this.date = date;
        this.description = description;
        this.profileImage = profileImage;
        this.recipeMainImage = recipeMainImage;
        this.time = time;
        this.title = title;
        this.uid = uid;
        this.username = username;
        this.postKey = postKey;
        this.commenterID = commenterID;
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

    public static int getCommentType() {
        return COMMENT_TYPE;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getRecipeMainImage() {
        return recipeMainImage;
    }

    public void setRecipeMainImage(String recipeMainImage) {
        this.recipeMainImage = recipeMainImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPostKey() {
        return postKey;
    }

    public void setPostKey(String postKey) {
        this.postKey = postKey;
    }

    public String getCommenterID() {
        return commenterID;
    }

    public void setCommenterID(String commenterID) {
        this.commenterID = commenterID;
    }

}
