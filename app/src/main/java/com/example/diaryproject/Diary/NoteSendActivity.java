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

import com.example.diaryproject.R;
import com.example.diaryproject.sign.SignUpActivity;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NoteSendActivity extends AppCompatActivity {
    private String user_nick;
    private String comment_nick;
    private String writer_nick;

    private TextView title;
    private Button okbtn;
    private EditText content;

    private String content_text;

    private String TAG = "PHPTEST";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_send);

        Intent getIntent = getIntent();
        user_nick = getIntent.getExtras().getString("USERNICK");
        comment_nick = getIntent.getExtras().getString("COMMENTNICK");
        writer_nick = getIntent.getExtras().getString("WRITENICK");


        title = findViewById(R.id.titleText);
        okbtn = findViewById(R.id.noteOk_btn);
        content = findViewById(R.id.noteText);

        if(comment_nick != null){
            title.setText(comment_nick + " 님에게 쪽지를 보냅니다.");
        }
        else if(writer_nick != null){
            title.setText(writer_nick + " 님에게 쪽지를 보냅니다.");
        }


        okbtn.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                content_text = content.getText().toString();
                SendNote task = new SendNote();

                if(comment_nick != null) {
                    task.execute(user_nick, comment_nick, content_text);
                    Toast.makeText(getApplicationContext(), comment_nick + " 님에게 쪽지를 보냈습니다!", Toast.LENGTH_LONG).show();
                } else{
                    task.execute(user_nick, writer_nick, content_text);
                    Toast.makeText(getApplicationContext(), writer_nick + " 님에게 쪽지를 보냈습니다!", Toast.LENGTH_LONG).show();
                }

                finish();

            }
        });
    }


    class SendNote extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(NoteSendActivity.this,
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

            String send_nick = (String)params[0];
            String recv_nick = (String)params[1];
            String content = (String)params[2];

            String serverURL = getString(R.string.sever)+ "/SendNote.php";
            String postParameters = "send_nick=" + send_nick + "&recv_nick=" + recv_nick + "&content=" + content;


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
