package com.fresconews.fresco.ui.behavior;

import android.content.Context;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.fresconews.fresco.R;

/**
 * Created by Ryan on 4/26/2016.
 */
public class AssignmentToolbarBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    private int maxHeight;

    public AssignmentToolbarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);

        float maxHeightDp = attrs.getAttributeFloatValue("app", "behavior_maxHeightDp", 55);

        maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, maxHeightDp, context.getResources().getDisplayMetrics());
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency.getId() == R.id.assignment_drawer;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(dependency);
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            child.setVisibility(View.INVISIBLE);
        }
        else {
            child.setVisibility(View.VISIBLE);
        }

        int titleHeight = dependency.findViewById(R.id.handle).getHeight();

        if (dependency.getTop() + titleHeight * 2 > child.getTop() || child.getHeight() < maxHeight) {
            int maxTop = child.getBottom() - maxHeight;
            child.setTop(Math.max(dependency.getTop() + titleHeight * 2, maxTop));
            return true;
        }

        return super.onDependentViewChanged(parent, child, dependency);
    }
}
