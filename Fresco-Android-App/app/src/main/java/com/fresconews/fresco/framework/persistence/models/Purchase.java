package com.fresconews.fresco.framework.persistence.models;

import com.fresconews.fresco.framework.network.responses.NetworkPurchase;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

/**
 * Created by wumau on 12/13/2016.
 */
@Table(database = FrescoDatabase.class)
@ManyToMany(referencedTable = Outlet.class)
public class Purchase extends BaseModel {

    @PrimaryKey
    private String id;

    @Column
    private int amount;

    @ForeignKey
    private ForeignKeyContainer<Post> parentPost;

    public void associatePost(Post parentPost) {
        this.parentPost = FlowManager.getContainerAdapter(Post.class).toForeignKeyContainer(parentPost);
    }

    public static Purchase from(Post parentPost, NetworkPurchase networkPurchase) {
        Purchase purchase = new Purchase();
        purchase.id = networkPurchase.getId();
        purchase.amount = networkPurchase.getAmount();

        Outlet outlet = Outlet.from(networkPurchase.getOutlet());
        outlet.save();

        boolean relationExists = SQLite.selectCountOf()
                                       .from(Purchase_Outlet.class)
                                       .where(Purchase_Outlet_Table.purchase_id.eq(purchase.getId()))
                                       .and(Purchase_Outlet_Table.outlet_id.eq(outlet.getId()))
                                       .count() != 0;

        if (!relationExists) {
            Purchase_Outlet purchaseOutlet = new Purchase_Outlet();
            purchaseOutlet.setPurchase(purchase);
            purchaseOutlet.setOutlet(outlet);

            purchaseOutlet.save();
        }

        purchase.associatePost(parentPost);

        return purchase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public ForeignKeyContainer<Post> getParentPost() {
        return parentPost;
    }

    public void setParentPost(ForeignKeyContainer<Post> parentPost) {
        this.parentPost = parentPost;
    }

    public Outlet getOutlet() {
        Purchase_Outlet purchaseOutlet = SQLite.select()
                                               .from(Purchase_Outlet.class)
                                               .where(Purchase_Outlet_Table.purchase_id.eq(id))
                                               .querySingle();
        if (purchaseOutlet != null) {
            return purchaseOutlet.getOutlet();
        }
        return null;
    }
}
