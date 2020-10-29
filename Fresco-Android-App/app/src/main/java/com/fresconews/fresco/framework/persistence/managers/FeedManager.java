package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;
import android.net.Uri;

import com.fresconews.fresco.framework.network.responses.NetworkFrescoObject;
import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.fresconews.fresco.framework.network.services.FeedService;
import com.fresconews.fresco.framework.persistence.DBFlowDataSource;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.fresconews.fresco.framework.persistence.models.FeedRecord;
import com.fresconews.fresco.framework.persistence.models.FeedRecord_Table;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Gallery_Post;
import com.fresconews.fresco.framework.persistence.models.Gallery_Post_Table;
import com.fresconews.fresco.framework.persistence.models.Gallery_Table;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Post_Table;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.schedulers.Schedulers;

public class FeedManager {

    private static final int DELAY_POLL_FOLLOWING = 15;

    private FeedService feedService;
    private SessionManager sessionManager;
    private Context context;
    private Map<NetworkGallery, List<Uri>> uploadingGalleries;
    private FeedRecord firstFeedRecord;

    public FeedManager(FeedService feedService, SessionManager sessionManager, Context context) {
        this.feedService = feedService;
        this.sessionManager = sessionManager;
        this.context = context;
        uploadingGalleries = new HashMap<>();
    }

    public Observable<List<BaseModel>> user(String id, int limit) {
        return handleFeed(feedService.user(id, limit), userFeedId(id));
    }

    public Observable<List<BaseModel>> user(String id, int limit, Date last) {
        return handleFeed(feedService.user(id, limit, last), userFeedId(id));
    }

