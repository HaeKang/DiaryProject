package com.example.diaryproject.Diary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.sign.SignInActivity;
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
import java.net.URLEncoder;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    String post_id;

    private String TAG = "PHPTEST";
    private static final String TAG_JSON = "post";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_DATE = "date";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_IMAGE = "image";

    TextView tTitle;
    TextView tContent;
    TextView tNickname;
    TextView tDate;
    ImageView tImage;
    Button btn;

    String mJsonString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent GetIntent = getIntent();
        post_id = GetIntent.getExtras().getString("POSTID");

        ReadPost task = new ReadPost();
        task.execute(post_id);

    }


    // DB연결
    private class ReadPost extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(PostActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();


            if (result == null || result.equals("")) {
                Toast.makeText(PostActivity.this, "안됨", Toast.LENGTH_LONG).show();
            } else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = getString(R.string.sever) + "/FindPost.php";
            String post_id = params[0];


            String postParameters = "post_id=" + post_id;


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


                String nickname = item.getString(TAG_NICKNAME);
                String title = item.getString(TAG_TITLE);
                String date = item.getString(TAG_DATE);
                String content = item.getString(TAG_CONTENT);
                Bitmap image = StringToBitMap(item.getString(TAG_IMAGE));


                tNickname = findViewById(R.id.user_text);
                tTitle = findViewById(R.id.title_text);
                tDate = findViewById(R.id.date_text);
                tContent = findViewById(R.id.content_text);
                tImage = findViewById(R.id.content_image);

                tNickname.setText(nickname);
                tTitle.setText(title);
                tDate.setText(date);
                tContent.setText(content);

                if(image != null) {
                    tImage.setImageBitmap(image);
                } else{
                    tImage.setImageResource(R.drawable.signup);
                }

            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }

        public Bitmap StringToBitMap(String image){
            Log.e("StringToBitMap","StringToBitMap");
            try{
                byte [] encodeByte=Base64.decode(image,Base64.DEFAULT);
                Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                return bitmap;

            }catch(Exception e){
                e.getMessage();
                return null;
            }
        }

    }
}


