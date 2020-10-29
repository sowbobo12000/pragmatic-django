package com.fresconews.fresco.v2.search;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.Bindable;
import android.speech.RecognizerIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.GalleryViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.UserSearchViewModelDataSource;
import com.fresconews.fresco.framework.persistence.managers.SearchManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;
import com.fresconews.fresco.framework.recyclerview.SearchRecyclerViewAdapter;
import com.fresconews.fresco.v2.utils.KeyboardUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.VideoScrollListener;

import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.fresconews.fresco.v2.search.SearchActivity.VOICE_RECOGNITION_REQUEST_CODE;

/**
 * Created by Blaze on 7/21/2016.
 * <p>
 * SearchViewModel basically piggy-backed off of the GalleryViewModel with regards to the
 * recycler adapter. However, the SearchRecyclerViewAdapter, which piggybacks off of the
 * PagingRecyclerViewBindingAdapter (java, am i right?) differs by manually inserting two
 * cards for the 'Users' and 'Stories'
 * In the future there could be need for expanding the Stories or Users cards in the RecyclerView.
 * To do this, alter the SearchRecyclerViewAdapter by inserting more cards of individual Users or
 * Stories. I.E. One of the three relative layouts found in search_story_card_full.xml as its own
 * cardview.
 * <p>
 * TLDR; It's a GalleryView with an extended Adapter to add in User and Stories cards
 */
public class SearchViewModel extends ActivityViewModel<SearchActivity> {
    private static final String TAG = SearchViewModel.class.getSimpleName();

    private static final int PAGE_SIZE = 10;

    @Inject
    SearchManager searchManager;

    @Inject
    UserManager userManager;

    private SearchRecyclerViewAdapter<GalleryViewModel> adapter;
    private RecyclerViewBindingAdapter<UserSearchViewModel> suggestedAdapter;

    public BindableView<EditText> editText = new BindableView<>();
    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    private SearchQuery currentSearchQuery;
    private boolean queryFilled;

    private int totalGalleriesCount;
    private int totalUserCount;
    private int totalStoriesCount;

    private boolean searchMade;
    private boolean searchRequestFinished;
    private boolean emptyState;
    private boolean scrollListenerAdded;

