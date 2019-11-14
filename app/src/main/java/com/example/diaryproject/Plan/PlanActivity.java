package com.example.diaryproject.Plan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.R;

import java.util.ArrayList;
import java.util.List;

public class PlanActivity extends AppCompatActivity {

    String user_id;
    String user_nick;
    String select_date;

    ArrayList<String> content_list;
    ArrayList<String> idx_list;


    TextView title;
    TextView content;
    Button add_btn;
    Button delete_btn;

    // 일정 db에 넣고 selectmain으로 이동 이거 팝업창으로 할까? https://ghj1001020.tistory.com/9

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_plan);

        Intent intent = getIntent();
        user_id = intent.getExtras().getString("user_id");
        user_nick = intent.getExtras().getString("user_nickname");
        select_date = intent.getExtras().getString("date");
        content_list = intent.getExtras().getStringArrayList("content");
        idx_list = intent.getExtras().getStringArrayList("idx");


        title = findViewById(R.id.plan_title_text);
        title.setText(user_nick + "님의 \n" + select_date + " 일정");


        content = findViewById(R.id.plan_content_text);

        if(content_list == null){
            content.setText("없음");
        } else{
            String content_text = "";
            for (int i=0; i<content_list.size(); i++){
                content_text += (i+1) + ". " + content_list.get(i) + "\n";
            }
            content.setText(content_text);
        }


        add_btn = findViewById(R.id.plan_add_btn);
        delete_btn = findViewById(R.id.plan_delete_btn);

        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 일정추가 activity로 이동
                Intent intent = new Intent(PlanActivity.this, PlanAddActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nick", user_nick);
                intent.putExtra("select_date",select_date);
                startActivity(intent);
                finish();
            }
        });

        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 삭제 db
                if(content_list == null){
                    Toast.makeText(PlanActivity.this, "삭제할 일정이 없습니다!", Toast.LENGTH_LONG).show();
                }else {
                    Intent intent = new Intent(PlanActivity.this, PlanDeleteActivity.class);
                    intent.putExtra("user_id", user_id);
                    intent.putExtra("user_nick", user_nick);
                    intent.putExtra("content",content_list);
                    intent.putExtra("idx",idx_list);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    public void onBackPressed(){
        finish();
    }
}
