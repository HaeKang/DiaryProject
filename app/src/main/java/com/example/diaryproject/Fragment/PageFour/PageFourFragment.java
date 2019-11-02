package com.example.diaryproject.Fragment.PageFour;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.diaryproject.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;
import java.util.Date;


public class PageFourFragment extends Fragment {

    Date today = new Date(System.currentTimeMillis());

    public PageFourFragment() {
        // Required empty public constructor
    }


    public static PageFourFragment newInstance(String p1, String p2){
        PageFourFragment fragment = new PageFourFragment();
        Bundle args = new Bundle();
        args.putString("user_id", p1);
        args.putString("user_nick",p2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_page_four, container, false);
        String id = getArguments().getString("user_id");    // user id
        String nickname = getArguments().getString("user_nick");    // user nickname


        MaterialCalendarView materialCalendarView = v.findViewById(R.id.four_calendar);

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
                new SaturdayDecorator());

        // 클릭 event
        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
                CalendarDay today = CalendarDay.today(); // 오늘날짜
            }
        });





        return v;
    }

    // 일요일 빨간색상
    public class SundayDecorator implements DayViewDecorator{

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



}
