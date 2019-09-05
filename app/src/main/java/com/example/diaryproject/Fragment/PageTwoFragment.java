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
public class PageTwoFragment extends Fragment {


    public PageTwoFragment() {
        // Required empty public constructor
    }

    public static PageTwoFragment newInstance(){
        Bundle args = new Bundle();
        PageTwoFragment fragment = new PageTwoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_page_two, container, false);
    }

}
