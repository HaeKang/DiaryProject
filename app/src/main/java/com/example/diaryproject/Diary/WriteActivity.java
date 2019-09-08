package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.Fragment.PageTwoFragment;
import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.sign.SignInActivity;
import com.example.diaryproject.sign.SignUpActivity;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

public class WriteActivity extends AppCompatActivity {
    private String id;
    private String nickname;
    private String title;
    private String content;


    private EditText etitle;
    private EditText econtent;
    private Button writeBtn;

    private String TAG = "PHPTEST";


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


    }

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
