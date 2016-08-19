package com.roberterrera.gifsearch.view;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.roberterrera.gifsearch.R;
import com.roberterrera.gifsearch.model.adapter.SearchAdapter;
import com.roberterrera.gifsearch.model.adapter.TrendingAdapter;
import com.roberterrera.gifsearch.model.giphyapi.Datum;
import com.roberterrera.gifsearch.model.giphyapi.Images;
import com.roberterrera.gifsearch.model.giphyapi.response.SearchResponse;
import com.roberterrera.gifsearch.model.giphyapi.response.TrendingResponse;
import com.roberterrera.gifsearch.model.service.GiphyFactory;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    public String query;
    public List<Datum> trendingList;
    public List<Datum> searchResults;
    public List<Images> trendingListImages;
    public List<Images> searchResultsImages;

    private ProgressBar progressBar;
    public RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Trending Gifs (powered by Giphy)");

        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        trendingListImages = new ArrayList<>();
        trendingList = new ArrayList<>();

        GetTrendingTask getTrendingTask = new GetTrendingTask();
        getTrendingTask.execute();

        searchResults = new ArrayList<>();
        searchResultsImages = new ArrayList<>();
        handleIntent(getIntent());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        GetTrendingTask getTrendingTask = new GetTrendingTask();
        getTrendingTask.execute();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);

//         Setup for the search action.
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();

        SearchableInfo info = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(info);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    public void handleIntent(Intent intent) {

        // Run the search query on the search action so results will be returned.
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);

            GetSearchResultsTask getSearchResultsTask = new GetSearchResultsTask();
            getSearchResultsTask.execute();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks.
        switch (item.getItemId()) {
            case R.id.action_search:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        GetTrendingTask getTrendingTask = new GetTrendingTask();
        getTrendingTask.execute();
        super.onBackPressed();
    }

    class GetTrendingTask extends AsyncTask<Void, Void, Void> {

        TrendingAdapter mTrendingAdapter = new TrendingAdapter(trendingListImages, MainActivity.this);

        @Override
        protected Void doInBackground(Void... voids) {
            getTrendingGifs();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);

            setUpTrendingRecyclerView(trendingListImages, mTrendingAdapter);
        }
    }

    class GetSearchResultsTask extends AsyncTask<Void, Void, Void> {

        SearchAdapter searchAdapter = new SearchAdapter(searchResultsImages, MainActivity.this);

        @Override
        protected Void doInBackground(Void... voids) {
            search(query);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.GONE);

            setUpSearchRecyclerView(searchResultsImages, searchAdapter);
        }
    }

    public void getTrendingGifs() {
        GiphyFactory.create().getTrending().enqueue(new Callback<TrendingResponse>() {
            @Override
            public void onResponse(Call<TrendingResponse> call, Response<TrendingResponse> response) {


                if (response.isSuccessful()) try {
                    TrendingResponse responseBody = response.body();
                    trendingList = responseBody.getData();

                    for (int j = 0; j < trendingList.size(); j++) {
                        Images image = trendingList.get(j).getImages();
                        trendingListImages.add(image);
                    }

                } catch (NoSuchMethodError e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<TrendingResponse> call, Throwable t) {
                Log.e("ONFAILURE", "API call failed.");
                t.printStackTrace();
            }
        });
    }

    public void search(String query) {
        GiphyFactory.create().searchRequest(query).enqueue(new Callback<SearchResponse>() {
            @Override
            public void onResponse(Call<SearchResponse> call, Response<SearchResponse> response) {

                if (response.isSuccessful()) try {
                    SearchResponse responseBody = response.body();
                    searchResults = responseBody.getData();

                    for (int j = 0; j < searchResults.size(); j++) {
                        Images image = searchResults.get(j).getImages();
                        searchResultsImages.add(image);
                    }
                } catch (NoSuchMethodError e) {
                    e.printStackTrace();
                } Log.d("SEARCH", "Search results size: " + String.valueOf(searchResultsImages.size()));

            }

            @Override
            public void onFailure(Call<SearchResponse> call, Throwable t) {
                Log.e("ONFAILURE", "API call failed.");
                t.printStackTrace();
            }
        });
    }

    public void setUpTrendingRecyclerView(List<Images> list, TrendingAdapter adapter) {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_trending);

        if (list != null) {
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                        2, StaggeredGridLayoutManager.VERTICAL));
            }
        }
    }

    public void setUpSearchRecyclerView(List<Images> list, SearchAdapter adapter) {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_trending);

        if (list != null) {
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                        2, StaggeredGridLayoutManager.VERTICAL));
            }
        }
    }
}


