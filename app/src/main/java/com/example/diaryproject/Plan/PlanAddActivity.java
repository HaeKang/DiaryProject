package com.example.diaryproject.Plan;

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

import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlanAddActivity extends AppCompatActivity {
    String user_id;
    String date;
    String user_nick;

    EditText plancontent;
    Button okBtn;
    TextView dateText;

    private String TAG = "PHPTEST";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_add);
        Intent intent = getIntent();
        user_id = intent.getStringExtra("user_id");
        user_nick = intent.getStringExtra("user_nick");
        date = intent.getStringExtra("select_date");

        dateText = findViewById(R.id.delete_text);
        dateText.setText(date + "\n일정을 추가합니다");


        okBtn = findViewById(R.id.plan_addok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plancontent = findViewById(R.id.plan_edit);
                String content = plancontent.getText().toString();
                if(content.equals(null) || content.equals("")){
                    Toast.makeText(PlanAddActivity.this, "일정을 입력해주세요", Toast.LENGTH_LONG).show();
                } else{
                    AddPlan task = new AddPlan();
                    task.execute(user_id, date,content);
                }
            }
        });
    }


    // DB에 넣기
    class AddPlan extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(PlanAddActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            if(result.contains("일정추가완료")) {
                StyleableToast.makeText(getApplicationContext(), "일정 추가 완료", Toast.LENGTH_LONG, R.style.sign).show();
                Intent intent = new Intent(PlanAddActivity.this, SelectMainActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nickname", user_nick);
                startActivity(intent);

                SelectMainActivity sm = (SelectMainActivity) SelectMainActivity.Select_Main_Activity;
                sm.finish();

                finish();

            } else{
               Toast.makeText(PlanAddActivity.this, "error", Toast.LENGTH_LONG).show();
            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String)params[0];
            String date = (String)params[1];
            String content = params[2];

            String serverURL = getString(R.string.sever) + "/AddPlan.php";

            String postParameters = "id=" + id + "&date=" + date + "&content=" + content;


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
