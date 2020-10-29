package com.fresconews.fresco.v2.gallery;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.viewmodels.PostViewModel;
import com.fresconews.fresco.framework.persistence.models.Post;

import java.util.List;

public class PostPagerAdapter extends PagerAdapter {

    private Activity activity;
    private List<Post> posts;

    public PostPagerAdapter(Activity activity, List<Post> posts) {
        this.activity = activity;
        this.posts = posts;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) container.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.item_post, container, true);
        binding.setVariable(BR.model, new PostViewModel(activity, posts.get(position)));

        return binding.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
