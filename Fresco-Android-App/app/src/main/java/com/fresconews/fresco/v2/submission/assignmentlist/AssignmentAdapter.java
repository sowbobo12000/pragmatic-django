package com.fresconews.fresco.v2.submission.assignmentlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentListItem;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder;
import com.fresconews.fresco.R;

import java.util.List;

public class AssignmentAdapter extends ExpandableRecyclerAdapter<AssignmentAdapter.AssignmentListItemViewHolder, AssignmentAdapter.OutletListItemViewHolder> {
    private static final String TAG = AssignmentAdapter.class.getSimpleName();

    private LayoutInflater mInflator;

    private String mSelectedAssignmentId;
    private String mSelectedOutletId;

    private OnAssignmentSelectedListener mListener;

    public AssignmentAdapter(Context context, @NonNull List<AssignmentListItem> parentItemList) {
        super(parentItemList);
        mInflator = LayoutInflater.from(context);
    }

    public void setListener(OnAssignmentSelectedListener listener) {
        mListener = listener;
    }

    public String getSelectedAssignmentId() {
        return mSelectedAssignmentId;
    }

    public String getSelectedOutletId() {
        return mSelectedOutletId;
    }

    public void setSelected(String assignmentId, String outletId) {
        mSelectedAssignmentId = assignmentId;
        mSelectedOutletId = outletId;
        notifyItemRangeChanged(0, getItemCount());
    }

    private void updateSelection() {
        if (mListener != null) {
            mListener.onSelected(mSelectedAssignmentId, mSelectedOutletId);
        }
    }

    @Override
    public AssignmentListItemViewHolder onCreateParentViewHolder(ViewGroup parentViewGroup) {
        View assignmentView = mInflator.inflate(R.layout.submission_assignment_view, parentViewGroup, false);
        return new AssignmentListItemViewHolder(assignmentView);
    }

    @Override
    public OutletListItemViewHolder onCreateChildViewHolder(ViewGroup childViewGroup) {
        View outletView = mInflator.inflate(R.layout.submission_outlet_view, childViewGroup, false);
        return new OutletListItemViewHolder(outletView);
    }

    @Override
    public void onBindParentViewHolder(AssignmentListItemViewHolder parentViewHolder, int position, ParentListItem parentListItem) {
        parentViewHolder.bind(((AssignmentListItem) parentListItem), this);
    }

    @Override
    public void onBindChildViewHolder(OutletListItemViewHolder childViewHolder, int position, Object childListItem) {
        childViewHolder.bind(((OutletListItem) childListItem), this);
    }

    public static class AssignmentListItemViewHolder extends ParentViewHolder {
        private static final String TAG = AssignmentListItemViewHolder.class.getSimpleName();


        private TextView mAssignmentTitle;
        private RadioButton mRadioButton;

        public AssignmentListItemViewHolder(View itemView) {
            super(itemView);
            mAssignmentTitle = ((TextView) itemView.findViewById(R.id.assignment_title));
            mRadioButton = ((RadioButton) itemView.findViewById(R.id.radio_button));
        }

        public void bind(AssignmentListItem listItem, AssignmentAdapter adapter) {
            if (adapter.mSelectedAssignmentId == null) {
                mRadioButton.setChecked(false);
            }
            else {
                mRadioButton.setChecked(listItem.getAssignment().getId().equals(adapter.mSelectedAssignmentId));
            }
            mRadioButton.setOnClickListener(view -> {
                adapter.setSelected(listItem.getAssignment().getId(), null);
                adapter.updateSelection();
            });
            if (listItem.getChildItemList().isEmpty()) {
                mRadioButton.setText(listItem.getTitle());
                mRadioButton.setVisibility(View.VISIBLE);
                mAssignmentTitle.setVisibility(View.GONE);
            }
            else {
                mAssignmentTitle.setText(listItem.getTitle());
                mRadioButton.setVisibility(View.GONE);
                mAssignmentTitle.setVisibility(View.VISIBLE);
            }
        }
    }

    public static class OutletListItemViewHolder extends ChildViewHolder {
        private static final String TAG = OutletListItemViewHolder.class.getSimpleName();

        private RadioButton mRadioButton;

        public OutletListItemViewHolder(View itemView) {
            super(itemView);
            mRadioButton = (RadioButton) itemView.findViewById(R.id.radio_button);
        }

        public void bind(OutletListItem listItem, AssignmentAdapter adapter) {
            if (adapter.mSelectedAssignmentId == null) {
                mRadioButton.setChecked(false);
            }
            else {
                mRadioButton.setChecked(listItem.getAssignment().getId().equals(adapter.mSelectedAssignmentId) &&
                        listItem.getOutlet().getId().equals(adapter.mSelectedOutletId));
            }

            mRadioButton.setText(listItem.getTitle());
            mRadioButton.setOnClickListener(view -> {
                adapter.setSelected(listItem.getAssignment().getId(), listItem.getOutlet().getId());
                adapter.updateSelection();
            });
        }
    }

    public interface OnAssignmentSelectedListener {
        void onSelected(String assignmentId, String outletId);
    }
}
