package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkStory;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.MultipleManyToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ModelContainer
@Table(database = FrescoDatabase.class)
@MultipleManyToMany({@ManyToMany(referencedTable = Gallery.class), @ManyToMany(referencedTable = SearchQuery.class)})
public class Story extends BaseModel {
    @PrimaryKey
    private String id;

    @Column
    private String title;

    @Column
    private String caption;

    @Column
    private Date createdAt;

    @Column
    private Date updatedAt;

    @Column
    private Date actionAt;

    @Column
    private int likes;

    @Column
    private int reposts;

    @Column
    private boolean liked;

    @Column
    private boolean reposted;

    @Column
    private String repostedBy;

    @Column
    private String address;

    @Column
    private int searchInt;

    @Column
    private Integer totalStoriesFromSearchQuery;

    public Story() {
        thumbnails = new ArrayList<>();
    }

    private List<StoryThumbnail> thumbnails;

    public List<StoryThumbnail> loadThumbnails() {
        if (thumbnails == null || thumbnails.isEmpty()) {
            thumbnails = SQLite.select()
                               .from(StoryThumbnail.class)
                               .where(StoryThumbnail_Table.story_id.eq(id))
                               .queryList();
        }
        return thumbnails;
    }

    public static Story from(NetworkStory networkStory) {
        Story story = new Story();

        story.id = networkStory.getId();
        story.title = networkStory.getTitle();
        story.caption = networkStory.getCaption();

        story.searchInt = -1;

        story.createdAt = networkStory.getCreatedAt();
        story.updatedAt = networkStory.getUpdatedAt();
        if (story.updatedAt == null) {
            story.updatedAt = story.createdAt;
        }

        story.actionAt = networkStory.getActionAt();
        if (story.actionAt == null) {
            story.actionAt = story.createdAt;
        }

        story.likes = networkStory.getLikes();
        story.reposts = networkStory.getReposts();
        story.liked = networkStory.isLiked();
        story.reposted = networkStory.isReposted();
        story.repostedBy = networkStory.getRepostedBy();
        story.address = networkStory.getAddress();

        NetworkStory.NetworkStoryThumbnail[] thumbnails = networkStory.getThumbnails();
        if (thumbnails != null) {
            for (NetworkStory.NetworkStoryThumbnail networkThumbnail : thumbnails) {
                StoryThumbnail thumbnail = StoryThumbnail.from(story, networkThumbnail);
                thumbnail.save();
                story.thumbnails.add(thumbnail);
            }
        }

        return story;
    }

    //<editor-fold desc="Getters and Setters">
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public int getSearchInt() {
        return searchInt;
    }

    public void setSearchInt(int searchInt) {
        this.searchInt = searchInt;
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

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public boolean isReposted() {
        return reposted;
    }

    public void setReposted(boolean reposted) {
        this.reposted = reposted;
    }

    public String getRepostedBy() {
        return repostedBy;
    }

    public void setRepostedBy(String repostedBy) {
        this.repostedBy = repostedBy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<StoryThumbnail> getThumbnails() {
        return thumbnails;
    }

    public void setThumbnails(List<StoryThumbnail> thumbnails) {
        this.thumbnails = thumbnails;
    }

    public Integer getTotalStoriesFromSearchQuery() {
        return totalStoriesFromSearchQuery;
    }

    public void setTotalStoriesFromSearchQuery(Integer totalStoriesFromSearchQuery) {
        this.totalStoriesFromSearchQuery = totalStoriesFromSearchQuery;
    }

    @Override
    public int hashCode() {
        return getId().hashCode() + getCaption().hashCode() + getLikes() + getReposts();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        Story otherStory = (Story) other;
        if (getId() != null && otherStory.getId() != null && !getId().equals(otherStory.getId())) {
            return false;
        }
        if (getCaption() != null && otherStory.getCaption() != null && !getCaption().equals(otherStory.getCaption())) {
            return false;
        }
        if (getLikes() != otherStory.getLikes()) {
            return false;
        }
        if (getReposts() != otherStory.getReposts()) {
            return false;
        }
        return true;
    }

    //</editor-fold>
}
