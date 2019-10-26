package com.example.diaryproject.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
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
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private Button Signup;
    private TextView Id;
    private TextView Pw;
    private TextView Nickname;
    private Button idcheck;
    private Button nickcheck;
    private String state_check = "";
    private boolean check_id_state = false;
    private boolean check_nick_state = false;

    private String TAG = "PHPTEST";
    private String TAG_JSON = "stateIdNick";
    String mJsonString;

    private String state_db = "";

    private String id = "";
    private String pw = "";
    private String nickname = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        Signup = findViewById(R.id.Signup_btn);
        Id = findViewById(R.id.Id);
        Pw = findViewById(R.id.Pw);
        Nickname = findViewById(R.id.Nickname);
        idcheck = findViewById(R.id.id_checkBtn);
        nickcheck = findViewById(R.id.nickname_checkBtn);


        Id.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        Pw.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        // 아이디 비번 영어숫자만 가능
        Id.setFilters(new InputFilter[] {filter});
        Pw.setFilters(new InputFilter[] {filter});


        // 아이디 중복체크 버튼 클릭
        idcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                state_check = "id";
                id = Id.getText().toString();

                if( id.equals(null)){
                    Toast.makeText(getApplicationContext(),"아이디를 입력하세요",Toast.LENGTH_LONG).show();
                }
                else {
                    CheckIdNick taskId = new CheckIdNick();
                    taskId.execute(state_check, id, "");
                }
            }
        });


        // 닉네임 중복체크 버튼 클릭
        nickcheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                state_check = "nickname";
                nickname = Nickname.getText().toString();

                if(nickname.equals(null)) {
                    Toast.makeText(getApplicationContext(),"닉네임을 입력하세요",Toast.LENGTH_LONG).show();
                }
                else{
                    CheckIdNick taskNick = new CheckIdNick();
                    taskNick.execute(state_check, "", nickname);
                }
            }
        });




        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(check_id_state && check_nick_state) {
                    id = Id.getText().toString();
                    pw = Pw.getText().toString();
                    nickname = Nickname.getText().toString();

                    InsertData task = new InsertData();
                    task.execute(id, pw, nickname);

                    Id.setText("");
                    Pw.setText("");
                    Nickname.setText("");


                    if (!id.equals("") && !pw.equals("") && !nickname.equals("") && state_db.equals("ok")) {
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        StyleableToast.makeText(getApplicationContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_LONG, R.style.sign).show();
                        startActivity(intent);
                        finish();
                    }
                }
                else if (check_id_state == false && check_nick_state == true){
                    StyleableToast.makeText(getApplicationContext(), "아이디 중복확인 필요", Toast.LENGTH_LONG, R.style.sign).show();
                }
                else{
                    StyleableToast.makeText(getApplicationContext(), "닉네임 중복확인 필요", Toast.LENGTH_LONG, R.style.sign).show();
                }
            }
        });
    }

    // 아이디 비번 영문,숫자만 허용
    protected InputFilter filter= new InputFilter() {

        public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

            Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
            if (!ps.matcher(source).matches()) {
                return "";
            }
            return null;
        }
    };

    // 회원가입 DB
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


    // 아이디 닉네임 중복확인 CheckIdNick.php

    private class CheckIdNick extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

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


            if(result == null){
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String state_check = params[0];
            String id = params[1];
            String nickname = params[2];

            String serverURL = getString(R.string.sever) + "/CheckIdNick.php";
            String postParameters = "state_check=" + state_check + "&id=" + id + "&nickname=" + nickname;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

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
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }

        private void showResult(){
            try {

                Id = findViewById(R.id.Id);
                Nickname = findViewById(R.id.Nickname);

                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                JSONObject item = jsonArray.getJSONObject(0);

                if(item.isNull("nick_check_state")) {

                    final String id_check = item.getString("id_check_state");
                    Log.d("test",id_check);

                    if(id_check.equals("ok")){
                        check_id_state = true;
                        StyleableToast.makeText(getApplicationContext(), "아이디 사용가능", Toast.LENGTH_LONG, R.style.sign).show();
                    }else{
                        check_id_state = false;
                        Id.setText("");
                        Id.requestFocus();
                    }

                } else {

                    final String nick_check = item.getString("nick_check_state");

                    if(nick_check.equals("ok")){
                        check_nick_state = true;
                        StyleableToast.makeText(getApplicationContext(), "닉네임 사용 가능", Toast.LENGTH_LONG, R.style.sign).show();
                    }else{
                        check_nick_state = false;
                        Nickname.setText("");
                        Nickname.requestFocus();
                    }
                }


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }
        }

    }


}
