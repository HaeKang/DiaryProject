package com.example.diaryproject.sign;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;
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

import androidx.appcompat.app.AppCompatActivity;

public class SignInActivity extends AppCompatActivity {

    private Button signin;
    private Button signup;
    private TextView id;
    private TextView pw;
    private CheckBox autologin;

    private String TAG = "PHPTEST";
    private static final String TAG_JSON="webnautes";
    private static final String TAG_ID = "id";
    private static final String TAG_NICKNAME ="nickname";
    String mJsonString;


    private SharedPreferences loginInfo;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        signin = findViewById(R.id.SignInOk_Btn);
        signup = findViewById(R.id.SignUp_Btn);

        id = findViewById(R.id.edit_username);
        pw = findViewById(R.id.edit_password);

        autologin = findViewById(R.id.autologin);

        loginInfo = getSharedPreferences("setting",0);
        String auto_loginid = loginInfo.getString("id",null);



        // 자동로그인 정보 X
        if(auto_loginid == null){

            signin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Login task = new Login();
                    task.execute(id.getText().toString(), pw.getText().toString());
                }
            });

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                    startActivity(intent);
                }
            });
        }

        else{
            Intent intent = new Intent(SignInActivity.this, SelectMainActivity.class);
            intent.putExtra("user_id", loginInfo.getString("id",""));
            intent.putExtra("user_nickname", loginInfo.getString("nickname",""));
            StyleableToast.makeText(getApplicationContext(), "자동로그인 했습니다" , Toast.LENGTH_LONG, R.style.sign).show();
            startActivity(intent);
            finish();
        }

    }

    // 로그인DB
    private class Login extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SignInActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            Log.d("test",result);
            if(result.equals("아이디와 패스워드를 다시 확인하세요") || result.equals("비밀번호를 다시 확인하세요") || result.equals("존재하지 않는 회원입니다") || result.equals("아이디를 다시 확인하세요")||result == null){
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            String pw = params[1];

            String serverURL = getString(R.string.sever) + "/Login.php";
            String postParameters = "id=" + id + "&pw=" + pw;


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
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                JSONObject item = jsonArray.getJSONObject(0);

                final String id = item.getString(TAG_ID);
                final String nickname = item.getString(TAG_NICKNAME);


                Intent intent = new Intent(getApplicationContext(), SelectMainActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nickname",nickname);

                // 자동로그인
                if(autologin.isChecked()){
                    SharedPreferences.Editor editor = loginInfo.edit();
                    editor.putString("id", id);
                    editor.putString("nickname", nickname);
                    editor.commit();
                }

                StyleableToast.makeText(getApplicationContext(), "반갑습니다 " + nickname + " 님!", Toast.LENGTH_LONG, R.style.sign).show();
                startActivity(intent);
                finish();


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }



    //뒤로버튼 event
    private long time = 0;
    public void onBackPressed(){
        if(System.currentTimeMillis()-time >= 2000){
            time = System.currentTimeMillis();
            StyleableToast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_LONG,R.style.backtoast).show();
        } else if(System.currentTimeMillis()-time < 2000){
            finish();
        }
    }

}