    SearchViewModel(SearchActivity activity, String query) {
        super(activity);
        searchRequestFinished = false;
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        if (!TextUtils.isEmpty(query)) {
            setQuery(query.trim());
        }
        setNavIcon(R.drawable.ic_navigation_arrow_back_white);

        scrollListenerAdded = false;
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        if (currentSearchQuery != null && !TextUtils.isEmpty(currentSearchQuery.getQuery())) {
            editText.get().setText(currentSearchQuery.getQuery());
        }
        editText.get().setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                KeyboardUtils.hideKeyboard(getActivity());
                return true;
            }
            return false;
        });

        if (currentSearchQuery != null) {
            performSearch(currentSearchQuery.getQuery());
        }
        else {
            loadNearbyUsers();
        }
    }

    public void back() {
        searchManager.clearSearchTerms();
        getActivity().finish();
    }

    private void loadNearbyUsers() {
        userManager.suggestions()
                   .observeOn(AndroidSchedulers.mainThread())
                   .subscribeOn(Schedulers.io())
                   .onErrorReturn(throwable -> null)
                   .subscribe(this::addSuggestedUserToDataSource);
    }

    public void performSearch(String query) {
        if (TextUtils.isEmpty(query)) { //the last two cause 500
            return;
        }
        if (searchManager != null) {
            searchManager.clearSearchTerms();
        }
        final String finalQuery = query.trim();
        setQuery(finalQuery);
        setSearchMade(true);

        IDataSource<GalleryViewModel> dataSource = new GalleryViewModelDataSource(getActivity(), searchManager.getGalleryListDataSource());
        if (adapter == null) {
            adapter = new SearchRecyclerViewAdapter<>(getActivity(), R.layout.view_search_users_card,
                    R.layout.view_search_stories_card, R.layout.item_gallery, dataSource, currentSearchQuery);
            adapter.setHasStableIds(true);
        }
        else {
            adapter.resetLastPagingPosition();
            adapter.setDataSource(dataSource);
        }
        totalGalleriesCount = -1;
        totalUserCount = -1;
        totalStoriesCount = -1;
        searchRequestFinished = false;

        adapter.getNewPageObservable()
               .onErrorReturn(throwable -> null)
               .subscribe(last -> {
                   if (last == null) {
                       searchManager.downloadGalleries(currentSearchQuery, PAGE_SIZE)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .doOnError(throwable -> {
                                        searchRequestFinished = true;
                                        setTotalGalleriesCount(0);
                                    })
                                    .onErrorReturn(throwable -> null)
                                    .subscribe(galleries -> {
                                        adapter.notifyDataSetChanged();
                                        searchRequestFinished = true;
                                        if (galleries == null) {
                                            setTotalGalleriesCount(0);
                                        }
                                        else {
                                            setTotalGalleriesCount(galleries.size());
                                        }
                                    });

                   }
                   else {
                       searchManager.downloadGalleries(currentSearchQuery, PAGE_SIZE, last.getId())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(Schedulers.io())
                                    .doOnError(throwable -> {
                                        searchRequestFinished = true;
                                        setTotalGalleriesCount(0);
                                    })
                                    .onErrorReturn(throwable -> null)
                                    .subscribe(galleries -> {
                                        if (galleries != null) { //for pagination
                                            adapter.notifyItemRangeChanged(adapter.getItemCount() - galleries.size(), galleries.size());
                                        }
                                        searchRequestFinished = true;
                                    });
                   }
               });

        if (recyclerView.get().getAdapter() == null) {
            recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.get().setAdapter(adapter);
        }
        else {
            recyclerView.get().swapAdapter(adapter, true);
        }

        if (!scrollListenerAdded) {
            recyclerView.get().addOnScrollListener(new VideoScrollListener<>(dataSource));
            scrollListenerAdded = true;
        }

        downloadSearchUsersAndStories(null);
    }

    private void addSuggestedUserToDataSource(List<User> users) {
        if (searchMade) {
            return;
        }
        if (users == null || users.isEmpty()) {
            setEmptyState(!searchRequestFinished); //todo maybe i should follow an empty state thing
        }
        else {
            IDataSource<UserSearchViewModel> dataSource = new UserSearchViewModelDataSource(getActivity(), new ListDataSource<>(users));
            if (suggestedAdapter == null) {
                suggestedAdapter = new RecyclerViewBindingAdapter<>(R.layout.item_suggested_user, dataSource);
            }
            else {
                suggestedAdapter.setDataSource(dataSource);
            }

            recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.get().setAdapter(suggestedAdapter);
        }
    }

    public Action1<View> startVoiceSearch = view -> {
        PackageManager pm = getActivity().getPackageManager();
        List activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities != null && activities.size() > 0) {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //These do nothing. Although they should.
            //https://code.google.com/p/android/issues/detail?id=16638
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                    Long.valueOf(1000));
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                    Long.valueOf(3000));
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                    Long.valueOf(3000));
            getActivity().startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_voice_search_missing);
        }
    };

    public Action1<View> close = view -> {
        editText.get().setText("");
        setQueryFilled(false);
        KeyboardUtils.bringUpKeyboard(getActivity(), editText.get());
    };

    public Action1<View> onNavIconClicked = view -> {
        getActivity().onBackPressed();
    };

    public Action1<SwipeRefreshLayout> refresh = swipeRefreshLayout -> {
        setEmptyState(false);
        if (searchMade) {
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            performSearch(currentSearchQuery.getQuery());
        }
        else {
            if (suggestedAdapter != null) {
                suggestedAdapter.notifyDataSetChanged();
            }
            userManager.suggestions()
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribeOn(Schedulers.io())
                       .onErrorReturn(throwable -> null)
                       .subscribe(users -> {
                           swipeRefreshLayout.setRefreshing(false);
                           addSuggestedUserToDataSource(users);
                           if (users != null) {
                               for (User user : users) {
                                   user.save();
                               }
                           }
                       });
        }
    };

    private void downloadSearchUsersAndStories(SwipeRefreshLayout swipeRefreshLayout) {
        searchManager.downloadUsers(currentSearchQuery, PAGE_SIZE)
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribeOn(Schedulers.io())
                     .doOnError(throwable -> {
                         searchRequestFinished = true;
                         setTotalUserCount(0);
                     })
                     .onErrorReturn(throwable -> null)
                     .subscribe(users -> {
                         if (swipeRefreshLayout != null) {
                             swipeRefreshLayout.setRefreshing(false);
                         }
                         searchRequestFinished = true;
                         adapter.setUserData(users, currentSearchQuery); //todo is this null list thing okay or not?
                         if (users == null) {
                             setTotalUserCount(0);
                         }
                         else {
                             setTotalUserCount(users.size());
                         }
                     });

        searchManager.downloadStories(currentSearchQuery, PAGE_SIZE)
                     .observeOn(AndroidSchedulers.mainThread())
                     .subscribeOn(Schedulers.io())
                     .doOnError(throwable -> {
                         searchRequestFinished = true;
                         setTotalStoriesCount(0);
                     })
                     .onErrorReturn(throwable -> null)
                     .subscribe(stories -> {
                         if (swipeRefreshLayout != null) {
                             swipeRefreshLayout.setRefreshing(false);
                         }
                         searchRequestFinished = true;
                         adapter.setStoriesData(stories, currentSearchQuery); //todo really is this okay
                         if (stories == null) {
                             setTotalStoriesCount(0);
                         }
                         else {
                             setTotalStoriesCount(stories.size());
                         }
                     });
    }

    public void setQuery(String query) {
        currentSearchQuery = searchManager.addQueryToDatabase(query);
        setQueryFilled(!TextUtils.isEmpty(query));
    }

    @Bindable
    public boolean isQueryFilled() {
        return queryFilled;
    }

    public void setQueryFilled(boolean queryFilled) {
        this.queryFilled = queryFilled;
        notifyPropertyChanged(BR.queryFilled);
    }

    @Bindable
    public boolean isSearchMade() {
        return searchMade;
    }

    public void setSearchMade(boolean searchMade) {
        this.searchMade = searchMade;
        notifyPropertyChanged(BR.searchMade);
    }

    private void setTotalUserCount(int totalUserCount) {
        this.totalUserCount = totalUserCount;
        setEmptyState(totalStoriesCount == 0 && totalUserCount == 0 && totalGalleriesCount == 0 && searchRequestFinished);
    }

    private void setTotalStoriesCount(int totalStoriesCount) {
        this.totalStoriesCount = totalStoriesCount;
        setEmptyState(totalStoriesCount == 0 && totalUserCount == 0 && totalGalleriesCount == 0 && searchRequestFinished);
    }

    private void setTotalGalleriesCount(int totalGalleriesCount) {
        this.totalGalleriesCount = totalGalleriesCount;
        setEmptyState(totalStoriesCount == 0 && totalUserCount == 0 && totalGalleriesCount == 0 && searchRequestFinished);
    }

    @Bindable
    public boolean isEmptyState() {
        return emptyState;
    }

    public void setEmptyState(boolean emptyState) {
        this.emptyState = emptyState;
        notifyPropertyChanged(BR.emptyState);
    }
}
