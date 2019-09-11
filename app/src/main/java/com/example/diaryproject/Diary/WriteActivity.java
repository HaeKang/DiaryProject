package com.example.diaryproject.Diary;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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

    private EditText etitle;
    private EditText econtent;
    private Button writeBtn;
    private ImageButton imageBtn;

    private String TAG = "PHPTEST";


    private static final int gallery = 1;
    private String temp = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        tedPermission(); // 권한 요청

        Intent GetIntent = getIntent();
        id = GetIntent.getExtras().getString("user_id");
        nickname = GetIntent.getExtras().getString("user_nick");


        etitle = findViewById(R.id.title);
        econtent = findViewById(R.id.content);
        writeBtn = findViewById(R.id.write_btn);
        imageBtn = findViewById(R.id.Picture_add);



        //글쓰기 버튼
        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                title = etitle.getText().toString();
                content = econtent.getText().toString();

                if(temp == "") {
                    Toast.makeText(getApplicationContext(),"이미지 업로드하샘",Toast.LENGTH_LONG).show();
                }
                else {
                    Write task = new Write();
                    Toast.makeText(getApplicationContext(),temp,Toast.LENGTH_LONG).show();
                    task.execute(getString(R.string.sever) + "/Write.php", id, nickname, title, content, temp);

                    Intent intent = new Intent(WriteActivity.this, MainActivity.class);
                    intent.putExtra("user_id", id);
                    intent.putExtra("user_nickname", nickname);
                    //StyleableToast.makeText(getApplicationContext(), "글쓰기 완료!", Toast.LENGTH_LONG, R.style.sign).show();
                    startActivity(intent);
                    finish();
                }
            }
        });

        // 이미지 추가
        imageBtn.setOnClickListener( new View.OnClickListener(){
            public void onClick(View v){
                // 갤러리 open
                goToAlbum();

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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            progressDialog.dismiss();
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String)params[0];
            String id = (String)params[1];
            String nick = (String)params[2];
            String title = (String)params[3];
            String content = (String)params[4];
            String image = (String)params[5];



            String postParameters = "id=" + id + "&nickname=" + nick + "&title=" + title
                    + "&content=" + content + "&image=" + image;


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



    // 갤러리열기 1
    private void goToAlbum(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/-");
        startActivityForResult(intent, gallery);
    }


    // 갤러리열기 2
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        Intent intent = new Intent();
        Bitmap bm;


        if(resultCode != Activity.RESULT_OK){   // 뒤로가기
            Toast.makeText(this, "사진을 선택해주세요",Toast.LENGTH_LONG).show();

            if(temp != null){
                temp = null;
            }
        }

        if(resultCode == RESULT_OK) {
            if (requestCode == gallery) {
                try{
                    bm = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                    bm = resize(bm);
                    BitMapToString(bm);
                    setImage(bm);

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                setResult(RESULT_OK, intent);

            }
        }
    }

    //이미지 인코딩
    public void BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] arr = baos.toByteArray();
        String image = Base64.encodeToString(arr, Base64.DEFAULT);

        try{
            temp = URLEncoder.encode(image,"utf-8");
        } catch (Exception e){
            Log.e("exception",e.toString());
        }

    }

    //이미지 셋팅
    private void setImage(Bitmap bm){
        ImageView imageView = findViewById(R.id.imageView6);
        imageView.setImageBitmap(bm);
    }

    //이미지 리사이징
    private Bitmap resize(Bitmap bm) {
        Configuration config = getResources().getConfiguration();
        if (config.smallestScreenWidthDp >= 800)
            bm = Bitmap.createScaledBitmap(bm, 400, 240, true);
        else if (config.smallestScreenWidthDp >= 600)
            bm = Bitmap.createScaledBitmap(bm, 300, 180, true);
        else if (config.smallestScreenWidthDp >= 400)
            bm = Bitmap.createScaledBitmap(bm, 200, 120, true);
        else if (config.smallestScreenWidthDp >= 360)
            bm = Bitmap.createScaledBitmap(bm, 180, 108, true);
        else
            bm = Bitmap.createScaledBitmap(bm, 160, 96, true);
        return bm;

    }



    //권한 요청
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();

    }

}
