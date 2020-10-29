package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkSearchResults;
import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.network.services.SearchService;
import com.fresconews.fresco.framework.persistence.DBFlowDataSource;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Gallery_SearchQuery;
import com.fresconews.fresco.framework.persistence.models.Gallery_Table;
import com.fresconews.fresco.framework.persistence.models.SearchQuery;
import com.fresconews.fresco.framework.persistence.models.SearchQuery_Table;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.persistence.models.Story_Gallery;
import com.fresconews.fresco.framework.persistence.models.Story_SearchQuery;
import com.fresconews.fresco.framework.persistence.models.Story_Table;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.persistence.models.User_SearchQuery;
import com.fresconews.fresco.framework.persistence.models.User_Table;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by Blaze on 7/22/2016.
 */
public class SearchManager {
    private static final String TAG = SearchManager.class.getSimpleName();

    private SearchService searchService;
    private Context context;

    private int searchInt;

    public SearchManager(SearchService searchService, Context context) {
        this.searchService = searchService;
        this.context = context;
    }

    public void clearSearchTerms() {
        SQLite.delete(Story.class)
              .execute();
        SQLite.delete(SearchQuery.class)
              .execute();
        SQLite.delete(Gallery_SearchQuery.class)
              .execute();
        SQLite.delete(Story_Gallery.class)
              .execute();
        SQLite.delete(Story_SearchQuery.class)
              .execute();
        SQLite.delete(User.class)
              .execute();
        SQLite.delete(User_SearchQuery.class)
              .execute();
        SQLite.delete(Gallery.class)
              .where(Gallery_Table.searchInt.greaterThan(0))
              .execute();
    }

    public Observable<List<Gallery>> downloadGalleries(SearchQuery searchQuery) {
        return handleGalleries(searchService.searchGalleries(searchQuery.getQuery()));
    }

    public Observable<List<Gallery>> downloadGalleries(SearchQuery searchQuery, int limit) {
        return handleGalleries(searchService.searchGalleries(searchQuery.getQuery(), limit));
    }

    public Observable<List<Gallery>> downloadGalleries(SearchQuery searchQuery, int limit, String last) {
        return handleGalleries(searchService.searchGalleries(searchQuery.getQuery(), limit, last));
    }

    public SearchQuery addQueryToDatabase(String query) {
        SearchQuery searchQuery = SQLite.select().from(SearchQuery.class).where(SearchQuery_Table.query.eq(query)).querySingle();
        if (searchQuery == null) {
            searchQuery = new SearchQuery();
            searchQuery.setQuery(query);

            FastStoreModelTransaction<SearchQuery> storeSearchQuery = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(SearchQuery.class))
                    .add(searchQuery)
                    .build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeSearchQuery);
        }
        return searchQuery;
    }

    private Observable<List<Gallery>> handleGalleries(Observable<NetworkSearchResults> response) {
        return response.map(type -> {
            List<Gallery> galleryResult = new ArrayList<>(type.getGalleries().getResults().size());
            for (NetworkGallery networkGallery : type.getGalleries().getResults()) {
                Gallery gallery = Gallery.from(networkGallery);
                gallery.setSearchInt(searchInt++);
                galleryResult.add(gallery);
            }

            FastStoreModelTransaction<Gallery> storeGallery = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(Gallery.class))
                    .addAll(galleryResult)
                    .build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeGallery);

            return galleryResult;
        });
    }

    public IDataSource<Gallery> getGalleryListDataSource() {
        FlowQueryList<Gallery> queryList = SQLite.select()
                                                 .from(Gallery.class)
                                                 .where(Gallery_Table.searchInt.greaterThan(-1))
                                                 .orderBy(Gallery_Table.searchInt, true)
                                                 .flowQueryList();

        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList, true);
    }

    public Observable<List<Story>> downloadStories(SearchQuery searchQuery, int limit) {
        return handleStories(searchService.searchStories(searchQuery.getQuery(), limit));
    }

    public Observable<List<Story>> downloadStories(SearchQuery searchQuery, int limit, String last) {
        return handleStories(searchService.searchStories(searchQuery.getQuery(), limit, last));
    }

    private Observable<List<Story>> handleStories(Observable<NetworkSearchResults> response) {
        return response.map(type -> {
            List<Story> result = new ArrayList<>(type.getStories().getResults().size());

            for (NetworkStory networkStoryPreview : type.getStories().getResults()) {
                Story story = Story.from(networkStoryPreview);
                story.setSearchInt(searchInt++);
                story.setTotalStoriesFromSearchQuery(type.getStoriesCount());

                result.add(story);
            }

            FastStoreModelTransaction<Story> storeStories = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(Story.class))
                    .addAll(result)
                    .build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeStories);

            return result;
        });
    }

    public IDataSource<Story> getStoryListDataSource() {
        FlowQueryList<Story> queryList = SQLite.select()
                                               .from(Story.class)
                                               .where(Story_Table.searchInt.greaterThan(-1))
                                               .flowQueryList();

        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    public Observable<List<User>> downloadUsers(SearchQuery searchQuery, int limit) {
        return handleUsers(searchService.searchUsers(searchQuery.getQuery(), limit));
    }

    public Observable<List<User>> downloadUsers(SearchQuery searchQuery, int limit, String last) {
        return handleUsers(searchService.searchUsers(searchQuery.getQuery(), limit, last));
    }

    public Observable<List<User>> autocompleteUsers(String prefix) {
        return searchService.autoCompleteUsers(prefix)
                            .map(networkSearchResults -> {
                                List<User> result = new ArrayList<>(networkSearchResults.getUsers().getResults().size());
                                for (NetworkUser networkUser : networkSearchResults.getUsers().getResults()) {
                                    User user = User.from(networkUser);
                                    user.setSearchInt(searchInt++);
                                    user.setTotalUsersFromSearchQuery(networkSearchResults.getUsersCount());
                                    result.add(user);
                                }
                                return result;
                            });
    }

    private Observable<List<User>> handleUsers(Observable<NetworkSearchResults> response) {
        return response.map(type -> {
            List<User> result = new ArrayList<>(type.getUsers().getResults().size());
            for (NetworkUser networkUser : type.getUsers().getResults()) {
                User user = User.from(networkUser);
                user.setSearchInt(searchInt++);
                user.setTotalUsersFromSearchQuery(type.getUsersCount());
                result.add(user);
            }

            FastStoreModelTransaction<User> users = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(User.class))
                    .addAll(result)
                    .build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(users);

            return result;
        });
    }

    public IDataSource<User> getUserListDataSource() {
        FlowQueryList<User> queryList = SQLite.select()
                                              .from(User.class)
                                              .where(User_Table.searchInt.greaterThan(-1))
                                              .flowQueryList();
        queryList.registerForContentChanges(context);
        //The below code was moved into the DBFlowDataSource in onDetached.
//        try {
//            queryList.endTransactionAndNotify();
//            queryList.close();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
        return new DBFlowDataSource<>(queryList);
    }

    public int getSearchInt() {
        return searchInt;
    }

    public void setSearchInt(int searchInt) {
        this.searchInt = searchInt;
    }
}
