package com.fresconews.fresco.framework.persistence.models;

import android.net.Uri;
import android.support.annotation.IntDef;

import com.fresconews.fresco.framework.network.responses.NetworkArticle;
import com.fresconews.fresco.framework.network.responses.NetworkGallery;
import com.fresconews.fresco.framework.network.responses.NetworkPost;
import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.MultipleManyToMany;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The stats object is flattened out, to make it easier to store in the database
 */
@ModelContainer
@Table(database = FrescoDatabase.class)
@MultipleManyToMany({@ManyToMany(referencedTable = Post.class), @ManyToMany(referencedTable = SearchQuery.class)})
public class Gallery extends BaseModel {
    private static final String TAG = Gallery.class.getSimpleName();

    @IntDef({HIGHLIGHT})
    public @interface GalleryRating {
    }

    public static final int HIGHLIGHT = 3;

    @PrimaryKey
    private String id;

    @Column
    private String caption;

    @Column
    private String ownerId;

    @Column
    private int photos;

    @Column
    private int videos;

    @Column
    @GalleryRating
    private int rating;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @Column
    private Date actionAt;

    @Column
    private Date highlightedAt;

    @Column
    private int searchInt;

    @Column
    private int likes;

    @Column
    private boolean liked;

    @Column
    private int reposts;

    @Column
    private boolean reposted;

    @Column
    private String repostedBy;

    @Column
    private int commentCount;

    @Column
    private String originalOwnerId;

    @Column
    private String curatorId;

    private List<Article> articles;

    @OneToMany(methods = {OneToMany.Method.LOAD, OneToMany.Method.SAVE}, variableName = "articles", isVariablePrivate = true)
    public List<Article> loadArticles() {
        if (articles == null || articles.isEmpty()) {
            articles = SQLite.select()
                             .from(Article.class)
                             .where(Article_Table.parentGallery_id.eq(id))
                             .queryList();
        }
        return articles;
    }

    public Gallery() {
        articles = new ArrayList<>();
    }

    public static Gallery from(NetworkGallery networkGallery) {
        return from(networkGallery, null);
    }

    public static Gallery from(NetworkGallery networkGallery, List<Uri> localUris) {
        Gallery gallery = new Gallery();

        gallery.id = networkGallery.getId();
        gallery.caption = networkGallery.getCaption();

        gallery.ownerId = networkGallery.getOwner() == null ? null : networkGallery.getOwner().getId();

        gallery.originalOwnerId = networkGallery.getOwner() == null ? null : networkGallery.getOwner().getId();

        gallery.curatorId = networkGallery.getCuratorId();

        if (gallery.ownerId == null) {
            //For Author: owner_id > external_account_id > importer_id > curator_id
            gallery.ownerId = networkGallery.getOwnerId();
            if (gallery.ownerId == null) {
                gallery.ownerId = networkGallery.getExternalAccountId();
                if (gallery.ownerId == null) {
                    gallery.ownerId = networkGallery.getImporterId();
                    if (gallery.ownerId == null) {
                        gallery.ownerId = networkGallery.getCuratorId();
                        if (gallery.ownerId == null) {
                            gallery.ownerId = "";
                        }
                    }
                }
            }
        }

        gallery.photos = networkGallery.getPhotoCount();
        gallery.videos = networkGallery.getVideoCount();

        //noinspection WrongConstant
        gallery.rating = networkGallery.getRating();

        gallery.createdAt = networkGallery.getCreatedAt();

        gallery.updatedAt = networkGallery.getUpdatedAt();
        if (gallery.updatedAt == null) {
            gallery.updatedAt = gallery.createdAt;
        }

        gallery.actionAt = networkGallery.getActionAt();
        if (gallery.actionAt == null) {
            gallery.actionAt = gallery.createdAt;
        }

        gallery.highlightedAt = networkGallery.getHighlightedAt();
        if (gallery.highlightedAt == null) {
            gallery.highlightedAt = gallery.createdAt;
        }

        gallery.likes = networkGallery.getLikes();
        gallery.liked = networkGallery.isLiked();

        gallery.reposts = networkGallery.getReposts();
        gallery.reposted = networkGallery.isReposted();

        gallery.repostedBy = networkGallery.getRepostedBy();

        gallery.searchInt = -1;
        gallery.commentCount = networkGallery.getCommentCount();

        NetworkPost[] networkPosts = networkGallery.getPosts();
        if (networkPosts != null) {
            int i = 0;
            for (NetworkPost networkPost : networkPosts) {
                Post post = Post.from(networkPost, i);
                if (localUris != null && networkPost.getStatus() < 2) {
                    post.setStream(localUris.get(i).toString());
                }

                post.save();
                
                boolean relationExists = SQLite.selectCountOf()
                                               .from(Gallery_Post.class)
                                               .where(Gallery_Post_Table.gallery_id.eq(gallery.getId()))
                                               .and(Gallery_Post_Table.post_id.eq(post.getId()))
                                               .count() != 0;

                if (relationExists) {
                    continue;
                }

                Gallery_Post galleryPost = new Gallery_Post();
                galleryPost.setGallery(gallery);
                galleryPost.setPost(post);

                galleryPost.save();
                i++;
            }
        }

        NetworkArticle[] networkArticles = networkGallery.getArticles();
        if (networkArticles != null) {
            for (NetworkArticle networkArticle : networkArticles) {
                Article article = Article.from(networkArticle, gallery);
                article.save();
                gallery.articles.add(article);
            }
        }

        NetworkStory[] networkStories = networkGallery.getStories();
        if (networkStories != null) {
            for (NetworkStory networkStory : networkStories) {
                boolean relationExists = SQLite.selectCountOf()
                                               .from(Story_Gallery.class)
                                               .where(Story_Gallery_Table.gallery_id.eq(gallery.getId()))
                                               .and(Story_Gallery_Table.story_id.eq(networkStory.getId()))
                                               .count() != 0;

                if (relationExists) {
                    continue;
                }

                Story story = Story.from(networkStory);

                if (!story.exists()) {
                    story.save();
                }

                Story_Gallery storyGallery = new Story_Gallery();
                storyGallery.setGallery(gallery);
                storyGallery.setStory(story);

                storyGallery.save();
            }
        }

        return gallery;
    }

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public int getPhotos() {
        return photos;
    }

