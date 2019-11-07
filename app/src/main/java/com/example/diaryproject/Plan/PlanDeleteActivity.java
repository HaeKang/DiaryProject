package com.example.diaryproject.Plan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.diaryproject.Diary.MainActivity;
import com.example.diaryproject.Diary.PostActivity;
import com.example.diaryproject.Fragment.Data;
import com.example.diaryproject.Fragment.PageOne.PageOneFragment;
import com.example.diaryproject.Fragment.PageOne.RecyclerAdapter;
import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PlanDeleteActivity extends AppCompatActivity {

    String user_id;
    String user_nick;
    ArrayList<String> idx_list;
    ArrayList<String> content_list;

    private String TAG = "PHPTEST";
    private PlanRecyclerAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_delete);

        Intent intent = getIntent();
        user_id = intent.getExtras().getString("user_id");
        user_nick = intent.getExtras().getString("user_nick");
        content_list = intent.getExtras().getStringArrayList("content");
        idx_list = intent.getExtras().getStringArrayList("idx");

        // recyclerview
        RecyclerView recyclerView = findViewById(R.id.plan_recyclerview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(PlanDeleteActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new PlanRecyclerAdapter();
        recyclerView.setAdapter(mAdapter);

        getData(content_list, idx_list);

        // list 클릭 event
        recyclerView.addOnItemTouchListener(new PlanRecyclerTouchListener(PlanDeleteActivity.this, recyclerView, new PageOneFragment.ClickListener(){
            @Override
            public void onClick(View view, int pos) {
                String idx = idx_list.get(pos);
                DeletePlan task = new DeletePlan();
                task.execute(idx);

                Intent intent = new Intent(PlanDeleteActivity.this, SelectMainActivity.class);
                intent.putExtra("user_id",user_id);
                intent.putExtra("user_nickname",user_nick);
                startActivity(intent);
                finish();

            }
        }));

    }


    // recyclerview 데이터 추가
    private void getData(ArrayList<String> content_list, ArrayList<String> idx_list){
        for (int i = 0; i < content_list.size(); i++) {
            PlanData data = new PlanData();
            data.setContent(content_list.get(i));
            data.setIdx(idx_list.get(i));
            mAdapter.addItem(data);
        }

        mAdapter.notifyDataSetChanged();

    }

    // recyclerview 클릭 class
    public static class PlanRecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector detector;
        private PageOneFragment.ClickListener clickListener;

        public PlanRecyclerTouchListener(Context context, final RecyclerView recyclerView, final PageOneFragment.ClickListener clickListener) {
            this.clickListener = clickListener;
            detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                public boolean onSingleTapUp(MotionEvent e){
                    return true;
                }
            });

        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && detector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }


    // 댓글 삭제 DB
    class DeletePlan extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(PlanDeleteActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Toast.makeText(PlanDeleteActivity.this, "일정 삭제 완료", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(PlanDeleteActivity.this, SelectMainActivity.class);
            intent.putExtra("user_id", user_id);
            intent.putExtra("user_nickname",user_nick);
            startActivity(intent);
            finish();
        }


        @Override
        protected String doInBackground(String... params) {

            String idx = (String)params[0];



            String serverURL = getString(R.string.sever) + "/DeletePlan.php";
            String postParameters = "idx=" + idx;


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
