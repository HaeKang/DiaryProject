package com.example.diaryproject.Fragment;

import com.example.diaryproject.Fragment.PageFour.PageFourFragment;
import com.example.diaryproject.Fragment.PageOne.PageOneFragment;
import com.example.diaryproject.Fragment.PageThree.PageThreeFragment;
import com.example.diaryproject.Fragment.PageTwo.PageTwoFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class FragmentAdapter extends FragmentPagerAdapter {

    private static int PAGE_NUMBER = 4;
    private String id_a;
    private String nick_a;

    public FragmentAdapter(FragmentManager fm, String id, String nick) {
        super(fm);
        id_a = id;
        nick_a = nick;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return PageOneFragment.newInstance(id_a,nick_a);
            case 1:
                return PageTwoFragment.newInstance(id_a,nick_a);
            case 2:
                return PageThreeFragment.newInstance(id_a,nick_a);
            case 3:
                return PageFourFragment.newInstance(id_a,nick_a);
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
            case 3:
                return "네번째";
            default:
                    return null;
        }
    }

}
