package com.example.diaryproject.Fragment;

import java.util.Date;

import androidx.recyclerview.widget.RecyclerView;

public class Data {

    private String title;
    private String nickname;
    private String postid;
    private String date;

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getPostid(){
        return postid;
    }

    public void setPostid(String postid){
        this.postid = postid;
    }

    public String getDate(){ return date;}

    public void setDate(String date) { this.date = date;}

}

