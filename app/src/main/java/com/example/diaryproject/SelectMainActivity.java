package com.example.diaryproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.diaryproject.Diary.MainActivity;
import com.example.diaryproject.account.AccountMainActivity;
import com.example.diaryproject.sign.SignInActivity;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class SelectMainActivity extends AppCompatActivity {

    private String id;
    private String nickname;

    ImageButton diary;
    ImageButton cal;
    Button logout;

    Date today = new Date(System.currentTimeMillis());

    ArrayList<String> mArrayList = new ArrayList<>();
    HashSet<CalendarDay> dayArrayList = new HashSet<>();

    String mJsonString;
    private String TAG = "PHPTEST";
    private static final String TAG_DATE ="date";
    private static final String TAG_JSON="date";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_main);

        Intent intent = getIntent();
        id = intent.getExtras().getString("user_id");
        nickname = intent.getExtras().getString("user_nickname");

        diary = findViewById(R.id.Diary_btn);
        cal = findViewById(R.id.Cal_btn);
        logout = findViewById(R.id.main_logout_btn);


        diary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectMainActivity.this, MainActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nickname", nickname);
                startActivity(intent);
            }
        });

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectMainActivity.this, AccountMainActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nickname", nickname);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences loginInfo = getSharedPreferences("setting",0);
                SharedPreferences.Editor editor = loginInfo.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(SelectMainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();

                Toast.makeText(SelectMainActivity.this,"로그아웃 했습니다.",Toast.LENGTH_LONG).show();
            }
        });


        // 캘린더
        final MaterialCalendarView materialCalendarView = findViewById(R.id.select_cal);

        getPostDate task = new getPostDate();
        task.execute(id);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
                .setMinimumDate(CalendarDay.from(2010, 4, 3))
                .setMaximumDate(CalendarDay.from(2050, 5, 12))
                .setCalendarDisplayMode(CalendarMode.WEEKS)
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.setDynamicHeightEnabled(true);

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator()
        );

        // 클릭 event
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                CalendarDay today = CalendarDay.today(); // 오늘날짜
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                materialCalendarView.addDecorators(
                        new EventDecorator()
                );

            }
        }, 1200);

    }



    // 일요일 빨간색상
    public class SundayDecorator implements DayViewDecorator {

        private final Calendar calendar = Calendar.getInstance();

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SUNDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.RED));
        }
    }

    // 토요일 파랑색
    public class SaturdayDecorator implements DayViewDecorator{

        private final Calendar calendar = Calendar.getInstance();

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            int weekDay = calendar.get(Calendar.DAY_OF_WEEK);
            return weekDay == Calendar.SATURDAY;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.BLUE));
        }
    }

    // 오늘날짜 노랑색
    public class TodayDecorator implements DayViewDecorator{

        private final Calendar calendar = Calendar.getInstance();
        CalendarDay today = CalendarDay.today();

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            day.copyTo(calendar);
            return today != null && day.equals(today);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new StyleSpan(Typeface.BOLD));
            view.addSpan(new DotSpan(10, Color.BLACK));
        }
    }


    // 글 쓴 날에 점찍기 deco
    public class EventDecorator implements DayViewDecorator {

        public EventDecorator() {

        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dayArrayList.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new DotSpan(10, Color.BLACK));
        }
    }




    // 글 목록이 있는 date들 불러오기
    private class getPostDate extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(SelectMainActivity.this,
                    "글을 불러옵니다", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();


            if(result.equals("글이 없오") || result == null){

            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String)params[0];
            String postParameters = "id=" + id;

            String serverURL = getString(R.string.sever) + "/FindPostDate.php";

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
                    JSONObject item = jsonArray.getJSONObject(i);

                    String date = item.getString(TAG_DATE);
                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date realdate = transFormat.parse(date);
                    dayArrayList.add(CalendarDay.from(realdate));

                }



            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }


}
