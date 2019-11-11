package com.example.diaryproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.Diary.MainActivity;
import com.example.diaryproject.Plan.PlanActivity;
import com.example.diaryproject.account.AccountMainActivity;
import com.example.diaryproject.push.AlarmReceiver;
import com.example.diaryproject.push.DeviceBootReceiver;
import com.example.diaryproject.sign.SignInActivity;
import com.muddzdev.styleabletoast.StyleableToast;
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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class SelectMainActivity extends AppCompatActivity {

    private String id;
    private String nickname;
    private String select_date;

    ImageButton diary;
    ImageButton cal;
    Button logout;
    TextView plantext;


    ArrayList<HashMap<String,String>> testArray = new ArrayList<>();

    ArrayList<String> listDate = new ArrayList<>();
    ArrayList<String> listContent = new ArrayList<>();
    ArrayList<String> listIndex = new ArrayList<>();

    HashSet<CalendarDay> dayArrayList = new HashSet<>();

    String mJsonString;
    private String TAG = "PHPTEST";
    private static final String TAG_JSON="plan";
    private static final String TAG_DATE ="date";
    private static final String TAG_CONTENT="content";
    private static final String TAG_INDEX="idx";

    public static Activity Select_Main_Activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_main);

        Select_Main_Activity = SelectMainActivity.this;


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
                finish();
            }
        });

        cal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SelectMainActivity.this, AccountMainActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nickname", nickname);
                startActivity(intent);
                finish();
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

        getPlanDate task = new getPlanDate();
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
                int year = date.getYear();
                int month = date.getMonth() + 1;
                int day = date.getDay();

                String select_date = year + "-" + month + "-" + day;

                // 일정 있을 때
                if(listDate.contains(select_date)){
                    ArrayList<String> content = getContent(testArray,select_date);
                    ArrayList<String> idx = getIdx(testArray, select_date);

                    Intent intent = new Intent(SelectMainActivity.this, PlanActivity.class);
                    intent.putExtra("date", select_date);
                    intent.putExtra("user_id", id);
                    intent.putExtra("user_nickname", nickname);
                    intent.putExtra("content", content);    // 그 날에 해당하는 content들 전달
                    intent.putExtra("idx", idx);
                    startActivity(intent);

                } else{
                    // 일정 없을 때
                    Intent intent = new Intent(SelectMainActivity.this, PlanActivity.class);
                    intent.putExtra("date", select_date);
                    intent.putExtra("user_id", id);
                    intent.putExtra("user_nickname", nickname);
                    startActivity(intent);
                }




            }
        });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                materialCalendarView.addDecorators(
                        new EventDecorator()
                );

            }
        }, 1000);


        // Noti 알람
        // 알람 시간 설정
        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());
        Calendar nextNotify = new GregorianCalendar();
        nextNotify.setTimeInMillis(millis);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        if(calendar.before(Calendar.getInstance())){
            calendar.add(Calendar.DATE, 1);
        }

        SharedPreferences.Editor editor = getSharedPreferences("daily alarm" , MODE_PRIVATE).edit();
        editor.putLong("nextNotifyTime", calendar.getTimeInMillis());
        editor.apply();

        diaryNotification(calendar);

    }

    void diaryNotification(Calendar calendar){
        Context mContext;
        mContext = getApplicationContext();

        PackageManager pm = mContext.getPackageManager();
        ComponentName receiver = new ComponentName(mContext, DeviceBootReceiver.class);
        Intent alaramIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alaramIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        // 부팅 후 실행되는 리시버 사용 가능하도록
        pm.setComponentEnabledSetting(receiver, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }


   private ArrayList<String> getContent(ArrayList<HashMap<String,String>> testArray, String date){
       ArrayList<String> listContent = new ArrayList<>();
       for(int i=0; i<testArray.size(); i++){
           if(date.equals(testArray.get(i).get("date"))){
               listContent.add(testArray.get(i).get("content"));
           }
       }

        return listContent;
   }

    private ArrayList<String> getIdx(ArrayList<HashMap<String,String>> testArray, String date){
        ArrayList<String> listIndex = new ArrayList<>();
        for(int i=0; i<testArray.size(); i++){
            if(date.equals(testArray.get(i).get("date"))){
                listIndex.add(testArray.get(i).get("index"));
            }
        }

        return listIndex;
    }


    // 오늘 날짜
    static public String getToday_date(){
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-d", Locale.KOREA);
        Date currentTime = new Date();
        String Today_day = mSimpleDateFormat.format(currentTime);
        return Today_day;
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
    public class getPlanDate extends AsyncTask<String, Void, String> {

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


            if (result.equals("글이 없오") || result == null) {

            } else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = (String) params[0];
            String postParameters = "id=" + id;

            String serverURL = getString(R.string.sever) + "/FindPlan.php";

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
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
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

        private void showResult() {
            try {
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
                testArray.clear();  // 총 array
                dayArrayList.clear();   // 날짜 deco위한 hashmap
                listDate.clear();
                listContent.clear();
                listIndex.clear();

                for (int i = 0; i < jsonArray.length(); i++) {

                    HashMap<String,String> mHashmap = new HashMap<>();
                    mHashmap.clear();

                    JSONObject item = jsonArray.getJSONObject(i);
                    String date = item.getString(TAG_DATE);
                    String content = item.getString(TAG_CONTENT);
                    String index = item.getString(TAG_INDEX);

                    mHashmap.put("date", date);
                    mHashmap.put("content", content);
                    mHashmap.put("index", index);

                    listDate.add(date);
                    listContent.add(content);
                    listIndex.add(index);

                    testArray.add(mHashmap);

                    SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-d");
                    Date realdate = transFormat.parse(date);
                    dayArrayList.add(CalendarDay.from(realdate));

                }

                Log.d("test",testArray.toString());

                // 만약 오늘 일정이 있으면
                String content = "";

                for (int i = 0; i < testArray.size(); i++) {
                    if(testArray.get(i).get("date").equals(getToday_date())){
                        content += testArray.get(i).get("content") + "\n";
                    }
                }

                plantext = findViewById(R.id.planshow_text);
                plantext.setText("오늘" + id + "님의 일정 \n" + content);


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
    }

    //뒤로버튼 event
    private long time = 0;
    public void onBackPressed(){
        if(System.currentTimeMillis()-time >= 2000){
            time = System.currentTimeMillis();
            StyleableToast.makeText(getApplicationContext(), "뒤로가기 버튼을 한번 더 누르면 종료합니다.", Toast.LENGTH_LONG,R.style.backtoast).show();
        } else if(System.currentTimeMillis()-time < 2000){
            finish();
        }
    }

}




