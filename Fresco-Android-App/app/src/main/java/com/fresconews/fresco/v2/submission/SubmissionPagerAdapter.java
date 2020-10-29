package com.fresconews.fresco.v2.submission;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;

import java.util.List;

/**
 * Created by ryan on 7/7/2016.
 */
public class SubmissionPagerAdapter extends PagerAdapter {
    private List<MediaItemViewModel> submissions;

    public SubmissionPagerAdapter(List<MediaItemViewModel> submissions) {
        this.submissions = submissions;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.submission_view_new, container, true);
        binding.setVariable(BR.model, submissions.get(position));
        return binding.getRoot();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(((View) object));
    }

    @Override
    public int getCount() {
        return submissions.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
