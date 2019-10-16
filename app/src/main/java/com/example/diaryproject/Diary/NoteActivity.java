package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NoteActivity extends AppCompatActivity {

    private static final String TAG_IDX = "idx";
    private static final String TAG_SENDNICK = "send_nick";
    private static final String TAG_SENDDATE = "date";
    private static final String TAG_CONTENT = "content";
    private String TAG = "PHPTEST";
    private static final String TAG_JSON = "note";

    String mJsonString;

    TextView tNickname;
    TextView tDate;
    TextView tContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Intent GetIntent = getIntent();
        String index = GetIntent.getStringExtra("INDEX");
        String user_id = GetIntent.getStringExtra("USERID");
        String user_nick = GetIntent.getStringExtra("NICKNAME");

        ReadNote task = new ReadNote();
        task.execute(index);

    }


    // 쪽지 불러오기 DB
    private class ReadNote extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(NoteActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();


            if (result == null || result.equals("")) {

            } else {
                mJsonString = result;
                showResult();

            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = getString(R.string.sever) + "/FindNote.php";
            String idx = params[0];


            String postParameters = "idx=" + idx;


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
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
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

        private void showResult() {
            try {
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                JSONObject item = jsonArray.getJSONObject(0);


                String nickname = item.getString(TAG_SENDNICK);
                String date = item.getString(TAG_SENDDATE);
                String content = item.getString(TAG_CONTENT);


                tNickname = findViewById(R.id.send_nick_note);
                tDate = findViewById(R.id.date_note);
                tContent = findViewById(R.id.content_note);

                tNickname.setText("보낸이 : " + nickname);
                tDate.setText(date);
                tContent.setText(content);


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }


    }

    public void onBackPressed(){
        finish();
    }
}
