package com.example.diaryproject.Plan;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.example.diaryproject.push.PlanDateList;

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


