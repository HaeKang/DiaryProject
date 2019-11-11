package com.example.diaryproject.push;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;



public class PlanDateList extends AsyncTask<String, Void, String> {

    String errorString = null;
    String mJsonString;
    public String today_state = "no";

    public String getToday_state(){
        return today_state;
    }

    private void setToday_state(String state){
        this.today_state = state;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (result.equals("글이 없오") || result == null) {

        } else {
            mJsonString = result;
            showResult();
        }
    }


    @Override
    protected String doInBackground(String... params) {

        String id = (String) params[0];
        String postParameters = "id=" + id;

        String serverURL = "http://cutesami.cafe24.com/FindPlan.php";

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
            Log.d("test", "POST response code - " + responseStatusCode);

            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = httpURLConnection.getInputStream();
            } else {
                inputStream = httpURLConnection.getErrorStream();
            }


            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }


            bufferedReader.close();


            return sb.toString();


        } catch (Exception e) {

            Log.d("TEST", "InsertData: Error ", e);
            errorString = e.toString();

            return null;
        }

    }

    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("plan");

            for (int i = 0; i < jsonArray.length(); i++) {

                HashMap<String,String> mHashmap = new HashMap<>();
                mHashmap.clear();

                JSONObject item = jsonArray.getJSONObject(i);
                String date = item.getString("date");

                if(date.equals(getToday_date())){
                    setToday_state("ok");
                }

            }


        } catch (JSONException e) {

            Log.d("test", "showResult : ", e);

        }

    }

    private String getToday_date() {
        SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-d", Locale.KOREA);
        Date currentTime = new Date();
        String Today_day = mSimpleDateFormat.format(currentTime);
        return Today_day;
    }
}