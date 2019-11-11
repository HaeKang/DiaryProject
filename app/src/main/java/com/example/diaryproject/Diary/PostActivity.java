package com.example.diaryproject.Diary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    String post_id;
    String user_id;

    private String TAG = "PHPTEST";
    private static final String TAG_JSON = "post";
    private static final String TAG_JSON2 = "compost";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_ID = "id";
    private static final String TAG_DATE = "date";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_COMMENT = "comment";
    private static final String TAG_WEATHER = "weather";

    private ComRecyclerAdapter mAdapter;
    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();


    TextView tTitle;
    TextView tContent;
    TextView tNickname;
    TextView tDate;
    ImageView wImage;
    ImageView pImage;
    EditText tComment;
    Button btn;

    String mJsonString;

    String user_nickname;
    String writer_nickname;
    String write_id;
    String comment_nickname;
    String comment_id;
    String comment;
    String RealTodayDate;

    String WriteDate;
    String write_content;
    String write_weather;
    String write_title;

    String state_post = "find";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent GetIntent = getIntent();
        post_id = GetIntent.getExtras().getString("POSTID");
        user_nickname = GetIntent.getExtras().getString("NICKNAME");
        user_id = GetIntent.getExtras().getString("USERID");


        // 오늘 날짜
        SimpleDateFormat fomat1 = new SimpleDateFormat( "yyyy-MM-dd");
        Date time = new Date();
        final String RealTodayDate = fomat1.format(time);



        //댓글 ui
        final RecyclerView mRecycler = findViewById(R.id.comment_recycler);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PostActivity.this);
        linearLayoutManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(linearLayoutManager);

        mAdapter = new ComRecyclerAdapter();
        mRecycler.setAdapter(mAdapter);


        // 댓글 recyclerview 클릭 이벤트
        mRecycler.addOnItemTouchListener(new RecyclerTouchListenerComment(getApplicationContext(),
                mRecycler, new ClickListenerComment(){

            public void onClick(View view, int pos){

            }

            public void onLongClick(View view, int pos){
                comment_nickname = mArrayList.get(pos).get(TAG_NICKNAME);
                comment_id = mArrayList.get(pos).get(TAG_ID);
                final String comment_delete = mArrayList.get(pos).get(TAG_COMMENT);

                if(user_nickname.equals(comment_nickname)) {
                    final CharSequence[] items_com = {"댓글 삭제하기"};
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                    alertDialogBuilder.setTitle("댓글 삭제");
                    alertDialogBuilder.setItems(items_com, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){

                                DeleteComment deletecomment = new DeleteComment();
                                deletecomment.execute(comment_nickname, post_id, comment_delete);

                            }
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                else{
                    final CharSequence[] items_com = {"쪽지보내기"};
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                    alertDialogBuilder.setTitle("작성자에게 쪽지보내기");
                    alertDialogBuilder.setItems(items_com, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){
                                Intent intentNoteCom = new Intent(getApplicationContext(), NoteSendActivity.class);
                                intentNoteCom.putExtra("USERID", user_id);
                                intentNoteCom.putExtra("COMMENTID", comment_id);
                                startActivity(intentNoteCom);
                            }
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

            }

        }));


        // 글 불러오기
        ReadPost task = new ReadPost();
        task.execute(post_id, state_post);

        // 댓글 불러오기
        ReadComment taskCom = new ReadComment();
        taskCom.execute(post_id);


        // 댓글 입력 버튼 클릭 event
        btn = findViewById(R.id.Ok_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tComment = findViewById(R.id.comment_text);
                comment = tComment.getText().toString();

                InsertComment taskcomment = new InsertComment();
                taskcomment.execute(user_nickname,post_id,comment, user_id);

                mAdapter.resetItem();

                ReadComment taskCom2 = new ReadComment();
                taskCom2.execute(post_id);

                tComment.setText("");

            }
        });


        //imageview 클릭
        pImage = findViewById(R.id.pencil_image);
        pImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user_nickname.equals(writer_nickname)) {
                    final CharSequence[] items = {"글 삭제하기", "글 수정하기"};
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                    alertDialogBuilder.setTitle("글 수정삭제");
                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0) {
                                state_post = "delete";
                                ReadPost deletetask = new ReadPost();
                                deletetask.execute(post_id, state_post);
                            } else if(index == 1){
                                // 글 수정
                                if(RealTodayDate.equals(WriteDate)){

                                    Toast.makeText(getApplicationContext(),"수정고고",Toast.LENGTH_LONG).show();
                                    Intent WriteIntent = new Intent(getApplicationContext(), WriteActivity.class);
                                    WriteIntent.putExtra("user_id", user_id);
                                    WriteIntent.putExtra("user_nick",user_nickname);
                                    WriteIntent.putExtra("content", write_content);
                                    WriteIntent.putExtra("title", write_title);
                                    WriteIntent.putExtra("weather", write_weather);
                                    startActivity(WriteIntent);
                                    finish();

                                }else{

                                    Toast.makeText(getApplicationContext(),"오늘 작성한 글만 수정 가능",Toast.LENGTH_LONG).show();

                                }

                            }
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();

                }
                else{
                    final CharSequence[] items = {"쪽지보내기"};
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                    alertDialogBuilder.setTitle("작성자에게 쪽지 보내기");
                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){
                                Intent intentNoteWri = new Intent(getApplicationContext(), NoteSendActivity.class);
                                intentNoteWri.putExtra("USERNICK", user_nickname);
                                intentNoteWri.putExtra("WRITEID", write_id);
                                startActivity(intentNoteWri);
                            }

                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }

            }
        });




    }

    // 댓글 클릭 이벤트
    public interface ClickListenerComment{
        void onClick(View view, int pos);
        void onLongClick(View view, int pos);
    }

    public static class RecyclerTouchListenerComment implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private PostActivity.ClickListenerComment clickListener;

        public RecyclerTouchListenerComment(Context context, final RecyclerView recyclerView, final PostActivity.ClickListenerComment clickListener){
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
               public boolean onSingleTapUp(MotionEvent e){
                   return true;
               }

               public void onLongPress(MotionEvent e){
                   View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                   if(child != null && clickListener != null){
                       clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                   }
               }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if(child != null && clickListener != null && gestureDetector.onTouchEvent(e)){
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


    // 댓글 불러오기 layout
    private void getData(){

        List<String> listNick = new ArrayList<>();
        List<String> listComment = new ArrayList<>();
        listNick.clear();
        listComment.clear();

        for (int i = 0; i < mArrayList.size(); i++) {
            listNick.add(mArrayList.get(i).get(TAG_NICKNAME));
            listComment.add(mArrayList.get(i).get(TAG_COMMENT));
        }


        for (int i = 0; i < listNick.size(); i++) {
            CommentData data = new CommentData();
            data.setNick(listNick.get(i));
            data.setComment(listComment.get(i));
            mAdapter.addItem(data);
        }

        mAdapter.notifyDataSetChanged();

    }

    // 댓글 불러오기 DB
    private class ReadComment extends AsyncTask<String, Void, String> {

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

            } else {
                    mJsonString = result;
                    showResult();

            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = getString(R.string.sever) + "/FindComment.php";
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
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON2);
                mArrayList.clear();

                for(int i=0; i<jsonArray.length(); i++){
                    HashMap<String, String> hashMap = new HashMap<>();
                    JSONObject item = jsonArray.getJSONObject(i);

                    String nickname = item.getString(TAG_NICKNAME);
                    String comment = item.getString(TAG_COMMENT);
                    String id = item.getString(TAG_ID);

                    hashMap.put(TAG_NICKNAME, nickname);
                    hashMap.put(TAG_COMMENT, comment);
                    hashMap.put(TAG_ID, id);

                    mArrayList.add(hashMap);
                }


                getData();


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }



    // 댓글 달기 DB
    class InsertComment extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

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
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String nickname = (String)params[0];
            String post_id = (String)params[1];
            String comment = (String)params[2];
            String id = params[3];


            String serverURL = getString(R.string.sever) + "/comment.php";
            String postParameters = "nickname=" + nickname + "&post_id=" + post_id + "&comment=" + comment + "&id=" + id;


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


    // 댓글 삭제 DB
    class DeleteComment extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

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
            Toast.makeText(PostActivity.this, "댓글 삭제 완료", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(PostActivity.this, MainActivity.class);
            intent.putExtra("user_id", user_id);
            intent.putExtra("user_nickname",user_nickname);
            startActivity(intent);
            finish();
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String nickname = (String)params[0];
            String post_id = (String)params[1];
            String comment = (String)params[2];


            String serverURL = getString(R.string.sever) + "/deleteComment.php";
            String postParameters = "nickname=" + nickname + "&post_id=" + post_id + "&comment_user=" + comment;


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


    // 글 불러오기 DB
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
                Toast.makeText(PostActivity.this, "글 삭제 완료", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(PostActivity.this, MainActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nickname",user_nickname);
                startActivity(intent);
                finish();
            } else {
                if (state_post.equals("find")) {
                    mJsonString = result;
                    showResult();
                }
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = getString(R.string.sever) + "/FindPost.php";
            String post_id = params[0];
            String state = params[1];


            String postParameters = "post_id=" + post_id + "&state=" + state;


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
                String weather = item.getString(TAG_WEATHER);
                String id = item.getString(TAG_ID);

                WriteDate = date;
                write_content = content;
                write_weather = weather;
                write_title = title;

                write_id = id;
                writer_nickname = nickname;

                tNickname = findViewById(R.id.user_text);
                tTitle = findViewById(R.id.title_text);
                tDate = findViewById(R.id.delete_text);
                tContent = findViewById(R.id.content_text);
                wImage = findViewById(R.id.weather_img);

                tNickname.setText("작성자 : " + nickname);
                tTitle.setText(title);
                tDate.setText(date);
                tContent.setText(content);

                switch (weather){
                    case "맑음":
                        wImage.setImageResource(R.drawable.sun_check);
                        break;
                    case "해구름" :
                        wImage.setImageResource(R.drawable.suncloud_check);
                        break;
                    case "구름":
                        wImage.setImageResource(R.drawable.cloud_check);
                        break;
                    case "비":
                        wImage.setImageResource(R.drawable.rain_check);
                        break;
                    case "눈":
                        wImage.setImageResource(R.drawable.snow_check);
                        break;
                }

            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }

    }

    public void onBackPressed(){
        finish();
    }

}


