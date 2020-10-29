package com.fresconews.fresco.v2.gallery.gallerylist;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.GalleryViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.utils.VideoScrollListener;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;

public class GalleryListViewModel extends ActivityViewModel<GalleryListActivity> {

    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    @Inject
    GalleryManager galleryManager;

    public GalleryListViewModel(GalleryListActivity activity) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setNavIcon(R.drawable.ic_navigation_arrow_back_white);
        setTitle(activity.getIntent().getStringExtra(GalleryListActivity.TITLE));
    }

    @Override
    public void onBound() {
        super.onBound();
        List<String> galleryIds = getActivity().getIntent().getStringArrayListExtra(GalleryListActivity.GALLERY_IDS);
        if (galleryIds != null) {
            if (galleryIds.size() > 1) {
                galleryManager.downloadGalleries(galleryIds)
                              .observeOn(AndroidSchedulers.mainThread())
                              .onErrorReturn(throwable -> null)
                              .subscribe(galleries -> { //todo not sure if null is handled, perhaps empty observable would be better
                                  IDataSource<GalleryViewModel> dataSource = new GalleryViewModelDataSource(getActivity(), new ListDataSource<>(galleries));
                                  RecyclerViewBindingAdapter<GalleryViewModel> adapter = new RecyclerViewBindingAdapter<>(R.layout.item_gallery, dataSource);

                                  recyclerView.get().setAdapter(adapter);
                                  recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
                                  recyclerView.get().addOnScrollListener(new VideoScrollListener<>(dataSource));
                              });
            }
            else if (galleryIds.size() == 1) {
                galleryManager.downloadGallery(galleryIds.get(0))
                              .observeOn(AndroidSchedulers.mainThread())
                              .onErrorReturn(throwable -> null)
                              .subscribe(gallery -> {//todo not sure if null is handled, perhaps empty observable would be better
                                  IDataSource<GalleryViewModel> dataSource = new GalleryViewModelDataSource(getActivity(), new ListDataSource<>(Collections.singletonList(gallery)));
                                  RecyclerViewBindingAdapter<GalleryViewModel> adapter = new RecyclerViewBindingAdapter<>(R.layout.item_gallery, dataSource);

                                  recyclerView.get().setAdapter(adapter);
                                  recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
                                  recyclerView.get().addOnScrollListener(new VideoScrollListener<>(dataSource));
                              });
            }
        }
    }

}
