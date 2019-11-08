package com.example.diaryproject.Fragment.PageThree;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.diaryproject.Diary.NoteActivity;
import com.example.diaryproject.Diary.PostActivity;
import com.example.diaryproject.Fragment.PageOne.PageOneFragment;
import com.example.diaryproject.R;

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
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PageThreeFragment extends Fragment {
    private RecyclerAdapterThree mAdapter;
    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();

    private String TAG = "PHPTEST";
    private static final String TAG_JSON="notelist";
    private static final String TAG_NICKNAME ="recv_id";

    private static final String TAG_SENDNID = "send_id";
    private static final String TAG_SENDDATE = "date";
    private static final String TAG_CONTENT = "content";
    private static final String TAG_IDX = "idx";

    String mJsonString;


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
        final String id = getArguments().getString("user_id");  // 현재 사용자 id
        final String nickname = getArguments().getString("user_nick");  // 현재 사용자 nickname


        TextView tw;
        tw = v.findViewById(R.id.NoteMainText);
        tw.setText(nickname + "님의 쪽지함");

        getAllNote task = new getAllNote();
        task.execute(id);

        RecyclerView mRecyclerView = v.findViewById(R.id.NoteRecycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerAdapterThree();
        mRecyclerView.setAdapter(mAdapter);


        // 클릭 이벤트
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), mRecyclerView, new ClickListener(){
            @Override
            public void onClick(View view, int pos) {
                String index = mArrayList.get(pos).get(TAG_IDX);
                String send_nick = mArrayList.get(pos).get(TAG_SENDNID);

                Intent intent = new Intent(getActivity(), NoteActivity.class);
                intent.putExtra("INDEX", index);    // 쪽지 index
                intent.putExtra("USERNICK",nickname);       // 현재 사용자 NICKNAME
                intent.putExtra("USERID", id);
                intent.putExtra("SENDNICK", send_nick);     // 쪽지를 보낸 사용자 nickname
                startActivity(intent);
            }
        }));

        return v;
    }


    //클릭Listener
    public interface ClickListener{
        void onClick(View view, int pos);
    }

    //recyclerview touch 이벤트
    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private GestureDetector detector;
        private PageThreeFragment.ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final PageThreeFragment.ClickListener clickListener) {
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


    // recyclerview 데이터 추가
    private void getData(){

        List<String> listSendId = new ArrayList<>();
        List<String> listContent = new ArrayList<>();
        List<String> listdate = new ArrayList<>();
        List<String> listIdx = new ArrayList<>();

        for (int i = 0; i < mArrayList.size(); i++) {
            listSendId.add(mArrayList.get(i).get(TAG_SENDNID));
            listContent.add(mArrayList.get(i).get(TAG_CONTENT));
            listdate.add(mArrayList.get(i).get(TAG_SENDDATE));
            listIdx.add(mArrayList.get(i).get(TAG_IDX));
        }


        for (int i = 0; i < listSendId.size(); i++) {
            NoteData data = new NoteData();
            data.setNickname(listSendId.get(i));
            data.setContent(listContent.get(i));
            data.setDate(listdate.get(i));
            mAdapter.addItem(data);
        }

        mAdapter.notifyDataSetChanged();

    }


    //DB 연결
    private class getAllNote extends AsyncTask<String, Void, String> {

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
            String recv_id = (String)params[0];
            String postParameters = "recv_id=" + recv_id;
            String serverURL = getString(R.string.sever) + "/NoteList.php";

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

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
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


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

                    String send_id = item.getString(TAG_SENDNID);
                    String date = item.getString(TAG_SENDDATE);
                    String content = item.getString(TAG_CONTENT);
                    String idx = item.getString(TAG_IDX);

                    int content_length = content.length();
                    if(content_length > 10){
                        content = content.substring(0,10) + "....";
                    }

                    hashMap.put(TAG_SENDNID, send_id);
                    hashMap.put(TAG_SENDDATE, date);
                    hashMap.put(TAG_CONTENT, content);
                    hashMap.put(TAG_IDX, idx);

                    mArrayList.add(hashMap);
                }

                getData();

            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }

}
