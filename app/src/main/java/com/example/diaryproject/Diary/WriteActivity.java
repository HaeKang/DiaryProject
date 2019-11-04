package com.example.diaryproject.Diary;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class WriteActivity extends AppCompatActivity {
    private String id;
    private String nickname;
    private String title;
    private String content;
    private String private_check = "false";

    private EditText etitle;
    private EditText econtent;
    private Button writeBtn;

    private RadioGroup weather_radio;

    private CheckBox privateCheck;

    private String TAG = "PHPTEST";


    private static final int gallery = 1;
    private String temp = "";

    String pre_title;
    String pre_content;
    String weather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);


        Intent GetIntent = getIntent();
        id = GetIntent.getExtras().getString("user_id");
        nickname = GetIntent.getExtras().getString("user_nick");

        pre_title = GetIntent.getExtras().getString("title");
        pre_content = GetIntent.getExtras().getString("content");


        etitle = findViewById(R.id.title);
        econtent = findViewById(R.id.content);
        writeBtn = findViewById(R.id.write_btn);
        privateCheck = findViewById(R.id.private_check);

        weather_radio = findViewById(R.id.weather_group);


        privateCheck.setOnClickListener(new CheckBox.OnClickListener(){
            @Override
            public void onClick(View v) {
                private_check = "true";
            }
        });


        if(pre_title != null){
            etitle.setText(pre_title);
            econtent.setText(pre_content);
        }


        //라디오 그룹 클릭 리스너
        weather_radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.sun_weather:
                        weather = "맑음";
                        break;
                    case R.id.suncloud_weather:
                        weather = "해구름";
                        break;
                    case R.id.cloud_weather:
                        weather = "구름";
                        break;
                    case R.id.rain_weather:
                        weather = "비";
                        break;
                    case R.id.snow_weather:
                        weather = "눈";
                        break;
                    default:
                        break;
                }


            }
        });


        //글쓰기 버튼
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title = etitle.getText().toString();
                content = econtent.getText().toString();

                if (weather == null) {
                    Toast.makeText(getApplicationContext(), "날씨를 선택해주세요", Toast.LENGTH_LONG).show();
                } else {
                    if (pre_title == null) {

                        Write task = new Write();
                        task.execute(getString(R.string.sever) + "/Write.php", id, nickname, title, content, weather, private_check);

                        Intent intent = new Intent(WriteActivity.this, MainActivity.class);
                        intent.putExtra("user_id", id);
                        intent.putExtra("user_nickname", nickname);
                        startActivity(intent);
                        finish();

                    } else {

                        // update문
                        WriteUpdate task = new WriteUpdate();
                        task.execute(getString(R.string.sever) + "/UpdatePost.php", title, content, weather, private_check);

                        Intent intent = new Intent(WriteActivity.this, MainActivity.class);
                        intent.putExtra("user_id", id);
                        intent.putExtra("user_nickname", nickname);
                        startActivity(intent);
                        finish();

                    }
                }
            }
        });

    }



    // DB에 넣기
    class Write extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(WriteActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if(result.contains("글 추가 완성")) {
                StyleableToast.makeText(getApplicationContext(), "글 추가 완성", Toast.LENGTH_LONG, R.style.sign).show();
            } else{
                StyleableToast.makeText(getApplicationContext(), "글은 하루에 한번만 쓸 수 있습니다", Toast.LENGTH_LONG, R.style.sign).show();
            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String)params[0];
            String id = (String)params[1];
            String nick = (String)params[2];
            String title = (String)params[3];
            String content = (String)params[4];
            String weather = (String)params[5];
            String private_check = params[6];



            String postParameters = "id=" + id + "&nickname=" + nick + "&title=" + title
                    + "&content=" + content + "&weather=" + weather + "&private=" + private_check;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();
                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }

        }
    }


    // 글 수정 DB
    class WriteUpdate extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(WriteActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if(result.contains("글 수정 완료")) {
                StyleableToast.makeText(getApplicationContext(), "글 수정완료", Toast.LENGTH_LONG, R.style.sign).show();
            } else{
                StyleableToast.makeText(getApplicationContext(), "오류", Toast.LENGTH_LONG, R.style.sign).show();
            }
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String)params[0];
            String title = (String)params[1];
            String content = (String)params[2];
            String weather = (String)params[3];
            String private_check = params[4];



            String postParameters = "title=" + title + "&content=" + content + "&weather=" + weather + "&private=" + private_check;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();
                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }

        }
    }


}
