package com.example.m.recipebook;

public class Friends {

    public String date;
    public String uid;
    public String username;
    public String userimage;
    public int type;
    public static final int Friends_Type = 0;

    public Friends(){
    }


    public static int getFriends_Type() {
        return Friends_Type;
    }



    public Friends(int type, String date, String uid, String username, String userimage) {
        this.type = type;
        this.date = date;
        this.uid = uid;
        this.username = username;
        this.userimage = userimage;

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public String getUserimage() {
        return userimage;
    }

    public void setUserimage(String userimage) {
        this.userimage = userimage;
    }
}
