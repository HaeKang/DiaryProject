package com.example.diaryproject.account;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.diaryproject.R;
import com.example.diaryproject.SelectMainActivity;
import com.muddzdev.styleabletoast.StyleableToast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AddAccount extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private String user_id;
    private String user_nick;
    private String date;
    private String add_type;

    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        Intent getIntent = getIntent();
        user_id = getIntent.getStringExtra("user_id");
        user_nick = getIntent.getStringExtra("user_nickname");
        date = getIntent.getStringExtra("date");

        radioGroup = findViewById(R.id.TypeRadioGroup);


        // 스피너 연결
        Spinner spinner = findViewById(R.id.context_spinner);
        final ArrayAdapter sAdapter = ArrayAdapter.createFromResource(this, R.array.context_spinner, android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(sAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        //라디오 그룹 클릭 리스너
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.addMoney:
                        add_type = "수입";
                        break;
                    case R.id.MinusMoney:
                        add_type = "지출";
                        break;
                }


            }
        });


    }

    // 추가 버튼 클릭
    public void OnClick_addButton( View v )
    {
        Spinner eSpinner = findViewById(R.id.context_spinner);
        EditText ePrice =  findViewById( R.id.edit_money );

        String contexts = eSpinner.getSelectedItem().toString();
        String price_insert = ePrice.getText().toString();


        // DB
        AccountInsert task = new AccountInsert();
        task.execute(getString(R.string.sever)+ "/AccountInsert.php", user_id, contexts, price_insert, date, add_type);

        ePrice.setText( "" );

        // 저장 버튼 누른 후 키보드 안보이게 하기
        InputMethodManager imm =
                (InputMethodManager) getSystemService( Context.INPUT_METHOD_SERVICE );
        imm.hideSoftInputFromWindow( ePrice.getWindowToken(), 0 );



    }



    // 데이터 삽입
    class AccountInsert extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(AddAccount.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            if (result != "") {
                Intent goMain = new Intent(AddAccount.this, AccountMainActivity.class);
                goMain.putExtra("user_id",user_id);
                goMain.putExtra("user_nickname", user_nick);
                startActivity(goMain);
                finish();
            }
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String id = params[1];
            String context = params[2];
            String price = params[3];
            String date = params[4];
            String add_type = params[5];

            String serverURL = params[0];
            String postParameters = "id=" + id + "&context=" + context + "&price=" + price + "&date=" + date + "&add_type=" + add_type;


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

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

    //뒤로버튼 event
    public void onBackPressed(){
        Intent intent = new Intent(AddAccount.this , AccountMainActivity.class);
        intent.putExtra("user_id", user_id);
        intent.putExtra("user_nickname", user_nick);
        startActivity(intent);
        finish();

    }

}
