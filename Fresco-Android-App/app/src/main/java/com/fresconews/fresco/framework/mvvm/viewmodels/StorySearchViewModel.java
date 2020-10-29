package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.v2.storygallery.StoryGalleryActivity;
import com.fresconews.fresco.v2.utils.ImageUtils;

import rx.functions.Action1;

/**
 * Created by wumau on 9/23/2016.
 */
public class StorySearchViewModel extends ItemViewModel<Story> {

    public StorySearchViewModel(Story item) {
        super(item);
    }

    @Bindable
    public String getTitle() {
        return getItem().getTitle();
    }

    @Bindable
    public String getImageUrl() {
        if(getItem() == null || getItem().loadThumbnails() == null || getItem().loadThumbnails().size() == 0){
            return ""; //todo do we want a default story image of sorts?
        }
//        return getItem().loadThumbnails().get(0).getImage();
        return ImageUtils.getImageSizeV2(getItem().loadThumbnails().get(0).getImage(), 600);
    }

    public Action1<View> goToStory = view -> {
        if (getItem() != null) {
            StoryGalleryActivity.start(view.getContext(), getItem().getId(), getItem().getTitle(), getItem().getCaption(), true);
        }
    };
}