    public IDataSource<FeedRecord> userFeedDataSource(String id) {
        FlowQueryList<FeedRecord> queryList = SQLite.select()
                                                    .from(FeedRecord.class)
                                                    .where(FeedRecord_Table.feedId.eq(userFeedId(id)))
                                                    .orderBy(FeedRecord_Table.actionAt, false)
                                                    .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    public Observable<List<BaseModel>> following(String id, int limit) {
        return handleFeed(feedService.following(id, limit), followingFeedId(id), true);
    }

    public Observable<List<BaseModel>> following(String id, int limit, Date last) {
        return handleFeed(feedService.following(id, limit, last), followingFeedId(id));
    }

    public IDataSource<FeedRecord> followingFeedDataSource(String id) {
        FlowQueryList<FeedRecord> queryList = SQLite.select()
                                                    .from(FeedRecord.class)
                                                    .where(FeedRecord_Table.feedId.eq(followingFeedId(id)))
                                                    .orderBy(FeedRecord_Table.actionAt, false)
                                                    .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    public Observable<List<BaseModel>> likes(String userId, int limit) {
        return handleFeed(feedService.likes(userId, limit), likesFeedId(userId));
    }

    public Observable<List<BaseModel>> likes(String userId, int limit, Date last) {
        return handleFeed(feedService.likes(userId, limit, last), likesFeedId(userId));
    }

    public IDataSource<FeedRecord> likesFeedDataSource(String userId) {
        FlowQueryList<FeedRecord> queryList = SQLite.select()
                                                    .from(FeedRecord.class)
                                                    .where(FeedRecord_Table.feedId.eq(likesFeedId(userId)))
                                                    .orderBy(FeedRecord_Table.actionAt, false)
                                                    .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    public void clearAllFeeds() {
        SQLite.delete(FeedRecord.class).execute();
    }

    public void clearUserFeed(String userId) {
        SQLite.delete(FeedRecord.class)
              .where(FeedRecord_Table.feedId.eq(userFeedId(userId)))
              .execute();
        SQLite.delete(Gallery.class)
              .where(Gallery_Table.ownerId.eq(userId))
              .execute();
        SQLite.delete(Post.class)
              .where(Post_Table.owner_id.eq(userId))
              .execute();
    }

    public void clearFollowingFeed(String userId) {
        SQLite.delete(FeedRecord.class)
              .where(FeedRecord_Table.feedId.eq(followingFeedId(userId)))
              .execute();
    }

    public void clearLikesFeed(String userId) {
        SQLite.delete(FeedRecord.class)
              .where(FeedRecord_Table.feedId.eq(likesFeedId(userId)))
              .execute();
    }

    public void like(Gallery gallery) {
        like(FeedRecord.GALLERY, gallery.getId());
    }

    public void like(Story story) {
        like(FeedRecord.STORY, story.getId());
    }

    private void like(@FeedRecord.RecordType String type, String itemId) {
        Session session = sessionManager.getCurrentSession();
        if (session == null || !sessionManager.isLoggedIn()) {
            return;
        }

        FeedRecord record = SQLite.select()
                                  .from(FeedRecord.class)
                                  .where(FeedRecord_Table.feedId.eq(likesFeedId(session.getUserId())))
                                  .and(FeedRecord_Table.type.eq(type))
                                  .and(FeedRecord_Table.itemId.eq(itemId))
                                  .querySingle();

        if (record != null) {
            record.setType(type);
            record.setFeedId(likesFeedId(session.getUserId()));
            record.setId(record.getFeedId() + ":" + itemId);
            record.setItemId(itemId);
            record.setActionAt(new Date());
            record.update();
        }
    }

    public void unlike(Gallery gallery) {
        unlike(FeedRecord.GALLERY, gallery.getId());
    }

    public void unlike(Story story) {
        unlike(FeedRecord.STORY, story.getId());
    }

    private void unlike(@FeedRecord.RecordType String type, String itemId) {
        Session session = sessionManager.getCurrentSession();
        if (session == null || !sessionManager.isLoggedIn()) {
            return;
        }

        FeedRecord record = SQLite.select()
                                  .from(FeedRecord.class)
                                  .where(FeedRecord_Table.feedId.eq(likesFeedId(session.getUserId())))
                                  .and(FeedRecord_Table.type.eq(type))
                                  .and(FeedRecord_Table.itemId.eq(itemId))
                                  .querySingle();

        if (record != null) {
            record.delete();
        }
    }

    public void repost(Gallery gallery) {
        repost(FeedRecord.GALLERY, gallery.getId());
    }

    public void repost(Story story) {
        repost(FeedRecord.STORY, story.getId());
    }

    private void repost(@FeedRecord.RecordType String type, String itemId) {
        Session session = sessionManager.getCurrentSession();
        if (session == null || !sessionManager.isLoggedIn()) {
            return;
        }

        FeedRecord record = SQLite.select()
                                  .from(FeedRecord.class)
                                  .where(FeedRecord_Table.feedId.eq(userFeedId(session.getUserId())))
                                  .and(FeedRecord_Table.type.eq(type))
                                  .and(FeedRecord_Table.itemId.eq(itemId))
                                  .querySingle();
        if (record != null) {
            record.setType(type);
            record.setFeedId(userFeedId(session.getUserId()));
            record.setId(record.getFeedId() + ":" + itemId);
            record.setItemId(itemId);
            record.setActionAt(new Date());
            record.update();
        }
    }

    public void unrepost(Gallery gallery) {
        unrepost(FeedRecord.GALLERY, gallery.getId());
    }

    public void unrepost(Story story) {
        unrepost(FeedRecord.STORY, story.getId());
    }

    private void unrepost(@FeedRecord.RecordType String type, String itemId) {
        Session session = sessionManager.getCurrentSession();
        if (session == null || !sessionManager.isLoggedIn()) {
            return;
        }

        FeedRecord record = SQLite.select()
                                  .from(FeedRecord.class)
                                  .where(FeedRecord_Table.feedId.eq(userFeedId(session.getUserId())))
                                  .and(FeedRecord_Table.type.eq(type))
                                  .and(FeedRecord_Table.itemId.eq(itemId))
                                  .querySingle();
        if (record != null) {
            record.delete();
        }
    }

    public void addUploadingGallery(NetworkGallery gallery, List<Uri> uris) {
        gallery.setActionAt(new Date());
        gallery.setCreatedAt(new Date());
        uploadingGalleries.put(gallery, uris);
    }

    public void clearUploadingGalleries() {
        uploadingGalleries.clear();
    }

    private String userFeedId(String userId) {
        return "user:" + userId;
    }

    private String followingFeedId(String userId) {
        return "following:" + userId;
    }

    private String likesFeedId(String userId) {
        return "likes:" + userId;
    }

    private Observable<List<BaseModel>> handleFeed(Observable<List<NetworkFrescoObject>> response, String feedId, boolean saveFirstFollowing) {
        return response.map(networkFrescoObjects -> {
            List<Gallery> galleries = new ArrayList<>();
            List<Story> stories = new ArrayList<>();
            List<FeedRecord> feedRecords = new ArrayList<>();
            List<BaseModel> result = new ArrayList<>();

            for (NetworkFrescoObject frescoObject : networkFrescoObjects) {
                if (frescoObject instanceof NetworkStory) {
                    NetworkStory networkStory = (NetworkStory) frescoObject;
                    Story story = Story.from(networkStory);
                    stories.add(story);
                    result.add(story);
                    feedRecords.add(FeedRecord.from(story, feedId));
                }
                else if (frescoObject instanceof NetworkGallery) {
                    NetworkGallery networkGallery = (NetworkGallery) frescoObject;
                    Gallery gallery = null;
                    for (Map.Entry<NetworkGallery, List<Uri>> entry : uploadingGalleries.entrySet()) {
                        if (networkGallery.getId().equals(entry.getKey().getId())) {
                            gallery = Gallery.from(networkGallery, entry.getValue());
                            break;
                        }
                    }

                    if (gallery == null) {
                        gallery = Gallery.from(networkGallery);
                    }

                    //Attempt to make sure that galleries from search don't get re-written over
//                    FlowQueryList<Gallery> searchGals = SQLite.select()
//                            .from(Gallery.class)
//                            .where(Gallery_Table.id.eq(gallery.getId()))
//                            .flowQueryList();
//
//
//                    if (searchGals == null || searchGals.size() == 0) {
//                        LogUtils.i("FeedManager", "adding gallery - " + gallery.getOwnerId() + " - " + gallery.getCaption());
//                        galleries.add(gallery);
//                    }
                    galleries.add(gallery);

                    result.add(gallery);
                    feedRecords.add(FeedRecord.from(gallery, feedId));
                }
            }

            if (uploadingGalleries.size() > 0) {
                for (Map.Entry<NetworkGallery, List<Uri>> entry : uploadingGalleries.entrySet()) {
                    if (feedId.equals(userFeedId(entry.getKey().getOwner().getId()))) {
                        NetworkGallery networkGallery = entry.getKey();
                        List<Post> posts = SQLite.select()
                                                 .from(Post.class)
                                                 .innerJoin(Gallery_Post.class)
                                                 .on(Post_Table.id.withTable().eq(Gallery_Post_Table.post_id.withTable()))
                                                 .where(Gallery_Post_Table.gallery_id.eq(networkGallery.getId()))
                                                 .and(Post_Table.status.eq(2))
                                                 .queryList();

                        if (posts.size() > 0) {
                            uploadingGalleries.clear();
                        }
                    }
                }
            }

            FastStoreModelTransaction<Gallery> storeGallery = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(Gallery.class))
                    .addAll(galleries)
                    .build();

            FastStoreModelTransaction<Story> storeStory = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(Story.class))
                    .addAll(stories)
                    .build();

            FastStoreModelTransaction<FeedRecord> storeFeed = FastStoreModelTransaction
                    .saveBuilder(FlowManager.getModelAdapter(FeedRecord.class))
                    .addAll(feedRecords)
                    .build();

            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeGallery);
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeStory);
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeFeed);

            if (saveFirstFollowing && feedRecords != null && !feedRecords.isEmpty()) {
                firstFeedRecord = feedRecords.get(0);
            }

            return result;
        });
    }

    private Observable<List<BaseModel>> handleFeed(Observable<List<NetworkFrescoObject>> response, String feedId) {
        return handleFeed(response, feedId, false);
    }

    public Observable<Boolean> pollFollowing(String id, int secondsInterval) {
        return Observable.interval(0, secondsInterval, TimeUnit.SECONDS, Schedulers.io())
                         .filter(aLong -> sessionManager.isLoggedIn())
                         .flatMap(aLong -> feedService.following(id, 1))
                         .onErrorReturn(throwable -> null)
                         .map(networkFrescoObjects -> networkFrescoObjects == null || networkFrescoObjects.isEmpty() || (!networkFrescoObjects.isEmpty() && firstFeedRecord != null && networkFrescoObjects.get(0).getId().equals(firstFeedRecord.getItemId())));
    }

    public Observable<Boolean> pollFollowing(String id) {
        return pollFollowing(id, DELAY_POLL_FOLLOWING);
    }

}
