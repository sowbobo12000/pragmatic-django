package com.fresconews.fresco.framework.persistence.managers;

import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Post_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import rx.Observable;

/**
 * Created by Ryan on 6/10/2016.
 */
public class PostManager
{
    private static final String TAG = PostManager.class.getSimpleName();

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
}
