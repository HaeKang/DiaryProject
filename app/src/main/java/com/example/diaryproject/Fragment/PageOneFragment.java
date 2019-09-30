package com.example.diaryproject.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.diaryproject.Diary.PostActivity;
import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.sign.SignInActivity;
import com.muddzdev.styleabletoast.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageOneFragment extends Fragment {

    private RecyclerAdapter mAdapter;
    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();

    private String TAG = "PHPTEST";
    private static final String TAG_JSON="postlist";
    private static final String TAG_NICKNAME ="nickname";
    private static final String TAG_TITLE = "title";
    private static final String TAG_POSTID ="post_id";
    private static final String TAG_DATE ="date";
    String mJsonString;


    public PageOneFragment() {
        // Required empty public constructor
    }

    // 생성자
    public static PageOneFragment newInstance(String p1, String p2){
        Bundle args = new Bundle();
        args.putString("user_id", p1);
        args.putString("user_nick",p2);
        PageOneFragment fragment = new PageOneFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_page_one, container, false);

        final String id = getArguments().getString("user_id");
        final String nickname = getArguments().getString("user_nick");

        getAllPost task = new getAllPost();
        task.execute();

        RecyclerView mRecyclerView = v.findViewById(R.id.recyclerView);

        // grid layout
        //GridLayoutManager mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        //mRecyclerView.setLayoutManager(mGridLayoutManager);

        //일반 layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);


        mAdapter = new RecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // list 클릭 event
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new ClickListener(){
            @Override
            public void onClick(View view, int pos) {
                String postid = mArrayList.get(pos).get(TAG_POSTID);
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra("POSTID",postid);
                intent.putExtra("USERID",id);
                intent.putExtra("NICKNAME",nickname);
                startActivity(intent);
            }
        }));


        return v;
    }


    // recyclerview 데이터 추가
    private void getData(){

        List<String> listTitle = new ArrayList<>();
        List<String> listNick = new ArrayList<>();
        List<String> listpostid = new ArrayList<>();
        List<String> listdate = new ArrayList<>();

        for (int i = 0; i < mArrayList.size(); i++) {
            listTitle.add(mArrayList.get(i).get(TAG_TITLE));
            listNick.add(mArrayList.get(i).get(TAG_NICKNAME));
            listpostid.add(mArrayList.get(i).get(TAG_POSTID));
            listdate.add(mArrayList.get(i).get(TAG_DATE));
        }


        for (int i = 0; i < listTitle.size(); i++) {
            Data data = new Data();
            data.setTitle(listTitle.get(i));
            data.setNickname(listNick.get(i));
            data.setPostid(listpostid.get(i));
            data.setDate(listdate.get(i));
            mAdapter.addItem(data);
        }

            mAdapter.notifyDataSetChanged();

    }


    //클릭Listener
    public interface ClickListener{
        void onClick(View view, int pos);
    }

    // recyclerview 클릭 class
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector detector;
        private PageOneFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final PageOneFragment.ClickListener clickListener) {
            this.clickListener = clickListener;
            detector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                public boolean onSingleTapUp(MotionEvent e){
                    return true;
                }
            });

        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && detector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
                return true;
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }
    }


    //DB 연결
    private class getAllPost extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(),
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();


            if(result.equals("글이 없오") || result == null){

            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = getString(R.string.sever) + "/PostList.php";

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }

        private void showResult(){
            try {
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
                mArrayList.clear();

                for(int i=0; i<jsonArray.length(); i++){
                    HashMap<String, String> hashMap = new HashMap<>();
                    JSONObject item = jsonArray.getJSONObject(i);

                    String nickname = item.getString(TAG_NICKNAME);
                    String title = item.getString(TAG_TITLE);
                    int post_id  = item.getInt(TAG_POSTID);
                    String postId = Integer.toString(post_id);
                    String date = item.getString(TAG_DATE);

                    hashMap.put(TAG_NICKNAME, nickname);
                    hashMap.put(TAG_TITLE, title);
                    hashMap.put(TAG_POSTID,postId);
                    hashMap.put(TAG_DATE, date);

                    mArrayList.add(hashMap);
                }


                getData();


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }



}





