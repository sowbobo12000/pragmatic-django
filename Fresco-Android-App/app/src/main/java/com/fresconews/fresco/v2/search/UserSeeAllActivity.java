package com.fresconews.fresco.v2.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.google.gson.Gson;

import static com.fresconews.fresco.v2.search.SearchActivity.EXTRA_FROM_SEARCH;
import static com.fresconews.fresco.v2.search.SearchActivity.EXTRA_QUERY;

public class UserSeeAllActivity extends BaseActivity {

    public static void start(Context context, SearchQuery searchQuery) {
        Intent starter = new Intent(context, UserSeeAllActivity.class);
        starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        starter.putExtra(EXTRA_QUERY, new Gson().toJson(searchQuery, SearchQuery.class));
        starter.putExtra(EXTRA_FROM_SEARCH, true);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        String searchQueryString = getIntent().getStringExtra(EXTRA_QUERY);
        SearchQuery searchQuery = new Gson().fromJson(searchQueryString, SearchQuery.class);

        UserSeeAllViewModel viewModel = new UserSeeAllViewModel(this, searchQuery);
        setViewModel(R.layout.activity_user_see_all, viewModel);
    }
}
