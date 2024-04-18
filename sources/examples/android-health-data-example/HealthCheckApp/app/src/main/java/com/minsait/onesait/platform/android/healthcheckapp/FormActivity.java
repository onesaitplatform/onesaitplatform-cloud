/**
 * Copyright Indra Soluciones Tecnologías de la Información, S.L.U.
 * 2013-2019 SPAIN
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.minsait.onesait.platform.android.healthcheckapp;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class FormActivity extends AppCompatActivity {

    EditText mHeightField;
    EditText mWeightField;
    EditText mSysPressureField;
    EditText mDiaPressureField;
    EditText mCommentField;
    String mAccessToken;
    String mUsername;

    SharedPreferences preferences;
    private String pref_env;

    private void loadPreferences(){
        pref_env = preferences.getString("EnvSelect","rancher.sofia4cities.com");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences();

        mAccessToken = getIntent().getStringExtra("accessToken");
        mUsername =  getIntent().getStringExtra("username");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mUsername);


        mHeightField = (EditText) findViewById(R.id.height);
        mWeightField = (EditText) findViewById(R.id.weight);
        mSysPressureField = (EditText) findViewById(R.id.sys_pressure);
        mDiaPressureField = (EditText) findViewById(R.id.dia_pressure);
        mCommentField = (EditText) findViewById(R.id.comments);

        Button mButton = (Button) findViewById(R.id.store_form_button);
        mButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mHeightField.getText().length()!=0 &&
                        mWeightField.getText().length()!=0 &&
                        mSysPressureField.getText().length()!=0 &&
                        mDiaPressureField.getText().length()!=0 &&
                        mCommentField.getText().length()!=0){

                    new PostToS4CAsyncTask(mAccessToken,
                            mHeightField.getText().toString(),
                            mWeightField.getText().toString(),
                            mSysPressureField.getText().toString(),
                            mDiaPressureField.getText().toString(),
                            mCommentField.getText().toString()
                    ).execute((Void) null);

                }
                else{
                    Toast.makeText(FormActivity.this,"Please fill-in all fields",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public class PostToS4CAsyncTask extends AsyncTask<Void, Void, Integer> {

        private String mAccessToken = "";
        private String mHeight = "";
        private String mWeight = "";
        private String mSys = "";
        private String mDia = "";
        private String mComments = "";

        PostToS4CAsyncTask(String accessToken,
                           String height,
                           String weight,
                           String sys_press,
                           String dia_press,
                           String comments) {
            mAccessToken = accessToken;
            mHeight = height;
            mWeight = weight;
            mSys = sys_press;
            mDia = dia_press;
            mComments = comments;
        }
        @Override
        protected Integer doInBackground(Void... voids) {

            String urlS ="http://"+pref_env+"/api-manager/server/api/v1/citizenInterface";
            URL url = null;
            int responseCode = 500;
            try {
                url = new URL(urlS);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try{
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setAllowUserInteraction(false);
                connection.setUseCaches(false);
                connection.setRequestProperty("Authorization", "Bearer "+mAccessToken);
                connection.setRequestProperty("Content-Type","application/json");

                JSONObject healthFrame = new JSONObject();
                JSONObject citizenHealthData =  new JSONObject();
                citizenHealthData.put("height",Integer.parseInt(mHeight));
                citizenHealthData.put("weight",Integer.parseInt(mWeight));
                citizenHealthData.put("sys_pressure",Integer.parseInt(mSys));
                citizenHealthData.put("dia_pressure",Integer.parseInt(mDia));
                citizenHealthData.put("comments",mComments);
                healthFrame.put("citizenHealthData",citizenHealthData);

                //{"citizenHealthData":{ "height":1,"weight":28.6,"sys_pressure":1,"dia_pressure":1,"comments":"string"}}

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(healthFrame.toString());
                writer.flush();
                writer.close();
                os.close();

                connection.connect();
                responseCode = connection.getResponseCode();

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    final StringBuilder output = new StringBuilder("Request URL " + url);
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String line = "";
                    StringBuilder responseOutput = new StringBuilder();
                    while((line = br.readLine()) != null ) {
                        responseOutput.append(line);
                    }
                    br.close();
                    connection.disconnect();

                    //Toast.makeText(mActivity.getBaseContext(),"Form stored successfully",Toast.LENGTH_SHORT);
                }
                else{
                    int code = connection.getResponseCode();
                    String msg = connection.getResponseMessage();
                    String dummy = connection.getRequestMethod();
                    //Toast.makeText(mActivity.getBaseContext(),"Connection ERROR",Toast.LENGTH_SHORT);
                }

            }
            catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK){
                mHeightField.setText("");
                mWeightField.setText("");
                mSysPressureField.setText("");
                mDiaPressureField.setText("");
                mCommentField.setText("");
                mHeightField.requestFocus();
                Toast.makeText(FormActivity.this,"Form stored successfully",Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(FormActivity.this,"Could not connect to S4C Platform",Toast.LENGTH_SHORT).show();
            }
        }
    }
}
