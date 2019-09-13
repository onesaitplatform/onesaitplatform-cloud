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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mbriceno on 17/05/2018.
 */

public class HealthData implements Parcelable{

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getSysPressure() {
        return sysPressure;
    }

    public void setSysPressure(int sysPressure) {
        this.sysPressure = sysPressure;
    }

    public int getDiaPressure() {
        return diaPressure;
    }

    public void setDiaPressure(int diaPressure) {
        this.diaPressure = diaPressure;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    private String comments;
    private int height;
    private int weight;
    private int sysPressure;
    private int diaPressure;
    private String timestamp;


    public HealthData(){

    }

    private HealthData(Parcel in){
        comments = in.readString();
        height = in.readInt();
        weight = in.readInt();
        sysPressure = in.readInt();
        diaPressure = in.readInt();
        timestamp = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(comments);
        parcel.writeInt(height);
        parcel.writeInt(weight);
        parcel.writeInt(sysPressure);
        parcel.writeInt(diaPressure);
        parcel.writeString(timestamp);
    }

    public static final Creator<HealthData> CREATOR
            = new Creator<HealthData>() {

        @Override
        public HealthData createFromParcel(Parcel in) {
            return new HealthData(in);
        }

        @Override
        public HealthData[] newArray(int size) {
            return new HealthData[size];
        }
    };
}
