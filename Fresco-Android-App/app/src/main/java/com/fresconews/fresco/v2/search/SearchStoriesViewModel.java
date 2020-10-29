package com.fresconews.fresco.v2.search;

import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.StorySearchViewModel;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.v2.storiespreview.StoriesPreviewActivity;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by wumau on 9/25/2016.
 */

public class SearchStoriesViewModel extends ViewModel {
    private StorySearchViewModel story1SearchViewModel;
    private StorySearchViewModel story2SearchViewModel;
    private StorySearchViewModel story3SearchViewModel;
    private int totalStoriesCount;
    private String seeStoriesButtonText;
    private SearchQuery searchQuery;

    public SearchStoriesViewModel(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public void setData(List<Story> stories) {
        if (stories != null && stories.size() > 0) {
            if (stories.size() > 0) {
                setStory1SearchViewModel(new StorySearchViewModel(stories.get(0)));
            }
            if (stories.size() > 1) {
                setStory2SearchViewModel(new StorySearchViewModel(stories.get(1)));
            }
            if (stories.size() > 2) {
                setStory3SearchViewModel(new StorySearchViewModel(stories.get(2)));
            }
            setSeeStoriesButtonText("See All " + stories.get(0).getTotalStoriesFromSearchQuery() + " Stories");
            setTotalStoriesCount(stories.get(0).getTotalStoriesFromSearchQuery());
        }
        else {
            setTotalStoriesCount(0);
        }
    }

    @Bindable
    public String getSeeStoriesButtonText() {
        return seeStoriesButtonText;
    }

    public void setSeeStoriesButtonText(String seeStoriesButtonText) {
        this.seeStoriesButtonText = seeStoriesButtonText;
        notifyPropertyChanged(BR.seeStoriesButtonText);
    }

    @Bindable
    public int getTotalStoriesCount() {
        return totalStoriesCount;
    }

    public void setTotalStoriesCount(int totalStoriesCount) {
        this.totalStoriesCount = totalStoriesCount;
        notifyPropertyChanged(BR.totalStoriesCount);
    }

    @Bindable
    public StorySearchViewModel getStory1SearchViewModel() {
        return story1SearchViewModel;
    }

    public void setStory1SearchViewModel(StorySearchViewModel story1SearchViewModel) {
        this.story1SearchViewModel = story1SearchViewModel;
        notifyPropertyChanged(BR.story1SearchViewModel);
    }

    @Bindable
    public StorySearchViewModel getStory2SearchViewModel() {
        return story2SearchViewModel;
    }

    public void setStory2SearchViewModel(StorySearchViewModel story2SearchViewModel) {
        this.story2SearchViewModel = story2SearchViewModel;
        notifyPropertyChanged(BR.story2SearchViewModel);
    }

    @Bindable
    public StorySearchViewModel getStory3SearchViewModel() {
        return story3SearchViewModel;
    }

    public void setStory3SearchViewModel(StorySearchViewModel story3SearchViewModel) {
        this.story3SearchViewModel = story3SearchViewModel;
        notifyPropertyChanged(BR.story3SearchViewModel);
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public Action1<View> seeAllStories = view -> {
        StoriesPreviewActivity.start(view.getContext(), getSearchQuery(), true, true);
    };
}
