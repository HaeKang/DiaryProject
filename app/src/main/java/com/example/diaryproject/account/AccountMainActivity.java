package com.example.diaryproject.account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.example.diaryproject.Diary.MainActivity;
import com.example.diaryproject.Diary.PostActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;
import com.muddzdev.styleabletoast.StyleableToast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

public class AccountMainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private TextView thedate;
    private Button btn_go_calendar;
    private Button btn_go_chart;
    private Button btn_go_add;
    private TextView sum_view;
    private TextView sum_month_view;


    String user_id;
    String user_nick;
    String Account_idx;

    public static String View_DATE = getToday_date();
    private String year;
    private String month;
    private String month_date;


    private static final String TAG_JSON="webnautes";
    String mJsonString;
    String TAG_SUM = "sum";

    String TAG_CONTEXT = "context";
    String TAG_PRICE = "price";
    String TAG_IDX = "idx";

    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();
    private RecyclerAdapter mAdapter;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_main);

        Intent intent = getIntent();
        user_id = intent.getExtras().getString("user_id");
        user_nick = intent.getExtras().getString("user_nickname");
        String date = intent.getStringExtra("date");

        //레이아웃 변수설정
        thedate = (TextView) findViewById(R.id.date);
        btn_go_calendar = (Button) findViewById(R.id.btn_go_calendar);
        btn_go_chart = findViewById(R.id.pie_btn);
        mRecyclerView = findViewById(R.id.account_list);
        sum_view = (TextView) findViewById(R.id.total_sum);
        sum_month_view = findViewById(R.id.month_sum);
        btn_go_add = findViewById(R.id.goAdd);


        //날짜 표시 인텐트 설정
        if(!TextUtils.isEmpty(date)){
            View_DATE = date;
            thedate.setText(date);

            Log.d(TAG, "string is not empty");
        } else{
            //date = getToday_date();
            thedate.setText(View_DATE);
        }


        // recyclerview 설정
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        AccountList taskList = new AccountList();
        taskList.execute(user_id, View_DATE);

        // 리스트 클릭 이벤트
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListenerAccount(getApplicationContext(), mRecyclerView, new ClickListenerAccount() {
            @Override
            public void onClick(View view, int pos) {
                Account_idx = mArrayList.get(pos).get(TAG_IDX);

                final CharSequence[] items_com = {"리스트 삭제하기"};
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(AccountMainActivity.this);
                alertDialogBuilder.setTitle("삭제");
                alertDialogBuilder.setItems(items_com, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        if(index == 0){

                            DeleteAccount deleteAccount = new DeleteAccount();
                            deleteAccount.execute(Account_idx);
                            SumAccount sumAccount = new SumAccount();
                            sumAccount.execute(user_id, View_DATE);

                        }
                        dialog.dismiss();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }

            @Override
            public void onLongClick(View view, int pos) {

            }
        }));

        // 총합 가격 표시

        SumAccount sumtask = new SumAccount();
        sumtask.execute(user_id, View_DATE);

        String[] array = View_DATE.split("/");
        year = array[0];
        month = array[1];
        month_date = year + "/" + month + "/%";

        SumMonthAccount sumMonthAccount = new SumMonthAccount();
        sumMonthAccount.execute(user_id, month_date);



        // 달력 이동
        btn_go_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AccountMainActivity.this, CalenderActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nickname", user_nick);
                startActivity(intent);
                finish();
            }
        });

        // 차트activity로 이동
        btn_go_chart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountMainActivity.this, ChartActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nickname", user_nick);
                intent.putExtra("date", View_DATE);
                startActivity(intent);
            }
        });

        // 데이터 입력 이동
        btn_go_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccountMainActivity.this, AddAccount.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nickname", user_nick);
                intent.putExtra("date", View_DATE);
                startActivity(intent);
                finish();
            }
        });

    }


    // 오늘 날짜
    static public String getToday_date(){
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy/M/d", Locale.KOREA);
        Date currentTime = new Date();
        String Today_day = mSimpleDateFormat.format(currentTime);
        return Today_day;
    }

    // 리스트 클릭 이벤트
    public interface ClickListenerAccount{
        void onClick(View view, int pos);
        void onLongClick(View view, int pos);
    }

    public static class RecyclerTouchListenerAccount implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private ClickListenerAccount clickListener;

        public RecyclerTouchListenerAccount (Context context, final RecyclerView recyclerView, final ClickListenerAccount clickListener){
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



    // recyclerview 데이터 추가
    private void getData(){
        Log.d("test"," getData 호출");
        List<String> listContext = new ArrayList<>();
        List<String> listPrice = new ArrayList<>();
        List<String> listIdx = new ArrayList<>();

        listContext.clear();
        listPrice.clear();
        listIdx.clear();

        for (int i = 0; i < mArrayList.size(); i++) {
            listContext.add(mArrayList.get(i).get(TAG_CONTEXT));
            listPrice.add(mArrayList.get(i).get(TAG_PRICE));
            listIdx.add(mArrayList.get(i).get(TAG_IDX));
        }

        for (int i = 0; i < listContext.size(); i++) {
            Data data = new Data();
            int price_int = Integer.parseInt(listPrice.get(i));
            String real_price;
            String context = listContext.get(i);

            // 지출이면
            if(price_int < 0){
                price_int = price_int * -1;
                real_price = Integer.toString(price_int);
                context = "지출 : " + context;

                data.setContext(context);
                data.setPrice(real_price);
                mAdapter.addItem(data);

            } else{
                context = "수입 : " + context;
                data.setContext(context);
                data.setPrice(listPrice.get(i));
                mAdapter.addItem(data);
            }
        }

        mAdapter.notifyDataSetChanged();

    }



    // 총합 가격
    private class SumAccount extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AccountMainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();


            if(result.equals("error")){
                //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }


            if (result == null || result.equals("")){
                Toast.makeText(getApplicationContext(),errorString,Toast.LENGTH_LONG).show();
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            String date = params[1];

            String serverURL = getString(R.string.sever) + "/AccountSum.php";
            String postParameters = "id=" + id + "&date=" + date;


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

                String sum = item.getString(TAG_SUM);
                sum_view.setText(sum);


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }

    // month sum 구하기
    // 총합 가격
    private class SumMonthAccount extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AccountMainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();


            if(result.equals("error")){
                //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }


            if (result == null || result.equals("")){
                Toast.makeText(getApplicationContext(),errorString,Toast.LENGTH_LONG).show();
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            String date = params[1];

            String serverURL = getString(R.string.sever) + "/AccountSumMonth.php";
            String postParameters = "id=" + id + "&date=" + date;


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

                String sum_month = item.getString(TAG_SUM);
                sum_month_view.setText(sum_month);


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }




    // Account list
    private class AccountList extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AccountMainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();


            if(result.equals("error")){
                //Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }


            if (result == null || result.equals("")){
                Toast.makeText(getApplicationContext(),errorString,Toast.LENGTH_LONG).show();
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            String date = params[1];

            String serverURL = getString(R.string.sever) + "/AccountList.php";
            String postParameters = "id=" + id + "&date=" + date;


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
                mArrayList.clear();

                for(int i=0; i<jsonArray.length(); i++){
                    HashMap<String, String> hashMap = new HashMap<>();
                    JSONObject item = jsonArray.getJSONObject(i);

                    String context = item.getString(TAG_CONTEXT);
                    String price = item.getString(TAG_PRICE);
                    String idx = item.getString(TAG_IDX);

                    hashMap.put(TAG_CONTEXT, context);
                    hashMap.put(TAG_PRICE, price);
                    hashMap.put(TAG_IDX, idx);

                    mArrayList.add(hashMap);
                }

                getData();


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }

    // 삭제
    class DeleteAccount extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AccountMainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            Toast.makeText(AccountMainActivity.this, "삭제 완료", Toast.LENGTH_LONG).show();
            AccountList accountList = new AccountList();
            accountList.execute(user_id, View_DATE);
            mRecyclerView.setAdapter(null);
            mAdapter = new RecyclerAdapter();
            mRecyclerView.setAdapter(mAdapter);
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String idx = (String)params[0];

            String serverURL = getString(R.string.sever) + "/DeleteAccount.php";
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

    //뒤로버튼 event
    private long time = 0;
    public void onBackPressed(){
        if(System.currentTimeMillis()-time >= 2000){
            time = System.currentTimeMillis();
            StyleableToast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번 더 누르면\n 메인으로 갑니다.", Toast.LENGTH_LONG,R.style.backtoast).show();
        } else if(System.currentTimeMillis()-time < 2000){
            Intent intent = new Intent(AccountMainActivity.this , SelectMainActivity.class);
            intent.putExtra("user_id", user_id);
            intent.putExtra("user_nickname", user_nick);
            finish();
            startActivity(intent);
        }
    }


}
