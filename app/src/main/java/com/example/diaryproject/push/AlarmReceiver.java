package com.example.diaryproject.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;
import com.example.diaryproject.StartActivity;

import java.util.Calendar;

import androidx.core.app.NotificationCompat;
import static android.content.Context.MODE_PRIVATE;

public class AlarmReceiver extends BroadcastReceiver {

    private SharedPreferences loginInfo;

    @Override
    public void onReceive(final Context context, Intent intent) {

        // db받아오는고
        loginInfo = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        String auto_loginid = loginInfo.getString("id", null);

        if (auto_loginid == null) {

        } else {
            final PlanDateList task = new PlanDateList();
            task.execute(auto_loginid);

            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(context, StartActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pending = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder.setSmallIcon(R.drawable.ic_home_black_24dp);

                final String channelName = "매일 알람 채널";
                String description = "매일 정해진 시간에 알람";
                int importance = NotificationManager.IMPORTANCE_HIGH;

                final NotificationChannel channel = new NotificationChannel("default", channelName, importance);
                channel.setDescription(description);

                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }

            } else {
                builder.setSmallIcon(R.mipmap.ic_launcher);
            }

            builder.setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setTicker("{Time to watch some cool stuff!}")
                    .setContentTitle("오늘 일정이 있습니다")
                    .setContentText("오늘의 일정을 확인해보세요")
                    .setContentInfo("INFO")
                    .setContentIntent(pending);


            if (notificationManager != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("test", task.getToday_state());
                        if (task.getToday_state().equals("ok")) {
                            Log.d("test", "여기옴2");
                            notificationManager.notify(1234, builder.build());
                        }
                    }
                }, 1000);

                Calendar nextNotifyTime = Calendar.getInstance();
                nextNotifyTime.add(Calendar.DATE, 1);

                SharedPreferences.Editor editor = context.getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                editor.putLong("nextNotifyTime", nextNotifyTime.getTimeInMillis());
                editor.apply();

            }

        }
    }
}
