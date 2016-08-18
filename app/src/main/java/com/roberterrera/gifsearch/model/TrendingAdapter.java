package com.roberterrera.gifsearch.model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.roberterrera.gifsearch.R;
import com.roberterrera.gifsearch.model.trending.Images;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Rob on 8/16/16.
 */
public class TrendingAdapter extends RecyclerView.Adapter<TrendingViewHolder> {

    private final List<Images> trendingImagesList;
//    private final List<Datum> trendingList;
    private String imageUrl;

    private Context context;

    public TrendingAdapter(List<Images> trendingImagesList, Context context){
//        this.trendingList = trendingList;
        this.trendingImagesList = trendingImagesList;
        this.context = context;
    }

    @Override
    public TrendingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_trending, parent, false);

        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TrendingViewHolder holder, int position) {

        try {
            imageUrl = trendingImagesList.get(position).getFixedWidthStill().getUrl();

            Picasso.with(holder.gifStill.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.stat_notify_error)
                    .into(holder.gifStill);

        } catch (NullPointerException e){
            e.printStackTrace();
        }

        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, int pos) {
                Toast.makeText(context, imageUrl, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return trendingImagesList.size();
    }
}