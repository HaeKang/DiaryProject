package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
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

    ImageView sendImg;
    Spinner note_spn;

    String user_nick = "";
    String user_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        Intent GetIntent = getIntent();
        final String index = GetIntent.getStringExtra("INDEX");
        user_nick = GetIntent.getStringExtra("USERNICK");
        user_id = GetIntent.getStringExtra("USERID");
        final String send_nick = GetIntent.getStringExtra("SENDNICK");

        if(send_nick.equals(null)){
            Toast.makeText(getApplicationContext(), "삭제된 쪽지입니다.", Toast.LENGTH_LONG).show();
            GoMainActivity(user_id, user_nick);
        }

        ReadNote task = new ReadNote();
        task.execute(index);

        sendImg = findViewById(R.id.send_note_img);
        note_spn = findViewById(R.id.note_spinner);


        // note_send 이미지 클릭하면 notesend acitivity로 이동
        sendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoSendActivity(send_nick, user_nick);
            }
        });


        // spinner
        note_spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)view).setText(null);
                if(position == 0){

                }
                else if(position == 1){
                    GoSendActivity(send_nick, user_nick);
                }
                else if(position == 2){
                    DeleteNote Deltask = new DeleteNote();
                    Deltask.execute(index);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    public void GoMainActivity(String user_id, String user_nick){
        Intent intent = new Intent(NoteActivity.this, MainActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("user_nickname",user_nick);
        startActivity(intent);
        finish();
    }

    public void GoSendActivity(String send_nick, String user_nick){
        Intent sendIntent = new Intent(getApplicationContext(), NoteSendActivity.class);
        sendIntent.putExtra("SENDNICK", send_nick);
        sendIntent.putExtra("USERNICK", user_nick);
        startActivity(sendIntent);
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


    // 쪽지 삭제 DB
    class DeleteNote extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

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
            Toast.makeText(NoteActivity.this, "쪽지 삭제 완료", Toast.LENGTH_LONG).show();
            GoMainActivity(user_id, user_nick);
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String index = (String)params[0];

            String serverURL = getString(R.string.sever) + "/deleteNote.php";
            String postParameters = "idx=" + index;


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


    public void onBackPressed(){
        finish();
    }
}
