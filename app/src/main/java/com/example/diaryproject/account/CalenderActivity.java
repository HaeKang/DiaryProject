package com.example.diaryproject.account;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;

import com.example.diaryproject.Diary.MainActivity;
import com.example.diaryproject.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;

public class CalenderActivity extends AppCompatActivity {

    private static final String TAG = "CalendarActivity";
    private CalendarView mCalendarView;
    String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);

        Intent idIntent = getIntent();
        user_id = idIntent.getExtras().getString("user_id");

        mCalendarView = (CalendarView) findViewById(R.id.calendarView);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                String date = i + "/" + (i1+1) + "/" + i2;
                Log.d(TAG, "onSelectDayChange: date : "+date);

                Intent intent = new Intent(CalenderActivity.this, AccountMainActivity.class);
                intent.putExtra("date", date);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
                finish();
            }
        });


    }
}
