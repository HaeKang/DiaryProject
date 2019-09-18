package com.example.diaryproject.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.Diary.PostActivity;
import com.example.diaryproject.Diary.WriteActivity;
import com.example.diaryproject.MainActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.StartActivity;
import com.example.diaryproject.sign.SignInActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

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
public class PageTwoFragment extends Fragment {

    private RecyclerAdapterTwo mAdapter;
    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();

    private String TAG = "PHPTEST";
    private static final String TAG_JSON="postlist";
    private static final String TAG_NICKNAME ="nickname";
    private static final String TAG_TITLE = "title";
    private static final String TAG_POSTID ="post_id";
    String mJsonString;



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
        Button logout = v.findViewById(R.id.logout_btn);


        final String id = getArguments().getString("user_id");
        final String nickname = getArguments().getString("user_nick");

        id_tv.setText(id);
        nick_tv.setText(nickname);

        //글쓰기 버튼 클릭
        write.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                intent.putExtra("user_id", id);
                intent.putExtra("user_nick",nickname);
                startActivity(intent);
            }
        });

        // 로그아웃
        logout.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                SharedPreferences loginInfo = getActivity().getSharedPreferences("setting",0);
                SharedPreferences.Editor editor = loginInfo.edit();
                editor.clear();
                editor.commit();

                Intent intent = new Intent(getActivity(), SignInActivity.class);
                startActivity(intent);
                ((MainActivity)getActivity()).finishMain();

                Toast.makeText(getActivity(),"로그아웃 했습니다.",Toast.LENGTH_LONG).show();

            }
        });


        //글 목록 보기
        getMyPost task = new getMyPost();
        task.execute(id);

        RecyclerView mRecyclerView = v.findViewById(R.id.Recycler_my);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerAdapterTwo();
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnItemTouchListener(new PageOneFragment.RecyclerTouchListener(getActivity(), mRecyclerView, new PageOneFragment.ClickListener(){
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



    private void getMyPost(){

        List<String> listTitle = new ArrayList<>();
        List<String> listNick = new ArrayList<>();
        List<String> listpostid = new ArrayList<>();

        for (int i = 0; i < mArrayList.size(); i++) {
            listTitle.add(mArrayList.get(i).get(TAG_TITLE));
            listNick.add(mArrayList.get(i).get(TAG_NICKNAME));
            listpostid.add(mArrayList.get(i).get(TAG_POSTID));
        }


        for (int i = 0; i < listTitle.size(); i++) {
            Data data = new Data();
            data.setTitle(listTitle.get(i));
            data.setNickname(listNick.get(i));
            data.setPostid(listpostid.get(i));
            mAdapter.addItem(data);
        }

        mAdapter.notifyDataSetChanged();

    }



    //DB 연결
    private class getMyPost extends AsyncTask<String, Void, String> {

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

            String id = (String)params[0];
            String postParameters = "id=" + id;

            String serverURL = getString(R.string.sever) + "/MyPostList.php";

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

                    String nickname = item.getString(TAG_NICKNAME);
                    String title = item.getString(TAG_TITLE);
                    int post_id  = item.getInt(TAG_POSTID);
                    String postId = Integer.toString(post_id);

                    hashMap.put(TAG_NICKNAME, nickname);
                    hashMap.put(TAG_TITLE, title);
                    hashMap.put(TAG_POSTID,postId);

                    mArrayList.add(hashMap);
                }


                getMyPost();


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }

}
