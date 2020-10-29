package com.fresconews.fresco.v2.home.highlights;

import android.databinding.Bindable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.FragmentViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.GalleryViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.VideoScrollListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ryan on 8/11/2016.
 */
public class HighlightsViewModel extends FragmentViewModel<HighlightsFragment> {
    private static final String TAG = HighlightsViewModel.class.getSimpleName();
    private static final int PAGE_SIZE = 10;

    @Inject
    GalleryManager galleryManager;

    @Inject
    AnalyticsManager analyticsManager;

    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    private IDataSource<GalleryViewModel> dataSource;
    private PagingRecyclerViewBindingAdapter<GalleryViewModel> adapter;
    private boolean emptyState = false;

    public HighlightsViewModel(HighlightsFragment fragment) {
        super(fragment);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        dataSource = new GalleryViewModelDataSource(getActivity(), galleryManager.getHighlightsDataSource());
        if (adapter == null) {
            adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_gallery, dataSource);
        }
        else {
            adapter.notifyDataSetChanged();
        }

        adapter.getNewPageObservable().onErrorReturn(throwable -> null).subscribe(last -> {
            if (last == null) {
                galleryManager.downloadHighlights(PAGE_SIZE)
                              .observeOn(AndroidSchedulers.mainThread())
                              .subscribeOn(Schedulers.io())
                              .onErrorReturn(throwable -> {
                                  setEmptyState(true);
                                  SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_highlights);
                                  return null;
                              })
                              .subscribe(galleries -> {
                                  adapter.notifyDataSetChanged();
                                  setEmptyState((galleries == null || galleries.isEmpty()) && adapter.getItemCount() == 0);
                                  if (galleries != null && !galleries.isEmpty()) {
                                      analyticsManager.galleriesScrolledByInHighlights(galleries.size());
                                  }
                              });
            }
            else {
                galleryManager.downloadHighlights(PAGE_SIZE, last.getId())
                              .observeOn(AndroidSchedulers.mainThread())
                              .subscribeOn(Schedulers.io())
                              .onErrorReturn(throwable -> {
                                  SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_highlights);
                                  return null;
                              })
                              .filter(galleries -> galleries != null)
                              .subscribe(galleries -> {
                                  adapter.notifyItemRangeChanged(adapter.getItemCount() - galleries.size(), galleries.size());
                                  analyticsManager.galleriesScrolledByInHighlights(galleries.size());
                              });
            }
        });

        if (recyclerView.get().getAdapter() == null) {
            recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.get().setAdapter(adapter);
            recyclerView.get().addOnScrollListener(new VideoScrollListener<>(dataSource));
        }
    }

    public void reload(SwipeRefreshLayout swipeRefreshLayout) {

        List<GalleryViewModel> list = new ArrayList<>();
        if (dataSource != null) {
            list.addAll(dataSource.list());
        }
        galleryManager.clearHighlights();
        adapter.resetLastPagingPosition();

        galleryManager.downloadHighlights(PAGE_SIZE)
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(Schedulers.io())
                      .onErrorReturn(throwable -> {
                          setEmptyState(true);
                          SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_loading_highlights);
                          return null;
                      })
                      .subscribe(galleries -> {
                          adapter.refresh(list, galleries, PAGE_SIZE, true);
                          if (swipeRefreshLayout != null) {
                              swipeRefreshLayout.setRefreshing(false);
                          }
                          setEmptyState((galleries == null || galleries.isEmpty()) && adapter.getItemCount() == 0);
                      });
    }

    public Action1<SwipeRefreshLayout> refresh = this::reload;

    @Bindable
    public boolean getEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }
}
