package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.diaryproject.R;

import org.w3c.dom.Text;

public class NoteSendActivity extends AppCompatActivity {
    private String user_nick;
    private String comment_nick;
    private String wirter_nick;


    private TextView title;
    private Button okbtn;
    private EditText content;


    private String content_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_send);

        Intent getIntent = getIntent();
        user_nick = getIntent.getExtras().getString("USERNICK");
        comment_nick = getIntent.getExtras().getString("COMMENTNICK");
        wirter_nick = getIntent.getExtras().getString("WRITENICK");


        title = findViewById(R.id.titleText);
        okbtn = findViewById(R.id.noteOk_btn);
        content = findViewById(R.id.noteText);

        if(comment_nick != null){
            title.setText(comment_nick + " 님에게 쪽지를 보냅니다.");
        }
        else if(wirter_nick != null){
            title.setText(wirter_nick + " 님에게 쪽지를 보냅니다.");
        }


        okbtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                content_text = content.getText().toString();


            }
        });


    }
}
