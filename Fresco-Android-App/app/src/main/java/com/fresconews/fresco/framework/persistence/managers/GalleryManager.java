package com.fresconews.fresco.framework.persistence.managers;

import android.content.Context;

import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.network.responses.NetworkComment;
import com.fresconews.fresco.framework.network.responses.NetworkCommentEntity;
import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkReport;
import com.fresconews.fresco.framework.network.responses.NetworkPost;
import com.fresconews.fresco.framework.network.responses.NetworkSuccessResult;
import com.fresconews.fresco.framework.network.responses.NetworkUser;
import com.fresconews.fresco.framework.network.services.GalleryService;
import com.fresconews.fresco.framework.persistence.DBFlowDataSource;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.fresconews.fresco.framework.persistence.models.Comment;
import com.fresconews.fresco.framework.persistence.models.CommentEntity;
import com.fresconews.fresco.framework.persistence.models.CommentEntity_Table;
import com.fresconews.fresco.framework.persistence.models.Comment_Table;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Gallery_Post;
import com.fresconews.fresco.framework.persistence.models.Gallery_Post_Table;
import com.fresconews.fresco.framework.persistence.models.Gallery_Table;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Post_Table;
import com.fresconews.fresco.framework.persistence.models.Story;
import com.fresconews.fresco.framework.persistence.models.Story_Gallery;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GalleryManager {
    private static final String TAG = GalleryManager.class.getSimpleName();

    private static final int DELAY_POLL_HIGHLIGHTS = 15;

    private GalleryService galleryService;
    private FeedManager feedManager;
    private SessionManager sessionManager;
    private Context context;
    private Gallery firstGallery;

    public GalleryManager(GalleryService galleryService, FeedManager feedManager, SessionManager sessionManager, Context context) {
        this.galleryService = galleryService;
        this.feedManager = feedManager;
        this.sessionManager = sessionManager;
        this.context = context;
    }

    public Observable<List<Gallery>> downloadHighlights(int limit, String last) {
        return handleGalleries(galleryService.highlights(limit, last));
    }

    public Observable<List<Gallery>> downloadHighlights(int limit) {
        return handleGalleries(galleryService.highlights(limit), true);
    }

    public Observable<NetworkComment> comment(String galleryId, String comment) {
        return galleryService.comment(galleryId, comment);
    }

    public Comment getComment(String commentId) {
        return SQLite.select().from(Comment.class).where(Comment_Table.id.eq(commentId)).querySingle();
    }

    public Observable<NetworkSuccessResult> deleteComment(String galleryId, String commentId) {
        return galleryService.deleteComment(galleryId, commentId);
    }

    public Observable<List<Comment>> downloadComment(String galleryId, String commentId) {
        return handleComment(galleryService.getComment(galleryId, commentId), galleryId);
    }

    public Observable<List<Comment>> downloadComments(String galleryId, int limit) {
        return handleComments(galleryService.getComments(galleryId, limit), galleryId);
    }

    public Observable<List<Comment>> downloadComments(String galleryId, int limit, String lastId) {
        return handleComments(galleryService.getComments(galleryId, limit, lastId), galleryId);
    }

    public Observable<List<Comment>> downloadNewerComments(String galleryId, int limit, String lastId) {
        return handleComments(galleryService.getComments(galleryId, limit, lastId, "asc"), galleryId); //'desc'
    }

    private Observable<List<Comment>> handleComment(Observable<NetworkComment> response, String galleryId) {
        return response.map(networkComment -> {
            List<Comment> result = new ArrayList<>();
            List<CommentEntity> entities = new ArrayList<CommentEntity>(); //can't really preset size
            Comment comment = Comment.from(networkComment);
            comment.setGalleryId(galleryId);
            if (!comment.exists()) {
                result.add(comment);
            }

            FastStoreModelTransaction<Comment> storeComment = FastStoreModelTransaction.saveBuilder(FlowManager
                    .getModelAdapter(Comment.class)).addAll(result).build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeComment);

            //Now create all the entities
            if (networkComment.getNetworkCommentEntities() != null && networkComment.getNetworkCommentEntities().length != 0) {
                for (NetworkCommentEntity networkCommentEntity : networkComment.getNetworkCommentEntities()) {
                    CommentEntity commentEntity = CommentEntity.from(networkCommentEntity);
                    if (commentEntity.exists()) {
                        continue;
                    }
                    entities.add(commentEntity);
                }

                //Save comment entities to DB
                FastStoreModelTransaction<CommentEntity> storeCommentEntity = FastStoreModelTransaction.saveBuilder(FlowManager
                        .getModelAdapter(CommentEntity.class)).addAll(entities).build();
                FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeCommentEntity);
            }

            return result;
        });
    }

    private Observable<List<Comment>> handleComments(Observable<List<NetworkComment>> response, String galleryId) {
        return response.map(comments -> {
            List<Comment> result = new ArrayList<>(comments.size());
            List<CommentEntity> entities = new ArrayList<CommentEntity>(); //can't really preset size
            for (NetworkComment networkComment : comments) {
                //Make the Comments to save to DB
                Comment comment = Comment.from(networkComment);
                comment.setGalleryId(galleryId);
                if (comment.exists()) {
                    continue;
                }
                result.add(comment);

                //Now create all the entities
                if (networkComment.getNetworkCommentEntities() != null && networkComment.getNetworkCommentEntities().length != 0) {
                    for (NetworkCommentEntity networkCommentEntity : networkComment.getNetworkCommentEntities()) {
                        CommentEntity commentEntity = CommentEntity.from(networkCommentEntity);
                        if (commentEntity.exists()) {
                            continue;
                        }
                        entities.add(commentEntity);
                    }
                }
            }

            //Save comments to DB
            FastStoreModelTransaction<Comment> storeComment = FastStoreModelTransaction.saveBuilder(FlowManager
                    .getModelAdapter(Comment.class)).addAll(result).build();
            FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeComment);

            //Save comment entities to DB
            if (entities.size() > 0) {
                FastStoreModelTransaction<CommentEntity> storeCommentEntity = FastStoreModelTransaction.saveBuilder(FlowManager
                        .getModelAdapter(CommentEntity.class)).addAll(entities).build();
                FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeCommentEntity);
            }

            return result;
        });
    }

    public void saveCommentEntities(NetworkComment networkComment) {
        List<CommentEntity> entities = new ArrayList<CommentEntity>(); //can't really preset size

        //Now create all the entities
        if (networkComment.getNetworkCommentEntities() != null && networkComment.getNetworkCommentEntities().length != 0) {
            for (NetworkCommentEntity networkCommentEntity : networkComment.getNetworkCommentEntities()) {
                CommentEntity commentEntity = CommentEntity.from(networkCommentEntity);
                if (commentEntity.exists()) {
                    continue;
                }
                entities.add(commentEntity);
            }
        }

        //Save comment entities to DB
        FastStoreModelTransaction<CommentEntity> storeCommentEntity = FastStoreModelTransaction.saveBuilder(FlowManager
                .getModelAdapter(CommentEntity.class)).addAll(entities).build();
        FlowManager.getDatabase(FrescoDatabase.class).executeTransaction(storeCommentEntity);
    }

    public IDataSource<Comment> getCommentsDataSource(String galleryId) {
        FlowQueryList<Comment> queryList = SQLite.select()
                                                 .from(Comment.class)
                                                 .where(Comment_Table.galleryId.eq(galleryId))
                                                 .orderBy(Comment_Table.createdAt, true) // Sort by created at
                                                 .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList, true);
    }

    public IDataSource<CommentEntity> getCommentsEntities(String commentId) {
        FlowQueryList<CommentEntity> queryList = SQLite.select()
                                                       .from(CommentEntity.class)
                                                       .where(CommentEntity_Table.commentId.eq(commentId))
                                                       .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    public Observable<NetworkGallery> downloadNetworkGallery(String id) {
        return galleryService.get(id);
    }

    public Observable<Gallery> downloadGallery(String id) {
        return handleGallery(galleryService.get(id));
    }

    public Observable<List<Post>> getPurchases(String id) {
        return galleryService.getPurchasedPosts(id)
                             .subscribeOn(Schedulers.io())
                             .observeOn(AndroidSchedulers.mainThread())
                             .onErrorReturn(throwable -> null)
                             .map(networkPosts -> {
                                 if (networkPosts != null) {
                                     List<Post> posts = new ArrayList<>();
                                     int i = 0;
                                     for (NetworkPost networkPost : networkPosts) {
                                         Post post = Post.from(networkPost, i);
                                         post.save();

                                         posts.add(post);
                                         i++;
                                     }
                                     return posts;
                                 }
                                 return null;
                             });
    }

    public Observable<List<Gallery>> downloadGalleries(List<String> galleryIds) {
        StringBuilder idBuilder = new StringBuilder();
        for (String galleryId : galleryIds) {
            idBuilder.append(galleryId);
            idBuilder.append(",");
        }
        idBuilder.deleteCharAt(idBuilder.length() - 1);

        return handleGalleries(galleryService.getList(idBuilder.toString()));
    }

    public List<Post> getFirstPostForGallery(String galleryId) {
        return SQLite.select()
                     .from(Post.class)
                     .innerJoin(Gallery_Post.class)
                     .on(Post_Table.id.withTable().eq(Gallery_Post_Table.post_id.withTable()))
                     .where(Gallery_Post_Table.gallery_id.withTable().eq(galleryId))
                     .groupBy(Gallery_Post_Table.post_id.withTable())
//                     .orderBy(Post_Table.createdAt, false)
                     .orderBy(Post_Table.index, true)
                     .limit(1)
                     .queryList();
    }

    public List<Post> getPostsForGallery(String galleryId) {
        return SQLite.select()
                     .from(Post.class)
                     .innerJoin(Gallery_Post.class)
                     .on(Post_Table.id.withTable().eq(Gallery_Post_Table.post_id.withTable()))
                     .where(Gallery_Post_Table.gallery_id.withTable().eq(galleryId))
                     .groupBy(Gallery_Post_Table.post_id.withTable())
//                     .orderBy(Post_Table.createdAt, true)
                     .orderBy(Post_Table.index, true)
                     .queryList();
    }

    public Observable<Post> getPost(String id) {
        return Observable.create(subscriber -> {
            SQLite.select()
                  .from(Post.class)
                  .where(Post_Table.id.eq(id))
                  .async()
                  .querySingleResultCallback((transaction, post) -> {
                      subscriber.onNext(post);
                      subscriber.onCompleted();
                  });
        });
    }

    public Observable<Gallery> getGallery(String id) {
        return Observable.create(subscriber -> {
            SQLite.select()
                  .from(Gallery.class)
                  .where(Gallery_Table.id.eq(id))
                  .orderBy(Gallery_Table.createdAt, false) // false = DESC
                  .async()
                  .querySingleResultCallback((transaction, gallery) -> {
                      if (subscriber != null) {
                          if (gallery != null) {
                              subscriber.onNext(gallery);
                          }
                          subscriber.onCompleted();
                      }
                  })
                  .execute();
        });
    }

    public Observable<Gallery> getOrDownloadGallery(String id) {
        return getGallery(id).switchIfEmpty(downloadGallery(id));
    }

    public IDataSource<Gallery> getHighlightsDataSource() {
        FlowQueryList<Gallery> queryList = SQLite.select()
                                                 .from(Gallery.class)
                                                 .where(Gallery_Table.highlightedAt.lessThan(new Date()))
                                                 .and(Gallery_Table.searchInt.lessThan(0))
                                                 .flowQueryList();
        queryList.registerForContentChanges(context);
        return new DBFlowDataSource<>(queryList);
    }

    public void clearGalleries() {
        SQLite.delete(Gallery.class).execute();
        SQLite.delete(Gallery_Post.class).execute();
        SQLite.delete(Comment.class).execute();
        SQLite.delete(CommentEntity.class).execute();
        SQLite.delete(Post.class).execute();
        SQLite.delete(Story_Gallery.class).execute();
        SQLite.delete(Story.class).execute();
    }

    public void clearHighlights() {
        SQLite.delete()
              .from(Gallery.class)
//              .where(Gallery_Table.rating.eq(Gallery.HIGHLIGHT))
              .where(Gallery_Table.highlightedAt.lessThan(new Date()))
              .execute();
    }

    public Observable<Boolean> like(Gallery gallery) {
        if (gallery.isLiked()) {
            return Observable.just(true);
        }

        likeLocal(gallery);

        return galleryService.like(gallery.getId())
                             .map(networkLikeResult -> true)
                             .onErrorReturn(throwable -> {
                                 LogUtils.e(TAG, "Error liking gallery", throwable);
                                 unlikeLocal(gallery);
                                 return false;
                             });
    }

    public Observable<Boolean> unlike(Gallery gallery) {
        if (!gallery.isLiked()) {
            return Observable.just(true);
        }

        unlikeLocal(gallery);

        return galleryService.unlike(gallery.getId())
                             .map(networkLikeResult -> true)
                             .onErrorReturn(throwable -> {
                                 LogUtils.e(TAG, "Error unliking gallery", throwable);
                                 likeLocal(gallery);
                                 return false;
                             });
    }

    public Observable<Boolean> repost(Gallery gallery) {

        if (gallery.isReposted()) {
            return Observable.just(true);
        }

        repostLocal(gallery);

        return galleryService.repost(gallery.getId())
                             .map(networkLikeResult -> true)
                             .onErrorReturn(throwable -> {
                                 LogUtils.e(TAG, "Error reposting gallery", throwable);
                                 unrepostLocal(gallery);
                                 return false;
                             });
    }

    public Observable<Boolean> unrepost(Gallery gallery) {
        if (!gallery.isReposted()) {
            return Observable.just(true);
        }

        unrepostLocal(gallery);

        return galleryService.unrepost(gallery.getId())
                             .map(networkLikeResult -> true)
                             .onErrorReturn(throwable -> {
                                 LogUtils.e(TAG, "Error reposting gallery", throwable);
                                 repostLocal(gallery);
                                 return false;
                             });
    }

    private Observable<Gallery> handleGallery(Observable<NetworkGallery> response) {
        return response.map(networkGallery -> {
            Gallery gallery = Gallery.from(networkGallery);
            gallery.save();
            return gallery;
        });
    }

    private Observable<List<Gallery>> handleGalleries(Observable<List<NetworkGallery>> response, boolean saveFirstGallery) {
        return response.map(galleries -> {
            List<Gallery> result = new ArrayList<>(galleries.size());
            for (NetworkGallery networkGallery : galleries) {
                Gallery gallery = Gallery.from(networkGallery);
                result.add(gallery);
            }

            FastStoreModelTransaction<Gallery> storeGallery =
                    FastStoreModelTransaction.saveBuilder(FlowManager.getModelAdapter(Gallery.class))
                                             .addAll(result)
                                             .build();
            FlowManager.getDatabase(FrescoDatabase.class)
                       .executeTransaction(storeGallery);

            if (saveFirstGallery && !result.isEmpty()) {
                firstGallery = result.get(0);
            }

            return result;
        });
    }

    private Observable<List<Gallery>> handleGalleries(Observable<List<NetworkGallery>> response) {
        return handleGalleries(response, false);
    }

    private void likeLocal(Gallery gallery) {
        gallery.setLiked(true);
        gallery.setLikes(gallery.getLikes() + 1);
        gallery.save();

        feedManager.like(gallery);
    }

    private void unlikeLocal(Gallery gallery) {
        gallery.setLiked(false);
        gallery.setLikes(gallery.getLikes() - 1);
        gallery.save();

        feedManager.unlike(gallery);
    }

    private void repostLocal(Gallery gallery) {
        gallery.setReposted(true);
        gallery.setReposts(gallery.getReposts() + 1);
        gallery.save();

        feedManager.repost(gallery);
    }

    private void unrepostLocal(Gallery gallery) {
        gallery.setReposted(false);
        gallery.setReposts(gallery.getReposts() - 1);
        gallery.save();

        feedManager.unrepost(gallery);
    }

    public Observable<Boolean> pollHighlights(int secondsInterval) {
        return Observable.interval(0, secondsInterval, TimeUnit.SECONDS, Schedulers.io())
                         .filter(aLong -> sessionManager.isLoggedIn())
                         .flatMap(aLong -> galleryService.highlights(1))
                         .onErrorReturn(throwable -> null)
                         .map(galleries -> galleries == null || galleries.isEmpty() || (!galleries.isEmpty() && firstGallery != null && galleries.get(0).getId().equals(firstGallery.getId())));
    }

    public Observable<Boolean> pollHighlights() {
        return pollHighlights(DELAY_POLL_HIGHLIGHTS);
    }

    public Observable<List<User>> usersReposted(String id, int limit) {
        return handleUsers(galleryService.usersReposted(id, limit));
    }

    public Observable<List<User>> usersReposted(String id, int limit, String last) {
        return handleUsers(galleryService.usersReposted(id, limit, last));
    }

    public Observable<List<User>> usersLiked(String id, int limit) {
        return handleUsers(galleryService.usersLiked(id, limit));
    }

    public Observable<List<User>> usersLiked(String id, int limit, String last) {
        return handleUsers(galleryService.usersLiked(id, limit, last));
    }

    private Observable<List<User>> handleUsers(Observable<List<NetworkUser>> response) {
        return response.map(networkUsers -> {
            List<User> result = new ArrayList<>(networkUsers.size());
            for (NetworkUser networkUser : networkUsers) {
                User user = User.from(networkUser);
                result.add(user);
            }
            return result;
        });
    }

    public Observable<NetworkReport> report(String id, String reason, String message) {
        return galleryService.report(id, reason, message);
    }
}
