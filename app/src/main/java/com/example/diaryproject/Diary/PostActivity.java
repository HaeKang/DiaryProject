package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.example.diaryproject.R;

public class PostActivity extends AppCompatActivity {

    private int postid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent GetIntent = getIntent();
        postid = GetIntent.getExtras().getInt("POSTID");


    }
}
