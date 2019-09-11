package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);


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

                Write task = new Write();
                task.execute(getString(R.string.sever)+ "/Write.php",id, nickname,title,content);

                Intent intent = new Intent(WriteActivity.this,MainActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nickname",nickname);
                StyleableToast.makeText(getApplicationContext(), "글쓰기 완료!", Toast.LENGTH_LONG, R.style.sign).show();
                startActivity(intent);
                finish();
            }
        });

        // 이미지 추가
        imageBtn.setOnClickListener( new View.OnClickListener(){
            public void onClick(View v){
                Intent intent_image = new Intent();
                intent_image.setAction(Intent.ACTION_GET_CONTENT);
                intent_image.setType("image/-");
                startActivityForResult(intent_image, gallery);
            }
         });
    }

    // 이미지 part
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Intent intent = new Intent();
        Bitmap bm;
        if(resultCode == RESULT_OK){
            try{
                bm = MediaStore.Images.Media.getBitmap( getContentResolver(), data.getData());
                bm = resize(bm);
                intent.putExtra("bitmap",bm);
            } catch (IOException e){
                e.printStackTrace();
            } catch (OutOfMemoryError e){
                Toast.makeText(getApplicationContext(),"이미지 용량 큼", Toast.LENGTH_LONG).show();
            }
            setResult(RESULT_OK, intent);
            finish();
        } else{
            setResult(RESULT_CANCELED, intent);
            finish();
        }
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

    //이미지 인코딩
    public void BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] arr = baos.toByteArray();
        String image = Base64.encodeToString(arr, Base64.DEFAULT);
        String temp = "";
        try{
            temp = "&imagedevice="+URLEncoder.encode(image,"utf-8");
        } catch (Exception e){
            Log.e("exception",e.toString());
        }
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
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = (String)params[0];
            String id = (String)params[1];
            String nick = (String)params[2];
            String title = (String)params[3];
            String content = (String)params[4];



            String postParameters = "id=" + id + "&nickname=" + nick + "&title=" + title
                    + "&content=" + content;


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
