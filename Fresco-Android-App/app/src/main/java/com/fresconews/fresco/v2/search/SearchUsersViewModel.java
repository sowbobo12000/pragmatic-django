package com.fresconews.fresco.v2.search;

import android.app.Activity;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.persistence.models.User;

import java.util.List;

import rx.functions.Action1;

/**
 * Created by wumau on 9/25/2016.
 */

public class SearchUsersViewModel extends ViewModel {

    private UserSearchViewModel user1SearchViewModel;
    private UserSearchViewModel user2SearchViewModel;
    private UserSearchViewModel user3SearchViewModel;
    private int totalUserCount;
    private String seeUsersButtonText;
    private Activity activity;
    private SearchQuery searchQuery;

    public SearchUsersViewModel(Activity activity, SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
        this.activity = activity;
    }

    public void setData(List<User> users) {
        if (users != null && users.size() > 0) {
            if (users.size() > 0) {
                setUser1SearchViewModel(new UserSearchViewModel(activity, users.get(0)));
            }
            if (users.size() > 1) {
                setUser2SearchViewModel(new UserSearchViewModel(activity, users.get(1)));
            }
            if (users.size() > 2) {
                setUser3SearchViewModel(new UserSearchViewModel(activity, users.get(2)));
            }
            setSeeUsersButtonText("See All " + users.get(0).getTotalUsersFromSearchQuery() + " Users");
            setTotalUserCount(users.get(0).getTotalUsersFromSearchQuery());
        }
        else {
            setTotalUserCount(0);
        }
    }

    @Bindable
    public String getSeeUsersButtonText() {
        return seeUsersButtonText;
    }

    public void setSeeUsersButtonText(String seeUsersButtonText) {
        this.seeUsersButtonText = seeUsersButtonText;
        notifyPropertyChanged(BR.seeUsersButtonText);
    }

    @Bindable
    public int getTotalUserCount() {
        return totalUserCount;
    }

    public void setTotalUserCount(int totalUserCount) {
        this.totalUserCount = totalUserCount;
        notifyPropertyChanged(BR.totalUserCount);
    }

    @Bindable
    public UserSearchViewModel getUser1SearchViewModel() {
        return user1SearchViewModel;
    }

    public void setUser1SearchViewModel(UserSearchViewModel user1SearchViewModel) {
        this.user1SearchViewModel = user1SearchViewModel;
        notifyPropertyChanged(BR.user1SearchViewModel);
    }

    @Bindable
    public UserSearchViewModel getUser2SearchViewModel() {
        return user2SearchViewModel;
    }

    public void setUser2SearchViewModel(UserSearchViewModel user2SearchViewModel) {
        this.user2SearchViewModel = user2SearchViewModel;
        notifyPropertyChanged(BR.user2SearchViewModel);
    }

    @Bindable
    public UserSearchViewModel getUser3SearchViewModel() {
        return user3SearchViewModel;
    }

    public void setUser3SearchViewModel(UserSearchViewModel user3SearchViewModel) {
        this.user3SearchViewModel = user3SearchViewModel;
        notifyPropertyChanged(BR.user3SearchViewModel);
    }

    public SearchQuery getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(SearchQuery searchQuery) {
        this.searchQuery = searchQuery;
    }

    public Action1<View> seeAllUsers = view -> {
        UserSeeAllActivity.start(view.getContext(), getSearchQuery());
    };
}
