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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.example.diaryproject.MainActivity;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {

    String post_id;
    String id;

    private String TAG = "PHPTEST";
    private static final String TAG_JSON = "post";
    private static final String TAG_JSON2 = "compost";
    private static final String TAG_NICKNAME = "nickname";
    private static final String TAG_DATE = "date";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_COMMENT = "comment";
    private static final String TAG_IMAGE = "image";

    private ComRecyclerAdapter mAdapter;
    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();


    TextView tTitle;
    TextView tContent;
    TextView tNickname;
    TextView tDate;
    ImageView tImage;
    ImageView pImage;
    EditText tComment;
    Button btn;

    String mJsonString;

    String user_nickname;
    String writer_nickname;
    String comment_nickname;
    String comment;

    String state_post = "find";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Intent GetIntent = getIntent();
        post_id = GetIntent.getExtras().getString("POSTID");
        user_nickname = GetIntent.getExtras().getString("NICKNAME");
        id = GetIntent.getExtras().getString("USERID");


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
                final String comment_delete = mArrayList.get(pos).get(TAG_COMMENT);

                if(user_nickname.equals(comment_nickname)) {
                    final CharSequence[] items_com = {"댓글 삭제하기"};
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                    alertDialogBuilder.setTitle("댓글 수정삭제");
                    alertDialogBuilder.setItems(items_com, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){

                                // 댓글 삭제가 안됨 왜지????????????? post_id로는 되는디 nickname, comment로는 안됨 이유찾자...
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
                    alertDialogBuilder.setTitle("댓글 수정삭제");
                    alertDialogBuilder.setItems(items_com, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){
                                Intent intentNoteCom = new Intent(getApplicationContext(), NoteSendActivity.class);
                                intentNoteCom.putExtra("USERNICK", user_nickname);
                                intentNoteCom.putExtra("COMMENTNICK", comment_nickname);
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
                taskcomment.execute(user_nickname,post_id,comment);

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
                    final CharSequence[] items = {"글 삭제하기"};
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PostActivity.this);
                    alertDialogBuilder.setTitle("글 수정삭제");
                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){
                                state_post  = "delete";
                                ReadPost deletetask = new ReadPost();
                                deletetask.execute(post_id, state_post);
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
                    alertDialogBuilder.setTitle("글 수정삭제");
                    alertDialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            if(index == 0){
                                Intent intentNoteWri = new Intent(getApplicationContext(), NoteSendActivity.class);
                                intentNoteWri.putExtra("USERNICK", user_nickname);
                                intentNoteWri.putExtra("WRITENICK", writer_nickname);
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

                    hashMap.put(TAG_NICKNAME, nickname);
                    hashMap.put(TAG_COMMENT, comment);

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


            String serverURL = getString(R.string.sever) + "/comment.php";
            String postParameters = "nickname=" + nickname + "&post_id=" + post_id + "&comment=" + comment;


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
            intent.putExtra("user_id", id);
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
                intent.putExtra("user_id", id);
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
                Bitmap image = StringToBitMap(item.getString(TAG_IMAGE));

                writer_nickname = nickname;

                tNickname = findViewById(R.id.user_text);
                tTitle = findViewById(R.id.title_text);
                tDate = findViewById(R.id.date_text);
                tContent = findViewById(R.id.content_text);
                tImage = findViewById(R.id.content_image);

                tNickname.setText("작성자 : " + nickname);
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
                byte [] encodeByte=image.getBytes();
                Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                return bitmap;

            }catch(Exception e){
                e.getMessage();
                return null;
            }
        }

    }

    public void onBackPressed(){
        finish();
    }

}


