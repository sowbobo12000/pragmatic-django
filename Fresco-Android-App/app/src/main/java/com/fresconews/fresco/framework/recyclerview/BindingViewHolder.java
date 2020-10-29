package com.fresconews.fresco.framework.recyclerview;

import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.framework.mvvm.ViewModel;

public class BindingViewHolder<T extends ViewModel> extends RecyclerView.ViewHolder {

    private ViewDataBinding mDataBinding;

    public BindingViewHolder(View itemView) {
        super(itemView);
        mDataBinding = DataBindingUtil.bind(itemView);
        mDataBinding.executePendingBindings();
    }

    public void bind(T item) {
        if(item == null){
            return;
        }
        mDataBinding.setVariable(BR.model, item);
        mDataBinding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public void onBound(ViewDataBinding binding) {
                item.onBound();
                binding.removeOnRebindCallback(this);
            }
        });
    }
}
