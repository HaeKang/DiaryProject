package com.example.diaryproject.account;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.example.diaryproject.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

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

public class ChartActivity extends AppCompatActivity {

    private PieChart chart;
    private String user_id;
    private String user_nick;
    private String date;
    private String year;
    private String month;
    private String search_date;

    String mJsonString;
    private static final String TAG_JSON="webnautes";
    private String TAG_FOOD = "sum_food";
    private String TAG_CLOTH  ="sum_cloth";
    private String TAG_TRAFFIC = "sum_traffic";
    private String TAG_BOOK = "sum_book";
    private String TAG_ELSE = "sum_else";

    private float sum_food = 0;
    private float sum_book = 0;
    private float sum_else = 0;
    private float sum_cloth = 0;
    private float sum_traffic = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        Intent getIntent = getIntent();
        user_id = getIntent.getStringExtra("user_id");
        user_nick = getIntent.getExtras().getString("user_nickname");
        date = getIntent.getStringExtra("date");

        String[] array = date.split("/");
        year = array[0];
        month = array[1];
        search_date = year + "/" + month + "/%";

        ValueSum task = new ValueSum();
        task.execute(user_id, search_date);

    }




    // 총합 가격
    private class ValueSum extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ChartActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();


            if(result.equals("error")){
                Toast.makeText(getApplicationContext(),result,Toast.LENGTH_LONG).show();
            }


            if (result == null || result.equals("")){
                Toast.makeText(getApplicationContext(),errorString,Toast.LENGTH_LONG).show();
            }
            else {
                mJsonString = result;
                showResult();
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String id = params[0];
            String date = params[1];

            String serverURL = getString(R.string.sever) + "/AccountPieSum.php";
            String postParameters = "id=" + id + "&date=" + date;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("PHPTEST", "response code - " + responseStatusCode);

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

                Log.d("PHPTEST", "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }

        }

        private void showResult(){
            try {
                JSONObject jsonObject = new JSONObject(mJsonString);
                JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

                for(int i=0; i<jsonArray.length(); i++){

                    JSONObject item = jsonArray.getJSONObject(i);
                    if(i == 0){
                        String get_sumfood = item.getString(TAG_FOOD);
                        if(!get_sumfood.isEmpty()) {
                            sum_food = Integer.parseInt(get_sumfood);
                        }
                    }

                    if(i == 1){
                        String get_sumbook = item.getString(TAG_BOOK);
                        if(!get_sumbook.isEmpty()) {
                            sum_book = Integer.parseInt(get_sumbook);
                        }
                    }

                    if(i == 2){
                        String get_sumelse = item.getString(TAG_ELSE);
                        if(!get_sumelse.isEmpty()) {
                            sum_else = Integer.parseInt(get_sumelse);
                        }
                    }
                    if( i == 3 ){
                        String get_sumcloth = item.getString(TAG_CLOTH);
                        if(!get_sumcloth.isEmpty()) {
                            sum_cloth = Integer.parseInt(get_sumcloth);
                        }
                    }
                    if( i == 4 ){
                        String get_sumtraffic = item.getString(TAG_TRAFFIC);
                        if(!get_sumtraffic.isEmpty()) {
                            sum_traffic = Integer.parseInt(get_sumtraffic);
                        }
                    }
                }

                // PIE 차트
                chart = findViewById(R.id.piechart);

                chart.setUsePercentValues(false);
                chart.getDescription().setEnabled(false);
                chart.setExtraOffsets(5,10,5,5);

                chart.setDragDecelerationFrictionCoef(0.95f);

                chart.setDrawHoleEnabled(false);
                chart.setHoleColor(Color.BLACK);
                chart.setTransparentCircleRadius(61f);

                ArrayList<PieEntry> yValues = new ArrayList<>();


                if(sum_food > 0){
                    yValues.add(new PieEntry(sum_food,"음식"));
                }
                if(sum_book > 0){
                    yValues.add(new PieEntry(sum_book,"도서"));
                }
                if(sum_cloth > 0){
                    yValues.add(new PieEntry(sum_cloth,"의류"));
                }
                if(sum_traffic > 0){
                    yValues.add(new PieEntry(sum_traffic,"교통"));
                }
                if(sum_else > 0){
                    yValues.add(new PieEntry(sum_else,"기타"));
                }

                Description description = new Description();
                description.setText(year +"년 " + month + "월" + " 지출 패턴 분석");
                description.setTextSize(15);
                chart.setDescription(description);
                chart.animateY(1000, Easing.EaseInOutCubic); //애니메이션

                PieDataSet dataSet = new PieDataSet(yValues,"종류");
                dataSet.setSliceSpace(3f);
                dataSet.setSelectionShift(5f);
                dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

                PieData data = new PieData((dataSet));
                data.setValueTextSize(10f);
                data.setValueTextColor(Color.BLACK);

                chart.setData(data);

            } catch (JSONException e) {

                Log.d("PHPTEST", "showResult : ", e);
            }

        }
    }


    //뒤로버튼 event
    public void onBackPressed(){
        Intent intent = new Intent(ChartActivity.this , AccountMainActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("user_nickname", user_nick);
        startActivity(intent);
        finish();
    }

}