package com.example.diaryproject.Fragment;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.diaryproject.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageOneFragment extends Fragment {

    private ArrayList<Data> mArrayList;
    private RecyclerAdapter mAdapter;

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

        RecyclerView mRecyclerView = v.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        getData();

        return v;
    }

    private void getData(){

        List<String> listTitle = Arrays.asList("test","test1","test2","test3");
        List<String> listNick = Arrays.asList("테스트","테1","테2","테3");
        List<String> listDate = Arrays.asList("0","1","2","3");

        for(int i=0; i< listTitle.size(); i++){
            Data data = new Data();
            data.setTitle(listTitle.get(i));
            data.setNickname(listNick.get(i));
            data.setDate(listDate.get(i));
            mAdapter.addItem(data);
        }

        mAdapter.notifyDataSetChanged();

    }

}
