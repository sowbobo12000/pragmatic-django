package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by wumau on 9/28/2016.
 */
@Table(database = FrescoDatabase.class)
public class SearchQuery extends BaseModel {

    @PrimaryKey(autoincrement = true)
    private int id;

    @Column
    private String query;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
