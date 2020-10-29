package com.fresconews.fresco.framework.recyclerview;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.search.SearchStoriesViewModel;
import com.fresconews.fresco.v2.search.SearchUsersViewModel;

import java.util.List;

import rx.Observable;

/**
 * Created by Blazej on 7/26/2016.
 */
public class SearchRecyclerViewAdapter<T extends ViewModel> extends PagingRecyclerViewBindingAdapter<T> {

    private static final int TYPE_USER = 13;
    private static final int TYPE_STORY = 12;
    private static final int TYPE_GALLERY = 33;

    @LayoutRes
    private int galleryLayout;

    @LayoutRes
    private int userLayout;

    @LayoutRes
    private int storyLayout;

    private int storiesCount;
    private int usersCount;
    private SearchStoriesViewModel searchStoriesViewModel;
    private SearchUsersViewModel searchUsersViewModel;
    private UserCardViewHolder userCardViewHolder;
    private StoryCardViewHolder storyCardViewHolder;

    public SearchRecyclerViewAdapter(Activity activity, @LayoutRes int userLayout, @LayoutRes int storyLayout,
                                     @LayoutRes int galleryLayout, IDataSource<T> galleryDataSource, SearchQuery searchQuery) {
        super(galleryLayout, galleryDataSource);
        this.userLayout = userLayout;
        this.galleryLayout = galleryLayout;
        this.storyLayout = storyLayout;

        searchStoriesViewModel = new SearchStoriesViewModel(searchQuery);
        searchUsersViewModel = new SearchUsersViewModel(activity, searchQuery);
    }

    @Override
    public BindingViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(userLayout, parent, false);
            userCardViewHolder = new UserCardViewHolder(view);
            return new BindingViewHolder<>(view);
        }
        else if (viewType == TYPE_STORY) {
            View view = LayoutInflater.from(parent.getContext()).inflate(storyLayout, parent, false);
            storyCardViewHolder = new StoryCardViewHolder(view);
            return new BindingViewHolder<>(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext()).inflate(galleryLayout, parent, false);
            return new BindingViewHolder<>(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            if (usersCount > 0) {
                return TYPE_USER;
            }
            else if (usersCount == 0 && storiesCount > 0) {
                return TYPE_STORY;
            }
        }
        else if (position == 1) {
            if (storiesCount > 0 && usersCount > 0) {
                return TYPE_STORY;
            }
        }
        return TYPE_GALLERY;
    }

    @Override
    public void onBindViewHolder(BindingViewHolder<T> holder, int position) {
        if (getItemViewType(position) == TYPE_USER) {
            userCardViewHolder.bind(searchUsersViewModel);
        }
        else if (getItemViewType(position) == TYPE_STORY) {
            storyCardViewHolder.bind(searchStoriesViewModel);
        }
        else {
            setOffset(getActiveCards() + 1);
            super.onBindViewHolder(holder, position - getActiveCards());
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + getActiveCards();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setUserData(List<User> users, SearchQuery searchQuery) {
        searchUsersViewModel.setData(users);
        usersCount = searchUsersViewModel.getTotalUserCount();
        if (usersCount > 0) {
            notifyItemChanged(0);
        }
        searchUsersViewModel.setSearchQuery(searchQuery);
    }

    public void setStoriesData(List<Story> stories, SearchQuery searchQuery) {
        searchStoriesViewModel.setData(stories);
        storiesCount = searchStoriesViewModel.getTotalStoriesCount();
        if (usersCount == 0 && storiesCount > 0) {
            notifyItemChanged(0);
        }
        else if (usersCount > 0 && storiesCount > 0) {
            notifyItemChanged(1);
        }
        searchStoriesViewModel.setSearchQuery(searchQuery);
    }

    private int getActiveCards() {
        if (storiesCount > 0 && usersCount > 0) {
            return 2;
        }
        else if ((storiesCount > 0 && usersCount == 0) || (storiesCount == 0 && usersCount > 0)) {
            return 1;
        }
        return 0;
    }

    public Observable<T> getNewPageObservable() {
        return Observable.create(this);
    }

    private static class UserCardViewHolder extends BindingViewHolder<SearchUsersViewModel> {
        UserCardViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class StoryCardViewHolder extends BindingViewHolder<SearchStoriesViewModel> {
        StoryCardViewHolder(View itemView) {
            super(itemView);
        }
    }
}
