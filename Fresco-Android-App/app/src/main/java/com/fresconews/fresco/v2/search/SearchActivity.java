package com.fresconews.fresco.v2.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.WindowManager;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.v2.utils.KeyboardUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

import java.util.ArrayList;

public class SearchActivity extends BaseActivity {
    private static final String TAG = SearchActivity.class.getSimpleName();

    public static final int VOICE_RECOGNITION_REQUEST_CODE = 100;
    public static final String EXTRA_FROM_SEARCH = "EXTRA_FROM_SEARCH";
    public static final String EXTRA_QUERY = "EXTRA_QUERY";

    private SearchViewModel searchViewModel;
    private String query;

    public static void start(Context context, String query, boolean newTask, boolean reorderToFront) {
        Intent starter = new Intent(context, SearchActivity.class);

        if (reorderToFront) {
            starter.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            starter.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        }
        if (newTask) {
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        starter.putExtra(EXTRA_QUERY, query);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        query = intent.getStringExtra(EXTRA_QUERY);
        if (query == null || query.equals("") || query.trim().length() == 0) { //the last two cause 500
            query = null;
        }
        else {
            query = query.trim();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        //Create a new search model and set it. Pass the query,
        searchViewModel = new SearchViewModel(this, query);
        setViewModel(R.layout.activity_search, searchViewModel);
    }

    @Override
    public void onBackPressed() {
        searchViewModel.back();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            KeyboardUtils.hideKeyboard(this);
        }
        catch (Exception e) {
            // Getting a null pointer, probably from getCurrentFocus().getWindowToken()
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it
            // could have heard
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            for (int i = 0; i < matches.size(); i++) {
                LogUtils.i(TAG, "Matches: " + String.valueOf(matches.get(i)));
            }
            String bestResult = null;
            if (matches.size() > 0) {
                bestResult = matches.get(0).toString(); // MIGHT HAVE HAD NO MATCHES
            }
            else {
                LogUtils.i(TAG, "No matches found!");
            }

            if (TextUtils.isEmpty(bestResult)) {
                return;
            }

            // How do i want to activate a search....
            searchViewModel.editText.get().setText(bestResult);
            searchViewModel.performSearch(bestResult);
        }
    }
}
