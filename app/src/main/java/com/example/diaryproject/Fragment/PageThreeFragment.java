package com.example.diaryproject.Fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.diaryproject.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageThreeFragment extends Fragment {


    public PageThreeFragment() {
        // Required empty public constructor
    }


    public static PageThreeFragment newInstance(String p1, String p2){
        PageThreeFragment fragment = new PageThreeFragment();
        Bundle args = new Bundle();
        args.putString("user_id", p1);
        args.putString("user_nick",p2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_page_three, container, false);
        String id = getArguments().getString("user_id");
        String nickname = getArguments().getString("user_nick");




        return v;
    }

}
