package com.example.diaryproject.Fragment.PageThree;

public class NoteData {
    private String content;
    private String nickname;
    private String date;

    public String getContent(){
        return content;
    }

    public void setContent(String title){
        this.content = title;
    }

    public String getNickname(){
        return nickname;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public String getDate(){ return date;}

    public void setDate(String date) { this.date = date;}

}
