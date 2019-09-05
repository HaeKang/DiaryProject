package com.example.diaryproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.diaryproject.sign.SignInActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Handler hd = new Handler();
        hd.postDelayed(new splashhandler(), 3000);
    }

    private class splashhandler implements Runnable{
        public void run(){
            startActivity(new Intent(getApplication(), SignInActivity.class));
            StartActivity.this.finish();
        }
    }
}
