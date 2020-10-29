package com.fresconews.fresco.v2.home.highlights;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseFragment;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.v2.home.HomeFragment;
import com.fresconews.fresco.v2.utils.LogUtils;

import javax.inject.Inject;

public class HighlightsFragment extends BaseFragment implements HomeFragment {
    @Inject
    AnalyticsManager analyticsManager;

    private HighlightsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
        viewModel = new HighlightsViewModel(this);
        analyticsManager.highlightsScreenOpened();
        return setViewModel(R.layout.fragment_highlights, viewModel, container);
    }

    @Override
    public void onPause() {
        super.onPause();
        analyticsManager.highlightsScreenClosed();
    }

    @Override
    public void reload() {
        if (viewModel != null) {
            viewModel.reload(null);
        }
    }
}
