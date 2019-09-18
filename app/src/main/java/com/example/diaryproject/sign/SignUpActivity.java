package com.example.diaryproject.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.R;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private Button Signup;
    private TextView Id;
    private TextView Pw;
    private TextView Nickname;

    private String TAG = "PHPTEST";

    private String state_db = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        Signup = findViewById(R.id.Signup_btn);
        Id = findViewById(R.id.Id);
        Pw = findViewById(R.id.Pw);
        Nickname = findViewById(R.id.Nickname);


        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String id = Id.getText().toString();
                String pw = Pw.getText().toString();
                String nickname = Nickname.getText().toString();


                InsertData task = new InsertData();
                task.execute(id, pw, nickname);

                Id.setText("");
                Pw.setText("");
                Nickname.setText("");


                if(!id.equals("") && !pw.equals("") && !nickname.equals("") && state_db.equals("ok")) {
                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                    StyleableToast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_LONG, R.style.sign).show();
                    startActivity(intent);
                    finish();
                }
                else{
                    StyleableToast.makeText(getApplicationContext(), "아이디, 닉네임 중복됩니다 다른 아이디와 닉네임을 사용해주세요", Toast.LENGTH_LONG, R.style.sign).show();
                }
            }
        });
    }


    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SignUpActivity.this,
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

            String id = (String)params[0];
            String pw = (String)params[1];
            String nick = (String)params[2];

            String serverURL = getString(R.string.sever)+ "/SignUp.php";
            String postParameters = "id=" + id + "&pw=" + pw + "&nickname=" + nick;


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
                state_db = "ok";
                return sb.toString();


            } catch (Exception e) {
                Log.d(TAG, "InsertData: Error ", e);
                return new String("Error: " + e.getMessage());
            }

        }
    }
}