    public void setPhotos(int photos) {
        this.photos = photos;
    }

    public int getVideos() {
        return videos;
    }

    public void setVideos(int videos) {
        this.videos = videos;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Date getActionAt() {
        return actionAt;
    }

    public void setActionAt(Date actionAt) {
        this.actionAt = actionAt;
    }

    public Date getHighlightedAt() {
        return highlightedAt;
    }

    public void setHighlightedAt(Date highlightedAt) {
        this.highlightedAt = highlightedAt;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getReposts() {
        return reposts;
    }

    public void setReposts(int reposts) {
        this.reposts = reposts;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public int getSearchInt() {
        return searchInt;
    }

    public void setSearchInt(int searchInt) {
        this.searchInt = searchInt;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isLiked() {
        return this.liked;
    }

    public void setReposted(boolean reposted) {
        this.reposted = reposted;
    }

    public boolean isReposted() {
        return this.reposted;
    }

    public String getRepostedBy() {
        return repostedBy;
    }

    public void setRepostedBy(String repostedBy) {
        this.repostedBy = repostedBy;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public int hashCode() {
        return getId().hashCode() + getCaption().hashCode() + getLikes() + getReposts() + getCommentCount();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Gallery otherGallery = (Gallery) other;
        if (getId() != null && otherGallery.getId() != null && !getId().equals(otherGallery.getId())) {
            return false;
        }
        if (getCaption() != null && otherGallery.getCaption() != null && !getCaption().equals(otherGallery.getCaption())) {
            return false;
        }
        if (getLikes() != otherGallery.getLikes()) {
            return false;
        }
        if (getReposts() != otherGallery.getReposts()) {
            return false;
        }
        if (getCommentCount() != otherGallery.getCommentCount()) {
            return false;
        }
        return true;
    }

    public String getOriginalOwnerId() {
        return originalOwnerId;
    }

    public void setOriginalOwnerId(String originalOwnerId) {
        this.originalOwnerId = originalOwnerId;
    }

    public String getCuratorId() {
        return curatorId;
    }

    public void setCuratorId(String curatorId) {
        this.curatorId = curatorId;
    }

    //</editor-fold>
}
