package com.example.diaryproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.widget.Toast;

import com.example.diaryproject.Fragment.FragmentAdapter;
import com.google.android.material.tabs.TabLayout;
import com.muddzdev.styleabletoast.StyleableToast;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private FragmentAdapter mAdapter;
    private TabLayout mTab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 레이아웃 설정하기
        mViewPager = findViewById(R.id.view_pager);
        mAdapter = new FragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);

        mTab = findViewById(R.id.tabs);
        mTab.setupWithViewPager(mViewPager);

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
