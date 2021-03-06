package com.example.diaryproject.Fragment.PageTwo;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.Diary.PostActivity;
import com.example.diaryproject.Diary.WriteActivity;
import com.example.diaryproject.Fragment.Data;
import com.example.diaryproject.Fragment.PageOne.PageOneFragment;
import com.example.diaryproject.Diary.MainActivity;
import com.example.diaryproject.R;
import com.example.diaryproject.account.RecyclerAdapter;
import com.example.diaryproject.sign.SignInActivity;

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
public class PageTwoFragment extends Fragment {

    private RecyclerAdapterTwo mAdapter;
    ArrayList<HashMap<String,String>> mArrayList = new ArrayList<>();

    private String TAG = "PHPTEST";
    private static final String TAG_JSON="postlist";
    private static final String TAG_NICKNAME ="nickname";
    private static final String TAG_TITLE = "title";
    private static final String TAG_POSTID ="post_id";
    private static final String TAG_DATE ="date";
    String mJsonString;

    Spinner post_spn;

    TextView id_tv;
    TextView nick_tv;
    Button write;
    Button logout;

    RecyclerView mRecyclerView;


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

        final View v = inflater.inflate(R.layout.fragment_page_two, container, false);
        nick_tv = v.findViewById(R.id.nick_textview);
        write = v.findViewById(R.id.write);
        logout = v.findViewById(R.id.logout_btn);

        post_spn = v.findViewById(R.id.post_spinner);
        post_spn.setSelection(0);

        final String user_id = getArguments().getString("user_id");
        final String user_nick= getArguments().getString("user_nick");

        nick_tv.setText(user_nick + "님이 작성한 이야기 목록입니다.");

        // recyclerview
        mRecyclerView = v.findViewById(R.id.Recycler_my);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new RecyclerAdapterTwo();
        mRecyclerView.setAdapter(mAdapter);

        // recyclerview 클릭 이벤트
        mRecyclerView.addOnItemTouchListener(new PageOneFragment.RecyclerTouchListener(getActivity(), mRecyclerView, new PageOneFragment.ClickListener(){
            @Override
            public void onClick(View view, int pos) {
                String postid = mArrayList.get(pos).get(TAG_POSTID);
                Intent intent = new Intent(getActivity(), PostActivity.class);
                intent.putExtra("POSTID",postid);
                intent.putExtra("USERID",user_id);
                intent.putExtra("NICKNAME",user_nick);
                startActivity(intent);
            }
        }));

        //글쓰기 버튼 클릭
        write.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View view){
                Intent intent = new Intent(getActivity(), WriteActivity.class);
                intent.putExtra("user_id", user_id);
                intent.putExtra("user_nick",user_nick);
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


        // spinner


        post_spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0){
                    // 전체글
                    ShowAll(user_id, user_nick, v);
                }
                else if(position == 1){
                    // 공개글
                    ShowOpen(user_id, user_nick, v);
                }
                else if(position == 2){
                    // 비공개글
                    ShowPrivate(user_id, user_nick, v);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        return v;

    }

    private void getMyPost(){

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
            Log.d("test",i + "번째 데이터 추가 중");
            Data data = new Data();
            data.setTitle(listTitle.get(i));
            data.setNickname(listNick.get(i));
            data.setPostid(listpostid.get(i));
            data.setDate(listdate.get(i));
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
                    "글을 불러옵니다", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();

            // recycler 중복 방지
            mRecyclerView.setAdapter(null);
            mAdapter = new RecyclerAdapterTwo();
            mRecyclerView.setAdapter(mAdapter);

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
            String state = (String)params[1];
            String postParameters = "id=" + id + "&private=" + state;

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
                    String date = item.getString(TAG_DATE);


                    hashMap.put(TAG_NICKNAME, nickname);
                    hashMap.put(TAG_TITLE, title);
                    hashMap.put(TAG_POSTID,postId);
                    hashMap.put(TAG_DATE, date);

                    mArrayList.add(hashMap);
                }


                getMyPost();


            } catch (JSONException e) {

                Log.d(TAG, "showResult : ", e);
            }

        }
    }

    private void ShowAll(final String id, final String nickname, View v){
        getMyPost task = new getMyPost();
        task.execute(id,"");

    }

    private void ShowOpen(final String id, final String nickname, View v){
        Log.d("test","ShowOpen실행");
        getMyPost task2 = new getMyPost();
        task2.execute(id,"0");
    }

    private void ShowPrivate(final String id, final String nickname, View v){
        getMyPost task3 = new getMyPost();
        task3.execute(id,"1");
    }

}
