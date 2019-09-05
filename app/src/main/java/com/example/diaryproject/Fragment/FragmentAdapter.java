package com.example.diaryproject.Fragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentAdapter extends FragmentPagerAdapter {

    private static int PAGE_NUMBER = 3;

    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return PageOneFragment.newInstance();
            case 1:
                return PageTwoFragment.newInstance();
            case 2:
                return PageThreeFragment.newInstance();
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return PAGE_NUMBER;
    }

    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "첫번째 탭";
            case 1:
                return "두번째";
            case 2:
                return "세번째";
                default:
                    return null;
        }
    }

}
