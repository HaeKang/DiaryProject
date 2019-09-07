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
public class PageOneFragment extends Fragment {


    public PageOneFragment() {
        // Required empty public constructor
    }

    public static PageOneFragment newInstance(){
        Bundle args = new Bundle();
        PageOneFragment fragment = new PageOneFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_one, container, false);
        return v;
    }

}
