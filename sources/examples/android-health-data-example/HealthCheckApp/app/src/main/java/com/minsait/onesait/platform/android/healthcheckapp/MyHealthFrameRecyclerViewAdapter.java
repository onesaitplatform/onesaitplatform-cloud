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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;


public class MyHealthFrameRecyclerViewAdapter extends RecyclerView.Adapter<MyHealthFrameRecyclerViewAdapter.ViewHolder> {

    private final List<HealthData> mValues;
    //private final OnListFragmentInteractionListener mListener;

    public MyHealthFrameRecyclerViewAdapter(List<HealthData> items) {
        mValues = items;
        //mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_healthframe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mWeightView.setText(String.valueOf(mValues.get(position).getWeight()));
        holder.mCommentsView.setText(mValues.get(position).getComments());
        holder.mHeightView.setText(String.valueOf(mValues.get(position).getHeight()));
        holder.mTimestampView.setText(mValues.get(position).getTimestamp());
        holder.mSysPressureView.setText(String.valueOf(mValues.get(position).getSysPressure()));
        holder.mDiaPressureView.setText(String.valueOf(mValues.get(position).getDiaPressure()));

        /*holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mHeightView;
        public final TextView mWeightView;
        public final TextView mSysPressureView;
        public final TextView mDiaPressureView;
        public final TextView mTimestampView;
        public final TextView mCommentsView;
        public HealthData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mWeightView = (TextView) view.findViewById(R.id.weight_tv);
            mHeightView = (TextView) view.findViewById(R.id.height_tv);
            mSysPressureView = (TextView) view.findViewById(R.id.sys_pressure_tv);
            mDiaPressureView = (TextView) view.findViewById(R.id.dia_pressure_tv);
            mCommentsView = (TextView) view.findViewById(R.id.comments_tv);
            mTimestampView= (TextView) view.findViewById(R.id.timestamp_tv);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mCommentsView.getText() + "'";
        }
    }

}
