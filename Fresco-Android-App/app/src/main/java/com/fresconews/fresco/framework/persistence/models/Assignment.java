package com.fresconews.fresco.framework.persistence.models;

import android.text.TextUtils;

import com.fresconews.fresco.framework.network.responses.NetworkAssignment;
import com.fresconews.fresco.framework.network.responses.NetworkOutlet;
import com.fresconews.fresco.framework.persistence.FrescoDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ManyToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

@Table(database = FrescoDatabase.class)
@ManyToMany(referencedTable = Outlet.class)
public class Assignment extends BaseModel {
    private static final String TAG = Assignment.class.getSimpleName();

    @PrimaryKey
    private String id;

    @Column
    private String title;

    @Column
    private String caption;

    @Column
    private String address;

    @Column
    private boolean isGlobal;

    @Column
    private Date startsAt;

    @Column
    private Date endsAt;

    @Column
    private float latitude;

    @Column
    private float longitude;

    @Column
    private float radius;

    @Column
    private boolean accepted;

    @Column
    private boolean acceptable;

    public static Assignment from(NetworkAssignment networkAssignment) {
        if (networkAssignment == null || TextUtils.isEmpty(networkAssignment.getId())) {
            return null;
        }

        Assignment assignment = new Assignment();

        assignment.id = networkAssignment.getId();

        assignment.title = networkAssignment.getTitle();
        assignment.caption = networkAssignment.getCaption();
        assignment.address = networkAssignment.getAddress();
        assignment.accepted = networkAssignment.isAccepted();
        assignment.acceptable = networkAssignment.isAcceptable();

        if (networkAssignment.getLocation() == null) {
            assignment.isGlobal = true;
            assignment.radius = 0;
            assignment.latitude = 0;
            assignment.longitude = 0;
        }
        else {
            assignment.isGlobal = false;
            assignment.radius = networkAssignment.getRadius();
            assignment.latitude = (float) networkAssignment.getLocation().lat();
            assignment.longitude = ((float) networkAssignment.getLocation().lon());
        }

        if (networkAssignment.getEndsAt() != null) {
            assignment.endsAt = networkAssignment.getEndsAt();
        }
        if (networkAssignment.getStartsAt() != null) {
            assignment.startsAt = networkAssignment.getStartsAt();
        }

        if (!assignment.exists()) {
            assignment.save();
        }

        if (networkAssignment.getOutlets() != null) {
            for (NetworkOutlet networkOutlet : networkAssignment.getOutlets()) {
                Outlet outlet = Outlet.from(networkOutlet);
                outlet.save();

                boolean relationExists = SQLite.selectCountOf()
                                               .from(Assignment_Outlet.class)
                                               .where(Assignment_Outlet_Table.assignment_id.eq(assignment.getId()))
                                               .and(Assignment_Outlet_Table.outlet_id.eq(outlet.getId()))
                                               .count() != 0;

                if (relationExists) {
                    continue;
                }

                Assignment_Outlet assignmentOutlet = new Assignment_Outlet();
                assignmentOutlet.setAssignment(assignment);
                assignmentOutlet.setOutlet(outlet);

                assignmentOutlet.save();
            }
        }

        return assignment;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }

    public Date getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(Date startsAt) {
        this.startsAt = startsAt;
    }

    public Date getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Date endsAt) {
        this.endsAt = endsAt;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAcceptable() {
        return acceptable;
    }

    public void setAcceptable(boolean acceptable) {
        this.acceptable = acceptable;
    }

    //</editor-fold>
}
