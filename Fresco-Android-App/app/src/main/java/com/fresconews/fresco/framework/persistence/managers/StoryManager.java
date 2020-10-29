package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.fresconews.fresco.framework.network.services.StoryService;
import com.fresconews.fresco.framework.persistence.DBFlowDataSource;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Gallery_Table;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.persistence.models.Story_Gallery;
import com.fresconews.fresco.framework.persistence.models.Story_Gallery_Table;
import com.fresconews.fresco.framework.persistence.models.Story_Table;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class StoryManager {
    private static final String TAG = StoryManager.class.getSimpleName();

    private StoryService storyService;
    private FeedManager feedManager;
    private Context context;

    public StoryManager(StoryService storyService, FeedManager feedManager, Context context) {
        this.storyService = storyService;
        this.feedManager = feedManager;
        this.context = context;
    }

    public Observable<List<Story>> recent(int limit) {
        return handleStories(storyService.recent(limit));
    }

    public Observable<List<Story>> recent(int limit, String last) {
        return handleStories(storyService.recent(limit, last));
    }

    public Observable<List<Gallery>> downloadGalleries(String id, int limit) {
        return handleGalleries(storyService.galleries(id, limit));
    }

    public Observable<List<Gallery>> downloadGalleries(String id, int limit, String last) {
        return handleGalleries(storyService.galleries(id, limit, last));
    }

    public Observable<Story> downloadStory(String id) {
        return handleStory(storyService.get(id));
    }

    public Observable<Story> getStory(String id) {
        return Observable.create(subscriber -> {
            SQLite.select()
                  .from(Story.class)
                  .where(Story_Table.id.eq(id))
                  .orderBy(Story_Table.createdAt, false) // false = DESC
                  .async()
                  .querySingleResultCallback((transaction, story) -> {
                      if (story != null) {
                          subscriber.onNext(story);
                      }
                      subscriber.onCompleted();
                  })
                  .execute();
        });
    }

    public Observable<Story> getOrDownloadStory(String id) {
        return getStory(id).switchIfEmpty(downloadStory(id));
    }

    public IDataSource<Story> getAllStories() {
        FlowQueryList<Story> queryList = SQLite.select()
                                               .from(Story.class)
                                               .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList, true);
    }

    public Observable<Boolean> like(Story story) {
        if (story.isLiked()) {
            return Observable.just(true);
        }

        likeLocal(story);

        return storyService.like(story.getId())
                           .map(networkLikeResult -> true)
                           .onErrorReturn(throwable -> {
                               LogUtils.e(TAG, "Error liking story: " + story.getId(), throwable);
                               unlikeLocal(story);
                               return false;
                           });
    }

    public Observable<Boolean> unlike(Story story) {
        if (!story.isLiked()) {
            return Observable.just(true);
        }

        unlikeLocal(story);

        return storyService.unlike(story.getId())
                           .map(networkLikeResult -> true)
                           .onErrorReturn(throwable -> {
                               LogUtils.e(TAG, "Error unliking story: " + story.getId(), throwable);
                               likeLocal(story);
                               return false;
                           });
    }

    public Observable<Boolean> repost(Story story) {
        if (story.isReposted()) {
            return Observable.just(true);
        }

        repostLocal(story);

        return storyService.repost(story.getId())
                           .map(networkLikeResult -> true)
                           .onErrorReturn(throwable -> {
                               unrepostLocal(story);
                               return false;
                           });
    }

    public Observable<Boolean> unrepost(Story story) {
        if (!story.isReposted()) {
            return Observable.just(true);
        }
        unrepostLocal(story);

        return storyService.unrepost(story.getId())
                           .map(networkLikeResult -> true)
                           .onErrorReturn(throwable -> {
                               repostLocal(story);
                               return false;
                           });
    }

    public void clearStories() {
        SQLite.delete(Story.class).execute();
        SQLite.delete(Story_Gallery.class).execute();
    }

    public void clearStoryGalleries() {
        SQLite.delete(Story_Gallery.class).execute();
    }

    public IDataSource<Gallery> getGalleryDatasource(String storyId) {
        FlowQueryList<Gallery> queryList = SQLite.select()
                                                 .from(Gallery.class)
                                                 .innerJoin(Story_Gallery.class)
                                                 .on(Gallery_Table.id.withTable().eq(Story_Gallery_Table.gallery_id.withTable()))
                                                 .where(Story_Gallery_Table.story_id.withTable().eq(storyId))
                                                 .groupBy(Story_Gallery_Table.gallery_id.withTable())
                                                 .orderBy(Gallery_Table.createdAt, false) //false DESC
                                                 .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    private Observable<Story> handleStory(Observable<NetworkStory> response) {
        return response.map(networkStory -> {
            Story story = Story.from(networkStory);
            story.save();
            return story;
        });
    }

    private Observable<List<Story>> handleStories(Observable<List<NetworkStory>> response) {
        return response.map(networkStories -> {
            List<Story> result = new ArrayList<>(networkStories.size());
            for (NetworkStory networkStory : networkStories) {
                Story story = Story.from(networkStory);
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

    private Observable<List<Gallery>> handleGalleries(Observable<List<NetworkGallery>> response) {
        return response.map(galleries -> {
            List<Gallery> result = new ArrayList<>(galleries.size());
            for (NetworkGallery networkGallery : galleries) {
                Gallery gallery = Gallery.from(networkGallery);
                result.add(gallery);
            }

            FastStoreModelTransaction<Gallery> storeGallery = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(Gallery.class))
                    .addAll(result)
                    .build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeGallery);

            return result;
        });
    }

    private void likeLocal(Story story) {
        story.setLiked(true);
        story.setLikes(story.getLikes() + 1);
        story.save();

        feedManager.like(story);
    }

    private void unlikeLocal(Story story) {
        story.setLiked(false);
        story.setLikes(story.getLikes() - 1);
        story.save();

        feedManager.unlike(story);
    }

    private void repostLocal(Story story) {
        story.setReposted(true);
        story.setReposts(story.getReposts() + 1);
        story.save();

        feedManager.repost(story);
    }

    private void unrepostLocal(Story story) {
        story.setReposted(false);
        story.setReposts(story.getReposts() - 1);
        story.save();

        feedManager.unrepost(story);
    }
}
