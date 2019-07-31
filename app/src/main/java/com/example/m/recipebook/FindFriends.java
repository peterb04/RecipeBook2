package com.example.m.recipebook;

public class FindFriends {

    public String profileimage, username;

    public FindFriends(){

    }

    public FindFriends(String profileimage, String username) {
        this.profileimage = profileimage;
        this.username = username;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
