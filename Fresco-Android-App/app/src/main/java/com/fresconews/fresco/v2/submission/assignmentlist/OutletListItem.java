package com.fresconews.fresco.v2.submission.assignmentlist;

import com.fresconews.fresco.framework.persistence.models.Assignment;
import com.fresconews.fresco.framework.persistence.models.Outlet;

/**
 * Created by ryan on 7/12/2016.
 */
public class OutletListItem {
    private Outlet mOutlet;
    private Assignment mAssignment;

    public OutletListItem(Outlet outlet, Assignment assignment) {
        mOutlet = outlet;
        mAssignment = assignment;
    }

    public String getTitle() {
        return "Submit to: " + mOutlet.getTitle();
    }

    public Outlet getOutlet() {
        return mOutlet;
    }

    public Assignment getAssignment() {
        return mAssignment;
    }
}
