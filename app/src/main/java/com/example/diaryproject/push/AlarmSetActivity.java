package com.example.diaryproject.push;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.diaryproject.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class AlarmSetActivity extends AppCompatActivity {

    //https://webnautes.tistory.com/1365

    Button ok;
    Button delete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_set);

        ok = findViewById(R.id.time_ok_btn);
        delete = findViewById(R.id.time_delete_btn);

        final TimePicker picker = findViewById(R.id.timePicker);
        picker.setIs24HourView(true);


        SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
        long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());
        Calendar nextNotify = new GregorianCalendar();
        nextNotify.setTimeInMillis(millis);


        final Calendar calendar = Calendar.getInstance();

        // 현재 지정된 시간으로 알람 시간 설정
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hour, hour_24, minute;
                String am_pm;
                if (Build.VERSION.SDK_INT >= 23 ){
                    hour_24 = picker.getHour();
                    minute = picker.getMinute();
                }
                else{
                    hour_24 = picker.getCurrentHour();
                    minute = picker.getCurrentMinute();
                }
                if(hour_24 > 12) {
                    am_pm = "PM";
                    hour = hour_24 - 12;
                }
                else
                {
                    hour = hour_24;
                    am_pm="AM";
                }
                // 현재 지정된 시간으로 알람 시간 설정
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);

                // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                if (calendar.before(Calendar.getInstance())) {
                    calendar.add(Calendar.DATE, 1);
                }

                Date currentDateTime = calendar.getTime();
                String date_text = new SimpleDateFormat("hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                Toast.makeText(getApplicationContext(),date_text + "마다 알람을 보내줍니다!", Toast.LENGTH_SHORT).show();

                //  Preference에 설정한 값 저장
                SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                editor.apply();


                diaryNotification(calendar);

                finish();
            }
        });


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeletediaryNotification(calendar);
                finish();
            }
        });
    }

    public void diaryNotification(Calendar calendar){
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

    public void DeletediaryNotification(Calendar calendar){
        Context mContext;
        mContext = getApplicationContext();

        PackageManager pm = mContext.getPackageManager();
        ComponentName receiver = new ComponentName(mContext, DeviceBootReceiver.class);
        Intent alaramIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alaramIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

           if (PendingIntent.getBroadcast(this, 0, alaramIntent, 0) != null && alarmManager != null) {
               alarmManager.cancel(pendingIntent);
               Toast.makeText(this,"알림이 삭제되었습니다",Toast.LENGTH_SHORT).show();
           }
           pm.setComponentEnabledSetting(receiver,
                   PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                   PackageManager.DONT_KILL_APP);
       }
}
