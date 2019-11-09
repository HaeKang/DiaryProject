package com.example.diaryproject.Plan;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;
import com.example.diaryproject.StartActivity;
import com.prolificinteractive.materialcalendarview.CalendarDay;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {
    private static final String TAG = "MyService";
    Notification Notifi;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called");

        if(intent == null){

            return Service.START_STICKY;

        } else{

            String id = intent.getStringExtra("user_id");

            final PlanDateList task = new PlanDateList();
            task.execute(id);
            Log.d(TAG, "Task() called");

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, task.getToday_state());
                    if(task.getToday_state().equals("ok")){
                        // 오늘 일정이 있는 경우

                    } else{

                    }
                }
            }, 1000);

            Log.d("test","됨?");

        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}


