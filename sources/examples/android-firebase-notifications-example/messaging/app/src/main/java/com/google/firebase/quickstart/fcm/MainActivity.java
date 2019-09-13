/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.fcm;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String BASE_URL = "http://development.onesaitplatform.com/api-manager/server/api/";


    /* Preferences related objects */
    SharedPreferences preferences;
    SharedPreferences.Editor preferencesEditor;
    private String pref_imei;
    private String pref_device_token;
    private String pref_onesait_token_id;

    private final int REQUEST_PERMISSION_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_PERMISSION_PHONE_STATE);
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        preferencesEditor = preferences.edit();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d(TAG, "Key: " + key + " Value: " + value);
            }
        }
        // [END handle_data_extras]

        Button subscribeButton = findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Subscribing to news topic");
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic("news")
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                String msg = getString(R.string.msg_subscribed);
                                if (!task.isSuccessful()) {
                                    msg = getString(R.string.msg_subscribe_failed);
                                }
                                Log.d(TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });
                // [END subscribe_topics]
            }
        });

        Button logTokenButton = findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();
                                preferencesEditor.putString(getString(R.string.pref_device_token), token);
                                preferencesEditor.apply();


                                pref_imei = preferences.getString(getString(R.string.pref_imei), "NaN");

                                if (pref_imei.equals("NaN")) {
                                    TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                                        return;
                                    }
                                    preferencesEditor.putString(getString(R.string.pref_imei), mngr.getDeviceId());
                                    preferencesEditor.apply();
                                    pref_imei = preferences.getString(getString(R.string.pref_imei), "NaN");
                                }


                                pref_onesait_token_id = preferences.getString(getString(R.string.pref_onesait_token_id),"NaN");
                                pref_imei = preferences.getString(getString(R.string.pref_imei), "NaN");
                                pref_device_token = preferences.getString(getString(R.string.pref_device_token),"NaN");

                                if(pref_onesait_token_id.equals("NaN")){

                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(BASE_URL)
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build();

                                    HttpInterface apiService =
                                            retrofit.create(HttpInterface.class);
                                    NativeNotifKeys key = new NativeNotifKeys("messaging",pref_device_token,pref_imei);
                                    NativeNotifKeysOntology instance = new NativeNotifKeysOntology(key);
                                    Call<PostResponse> call = apiService.postJson(instance);
                                    call.enqueue(new Callback<PostResponse>() {
                                        @Override
                                        public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
                                            int statusCode = response.code();
                                            PostResponse body = response.body();
                                            preferencesEditor.putString(getString(R.string.pref_onesait_token_id),body.ids.get(0));
                                            preferencesEditor.apply();
                                        }

                                        @Override
                                        public void onFailure(Call<PostResponse> call, Throwable t) {

                                        }
                                    });
                                }
                                else{
                                    Log.d(TAG,"Token_ID: "+pref_onesait_token_id);
                                    Retrofit retrofit = new Retrofit.Builder()
                                            .baseUrl(BASE_URL)
                                            .addConverterFactory(GsonConverterFactory.create())
                                            .build();

                                    HttpInterface apiService =
                                            retrofit.create(HttpInterface.class);
                                    NativeNotifKeys key = new NativeNotifKeys("messaging",pref_device_token,pref_imei);
                                    NativeNotifKeysOntology instance = new NativeNotifKeysOntology(key);
                                    Call<ResponseBody> call = apiService.putJson(pref_onesait_token_id,instance);
                                    call.enqueue(new Callback<ResponseBody>() {
                                        @Override
                                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                            int statusCode = response.code();
                                            ResponseBody body = response.body();
                                        }

                                        @Override
                                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                                        }
                                    });
                                }

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.d(TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });




            }
        });
    }

}
