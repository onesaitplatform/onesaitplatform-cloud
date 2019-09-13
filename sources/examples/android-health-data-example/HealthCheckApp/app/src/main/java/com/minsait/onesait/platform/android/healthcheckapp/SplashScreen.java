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

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mbriceno on 10/03/2015.
 */
public class SplashScreen extends Activity {
    private final int SPLASH_DISPLAY_DURATION = 2000;
    boolean granted = false;

    String[] permissions= new String[]{
            Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        //PreferenceManager.setDefaultValues(this,R.xml.preferences,false);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(requestPermissions()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //createAndShowAlertDialog();
                        Intent firstIntent = new Intent(getApplicationContext(),LoginActivity.class);
                        startActivity(firstIntent);
                        finish();
                    }

                }, SPLASH_DISPLAY_DURATION);
            }
        }
        else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    //createAndShowAlertDialog();
                    Intent firstIntent = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(firstIntent);
                    finish();
                }

            }, SPLASH_DISPLAY_DURATION);
        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            createAndShowAlertDialog();
                        }

                    }, SPLASH_DISPLAY_DURATION);

                } else {
                    finish();
                }
                return;
            }
        }

    }


    private  boolean requestPermissions() {

        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(this,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),1);
            return false;
        }
        return true;
    }

    private void createAndShowAlertDialog() {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alert_title_loc);
        builder.setMessage(R.string.alert_title_msg);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent firstIntent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(firstIntent);
                finish();
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
        dialog.show();*/
    }
}
