package com.example.diaryproject.Fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.Diary.WriteActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.StartActivity;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageTwoFragment extends Fragment {

    public PageTwoFragment() {

    }


    // id, nickname 불러오기
    public static PageTwoFragment newInstance(String p1, String p2){
        PageTwoFragment fragment = new PageTwoFragment();
        Bundle args = new Bundle();
        args.putString("user_id", p1);
        args.putString("user_nick",p2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_page_two, container, false);
        TextView id_tv = v.findViewById(R.id.id_textview);
        TextView nick_tv = v.findViewById(R.id.nick_textview);
        Button write = v.findViewById(R.id.write);

        final String id = getArguments().getString("user_id");
        final String nickname = getArguments().getString("user_nick");

        id_tv.setText(id);
        nick_tv.setText(nickname);

        write.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nick",nickname);
                startActivity(intent);
            }
        });

        return v;

    }


}
