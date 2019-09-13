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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mbriceno on 18/05/2018.
 */

public class MainMenuAdapter extends RecyclerView.Adapter<MainMenuAdapter.MainItemViewHolder>{

    Context mContext;
    private static final String TAG = MainMenuAdapter.class.getSimpleName();
    private static int viewHolderCount;
    private int mNumberItems;
    private ArrayList<MainItem> mMenu;

    private ListItemClickListener mOnClickListener;

    public interface ListItemClickListener{
        void onListItemClick(int clickedItemId);
    }

    public MainMenuAdapter(ArrayList<MainItem> itemArray, ListItemClickListener itemClickListener){
        viewHolderCount = 0;
        mNumberItems = itemArray.size();
        mMenu = new ArrayList<>(mNumberItems);
        mMenu = itemArray;
        mOnClickListener = itemClickListener;
    }

    @Override
    public MainItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        int layoutIdForItems = R.layout.main_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForItems, parent, shouldAttachToParentImmediately);
        MainItemViewHolder viewHolder = new MainItemViewHolder(view);
        viewHolderCount++;
        Log.d(TAG, "onCreateViewHolder: number of ViewHolders created: "
                + viewHolderCount);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MainItemViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        /*Picasso
                .with(mContext)
                .load(NetworkUtils.POSTERS_BASE_URL+mMoviesArray.get(position).getPosterURL())
                .into(holder.mPosterImageView);*/
        holder.mItemTextView.setText(mMenu.get(position).getDescription());
        holder.mItemImageView.setImageDrawable(ContextCompat.getDrawable(mContext,mMenu.get(position).getImageId()));

    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    class MainItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView mItemImageView;
        TextView mItemTextView;

        public MainItemViewHolder(View itemView) {
            super(itemView);
            mItemImageView = (ImageView) itemView.findViewById(R.id.iv_main_item);
            mItemTextView = (TextView) itemView.findViewById(R.id.tv_main_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }

}
