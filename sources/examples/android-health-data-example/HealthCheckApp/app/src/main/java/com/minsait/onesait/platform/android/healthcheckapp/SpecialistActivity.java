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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
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
import java.util.ArrayList;

public class SpecialistActivity extends AppCompatActivity implements RequestAdapter.ListItemClickListener  {

    private RequestAdapter mAdapter;
    private RecyclerView mItemsRV;
    private ArrayList<RequestData> mRequestArray = new ArrayList<>();

    private final int MAX_RETRIES = 3;
    int mGetRetries = MAX_RETRIES;

    String mAccessToken = "";
    String mUsername = "";
    String mInput = "";

    int clickedElement = 0;
    String clickedId = "";

    SharedPreferences preferences;
    private String pref_env;

    private void loadPreferences(){
        pref_env = preferences.getString("EnvSelect","rancher.sofia4cities.com");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specialist);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        loadPreferences();

        mAccessToken = getIntent().getStringExtra("accessToken");
        mUsername = getIntent().getStringExtra("username");

        getSupportActionBar().setTitle(mUsername);

        mItemsRV = (RecyclerView) findViewById(R.id.list_spec);
        mItemsRV.setLayoutManager(new LinearLayoutManager(this));
        mItemsRV.setHasFixedSize(true);
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
    protected void onResume() {
        super.onResume();
        loadPreferences();
        new GetFromS4CAsyncTask().execute((Void) null);
    }

    @Override
    public void onListItemClick(int clickedItemId) {
        clickedElement = clickedItemId;
        //createAndShowAlertDialog();
        Intent mIntent = new Intent();
        mIntent = new Intent(SpecialistActivity.this,AdviseActivity.class);
        mIntent.putExtra("accessToken",mAccessToken);
        mIntent.putExtra("username",mUsername);
        startActivity(mIntent);
    }

    public class GetFromS4CAsyncTask extends AsyncTask<Void, Void, Integer> {

        JSONArray ja = null;

        @Override
        protected Integer doInBackground(Void... voids) {

            String urlS ="http://"+pref_env+"/api-manager/server/api/v1/specialistInterface/PendingRequests";
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
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.setAllowUserInteraction(false);
                connection.setUseCaches(false);
                connection.setRequestProperty("Authorization", "Bearer "+mAccessToken);
                connection.setRequestProperty("Accept","application/json");

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

                    ja =  new JSONArray(responseOutput.toString());
                    mRequestArray = loadRequestDataFromJson(ja);


                }
                else{
                    int code = connection.getResponseCode();
                    String msg = connection.getResponseMessage();
                    String dummy = connection.getRequestMethod();
                }

            }
            catch (ProtocolException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer responseCode) {
            super.onPostExecute(responseCode);
            if(responseCode == HttpURLConnection.HTTP_OK){
                loadRequestData();
            }
            else{
                mGetRetries--;
                if(mGetRetries == 0){
                    mGetRetries = MAX_RETRIES;
                    Toast.makeText(SpecialistActivity.this,"Could not connect to S4C Platform",Toast.LENGTH_SHORT).show();
                }
                else {
                    new GetFromS4CAsyncTask().execute((Void) null);
                }
            }
        }
    }
    public ArrayList<RequestData> loadRequestDataFromJson(JSONArray arrayFromS4c){
        ArrayList<RequestData> mRequestData = new ArrayList<>(arrayFromS4c.length()+1);
        JSONObject data = new JSONObject();
        JSONObject contextData = new JSONObject();

        for(int i=0; i<arrayFromS4c.length();i++){
            RequestData dummyRequestData = new RequestData();
            try{
                data = arrayFromS4c.getJSONObject(i).getJSONObject("value").getJSONObject("specialistInbox");
                contextData = arrayFromS4c.getJSONObject(i).getJSONObject("value").getJSONObject("contextData");
                clickedId = arrayFromS4c.getJSONObject(i).getJSONObject("value").getString("_id");

                dummyRequestData.setUsername(data.getString("patient"));
                dummyRequestData.setPending(data.getString("pending"));

            }
            catch (JSONException e){
                e.printStackTrace();
            }
            mRequestData.add(dummyRequestData);
        }

        return mRequestData;
    }

    public void loadRequestData(){
        mItemsRV.setAdapter(new RequestAdapter(mRequestArray,SpecialistActivity.this));
    }

    private void createAndShowAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please input your recommendation");
        // Set up the input
        final TextInputEditText input = new TextInputEditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mInput = input.getText().toString().trim();
                new PostToInboxAsyncTask().execute((Void) null);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class PostToInboxAsyncTask extends AsyncTask<Void, Void, Integer> {


        @Override
        protected Integer doInBackground(Void... voids) {

            String urlS ="http://"+pref_env+"/api-manager/server/api/v1/citizenInboxInterface";
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
                JSONObject citizenInbox =  new JSONObject();
                citizenInbox.put("feedback",mInput);
                citizenInbox.put("specialist",mUsername);
                healthFrame.put("citizenInbox",citizenInbox);


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

                }
                else{
                    int code = connection.getResponseCode();
                    String msg = connection.getResponseMessage();
                    String dummy = connection.getRequestMethod();
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
                Toast.makeText(SpecialistActivity.this,"Feedback sent",Toast.LENGTH_SHORT).show();
                mRequestArray.remove(clickedElement);
            }
            else{
                Toast.makeText(SpecialistActivity.this,"ERROR: "+responseCode,Toast.LENGTH_SHORT).show();
            }
        }
    }
}
