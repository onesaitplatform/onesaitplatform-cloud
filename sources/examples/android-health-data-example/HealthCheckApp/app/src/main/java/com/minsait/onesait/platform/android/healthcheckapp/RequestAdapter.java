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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mbriceno on 18/05/2018.
 */

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestItemViewHolder>{

    Context mContext;
    private static final String TAG = MainMenuAdapter.class.getSimpleName();
    private static int viewHolderCount;
    private int mNumberItems;
    private ArrayList<RequestData> mRequests;

    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onListItemClick(int clickedItemId);
    }

    public RequestAdapter(ArrayList<RequestData> itemArray, ListItemClickListener itemClickListener){
        viewHolderCount = 0;
        mNumberItems = itemArray.size();
        mRequests = new ArrayList<>(mNumberItems);
        mRequests = itemArray;
        mOnClickListener = itemClickListener;
    }

    @Override
    public RequestItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        int layoutIdForItems = R.layout.request_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForItems, parent, shouldAttachToParentImmediately);
        RequestItemViewHolder viewHolder = new RequestItemViewHolder(view);
        viewHolderCount++;
        Log.d(TAG, "onCreateViewHolder: number of ViewHolders created: "
                + viewHolderCount);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RequestItemViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        /*Picasso
                .with(mContext)
                .load(NetworkUtils.POSTERS_BASE_URL+mMoviesArray.get(position).getPosterURL())
                .into(holder.mPosterImageView);*/
        holder.mItemTextView.setText(mRequests.get(position).getUsername());
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    class RequestItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mItemTextView;

        public RequestItemViewHolder(View itemView) {
            super(itemView);
            mItemTextView = (TextView) itemView.findViewById(R.id.tv_request_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }

}
