package com.fresconews.fresco.v2.submission.assignmentlist;

import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.fresconews.fresco.framework.persistence.models.Assignment;

import java.util.List;

public class AssignmentListItem implements ParentListItem {
    private List<OutletListItem> mOutlets;

    private Assignment mAssignment;

    public AssignmentListItem(Assignment assignment, List<OutletListItem> outlets) {
        mAssignment = assignment;
        mOutlets = outlets;
    }

    @Override
    public List<OutletListItem> getChildItemList() {
        return mOutlets;
    }

    @Override
    public boolean isInitiallyExpanded() {
        return false;
    }

    public String getTitle() {
        return mAssignment.getTitle();
    }

    public Assignment getAssignment() {
        return mAssignment;
    }
}
