package com.fresconews.fresco.v2.submission;

import android.content.Intent;
import android.databinding.Bindable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RadioButton;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableString;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AssignmentManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoUploadService2;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UploadManager;
import com.fresconews.fresco.framework.persistence.models.Assignment;
import com.fresconews.fresco.framework.persistence.models.GalleryCreateRequest;
import com.fresconews.fresco.framework.persistence.models.Outlet;
import com.fresconews.fresco.framework.persistence.models.PostCreateRequest;
import com.fresconews.fresco.framework.persistence.models.UploadStatusMessage;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.submission.assignmentlist.AssignmentAdapter;
import com.fresconews.fresco.v2.submission.assignmentlist.AssignmentListItem;
import com.fresconews.fresco.v2.submission.assignmentlist.OutletListItem;
import com.fresconews.fresco.v2.utils.KeyboardUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.fresconews.fresco.v2.utils.StringUtils;
import com.fresconews.fresco.v2.utils.TranscodingUtils;
import com.google.android.gms.maps.model.LatLng;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SubmissionViewModel extends ActivityViewModel<SubmissionActivity> {
    private static final String TAG = SubmissionViewModel.class.getSimpleName();

    @Inject
    UploadManager uploadManager;

    @Inject
    AssignmentManager assignmentManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<CirclePageIndicator> circleIndicator = new BindableView<>();
    public BindableView<CollapsingToolbarLayout> collapsingToolbar = new BindableView<>();

    public BindableView<RecyclerView> assignmentList = new BindableView<>();
    public BindableView<RecyclerView> globalList = new BindableView<>();
    public BindableView<RadioButton> noAssignmentButton = new BindableView<>();
    public BindableString caption = new BindableString();

    private ArrayList<MediaItemViewModel> mediaItems;
    private List<AssignmentListItem> assignmentListItems;
    private List<AssignmentListItem> globalListItems;

    private boolean globalVisible = false;
    private long mostRecentMediaItem = (new Date()).getTime();

    public SubmissionViewModel(SubmissionActivity activity, ArrayList<MediaItemViewModel> mediaItems) {
        super(activity);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
        this.mediaItems = mediaItems;
        assignmentListItems = new ArrayList<>();
        globalListItems = new ArrayList<>();

        setNavIcon(R.drawable.ic_navigation_arrow_back_white);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        if (viewPager.get() == null) {
            return;
        }

        KeyboardUtils.setupDismissKeyboardOnTapView(getActivity(), getRoot());
        SubmissionPagerAdapter adapter = new SubmissionPagerAdapter(mediaItems);
        viewPager.get().setAdapter(adapter);
        viewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (mediaItems != null) {
                    for (MediaItemViewModel mediaItemViewModel : mediaItems) {
                        if (mediaItemViewModel.videoView.get() != null) {
                            mediaItemViewModel.videoView.get().pause();
                        }
                    }
                    if (mediaItems.get(position) != null && mediaItems.get(position).videoView.get() != null) {
                        mediaItems.get(position).videoView.get().start();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        circleIndicator.get().setViewPager(viewPager.get());

        int indicatorRadius = Fresco2.getContext()
                                     .getResources()
                                     .getDimensionPixelSize(R.dimen.page_indicator_radius);
        circleIndicator.get().getLayoutParams().width = adapter.getCount() * indicatorRadius * 3;

        mostRecentMediaItem = 0; //just putting a little into the future
        List<LatLng> coords = new ArrayList<>();
        for (MediaItemViewModel mediaItem : mediaItems) {
            coords.add(new LatLng(mediaItem.getLat(), mediaItem.getLng()));
            if (mediaItem.getDate() != null && mediaItem.getDate().getTime() > mostRecentMediaItem) {
                mostRecentMediaItem = mediaItem.getDate().getTime(); // remember time is less for older items
            }
        }

        // TODO: The assignment selection list needs to be refactored to be more mvvm-y
        Map<Assignment, List<Outlet>> outletMap = new HashMap<>();
        Map<Assignment, List<Outlet>> globalOutletMap = new HashMap<>();
        assignmentManager.clearAssignments();

        assignmentManager.validAssignments(coords)
                         .observeOn(AndroidSchedulers.mainThread())
                         .onErrorReturn(throwable -> {
                             LogUtils.e(TAG, "Error getting assignments");
                             SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_getting_assignments);
                             return null;
                         })
                         .subscribe(assignments -> {
                             Observable.just(assignments)
                                       .flatMapIterable(x -> x)
                                       .filter(assignment -> assignment != null)
                                       .filter(assignment -> assignment.getStartsAt() != null)
                                       .filter(assignment -> assignment.getStartsAt().getTime() < mostRecentMediaItem)
                                       .flatMap(assignment -> assignmentManager.getOutletsForAssignment(assignment.getId()), (assignment, outlets) -> {
                                           if (assignment.isGlobal()) {
                                               globalOutletMap.put(assignment, outlets);
                                           }
                                           else {
                                               outletMap.put(assignment, outlets);
                                           }
                                           return outlets;
                                       })
                                       .doOnCompleted(() -> {
                                           // Global assignments
                                           for (Map.Entry<Assignment, List<Outlet>> assignmentListEntry : globalOutletMap.entrySet()) {
                                               List<OutletListItem> outletList = new ArrayList<>();
                                               if (assignmentListEntry.getValue().size() > 1) {
                                                   for (Outlet outlet : assignmentListEntry.getValue()) {
                                                       outletList.add(new OutletListItem(outlet, assignmentListEntry.getKey()));
                                                   }
                                               }
                                               globalListItems.add(new AssignmentListItem(assignmentListEntry.getKey(), outletList));
                                           }
                                           Collections.sort(globalListItems, (lhs, rhs) -> lhs.getAssignment().getId().compareTo(rhs.getAssignment().getId()));
                                           notifyPropertyChanged(BR.globalCount);
                                           notifyPropertyChanged(BR.globalAssignmentsString);

                                           globalList.get().setLayoutManager(new LinearLayoutManager(getActivity()));

                                           AssignmentAdapter globalAssignementAdapter = new AssignmentAdapter(getActivity(), globalListItems);
                                           globalAssignementAdapter.setListener(this::onSelected);
                                           globalList.get().setAdapter(globalAssignementAdapter);

                                           // Regular assignments
                                           for (Map.Entry<Assignment, List<Outlet>> assignmentListEntry : outletMap.entrySet()) {
                                               List<OutletListItem> outletList = new ArrayList<>();
                                               if (assignmentListEntry.getValue().size() > 1) {
                                                   for (Outlet outlet : assignmentListEntry.getValue()) {
                                                       outletList.add(new OutletListItem(outlet, assignmentListEntry.getKey()));
                                                   }
                                               }
                                               assignmentListItems.add(new AssignmentListItem(assignmentListEntry.getKey(), outletList));
                                           }
                                           Collections.sort(assignmentListItems, (lhs, rhs) -> lhs.getAssignment().getId().compareTo(rhs.getAssignment().getId()));
                                           notifyPropertyChanged(BR.assignmentCount);

                                           assignmentList.get().setLayoutManager(new LinearLayoutManager(getActivity()));

                                           AssignmentAdapter assignmentAdapter = new AssignmentAdapter(getActivity(), assignmentListItems);
                                           assignmentAdapter.setListener(this::onSelected);
                                           assignmentList.get().setAdapter(assignmentAdapter);
                                       })
                                       .onErrorReturn(throwable -> null)
                                       .subscribe();
                         });
    }

    public Action1<View> goBack = view -> {
        ActivityCompat.finishAfterTransition(getActivity());
    };

    private void onSelected(String assignmentId, String outletId) {
        if (assignmentList.get().getAdapter() != null) {
            ((AssignmentAdapter) assignmentList.get().getAdapter()).setSelected(assignmentId, outletId);
        }
        if (globalList.get().getAdapter() != null) {
            ((AssignmentAdapter) globalList.get().getAdapter()).setSelected(assignmentId, outletId);
        }

        if (assignmentId != null) {
            noAssignmentButton.get().setChecked(false);
        }
    }

    @Bindable
    public int getAssignmentCount() {
        return assignmentListItems.size();
    }

    @Bindable
    public int getGlobalCount() {
        return globalListItems.size();
    }

    @Bindable
    public String getGlobalAssignmentsString() {
        int globalAssignmentCount = getGlobalCount();
        return getActivity().getResources().getQuantityString(R.plurals.global_assignments, globalAssignmentCount, globalAssignmentCount);
    }

    @Bindable
    public boolean isGlobalVisible() {
        return globalVisible;
    }

    public Action1<View> toggleGlobal = view -> {
        globalVisible = !globalVisible;
        notifyPropertyChanged(BR.globalVisible);
    };

    public Action1<View> noAssignment = view -> {
        onSelected(null, null);
    };

    public Action1<View> submit = view -> {
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            return;
        }

        analyticsManager.finisedWritingSubmissionCaption();
        if (!isLoggedIn()) {

            OnboardingActivity.start(getActivity());
        }
        else {
            if (StringUtils.toNullIfEmpty(caption.get()) == null) {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_submitting_without_caption);
                return;
            }

            if (FrescoUploadService2.getCurrentUploadStatus().getUploadStatus() == UploadStatusMessage.UPLOADING) {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_already_uploading);
                return;
            }

            AssignmentAdapter adapter = (AssignmentAdapter) assignmentList.get().getAdapter();

            String assignmentId = null;
            String outletId = null;

            if (adapter != null) {
                LogUtils.i(TAG, "Submitting to Assignment: " + adapter.getSelectedAssignmentId() + " Outlet: " + adapter.getSelectedOutletId());

                assignmentId = adapter.getSelectedAssignmentId();
                outletId = adapter.getSelectedOutletId();
            }

            GalleryCreateRequest request = new GalleryCreateRequest()
                    .setCaption(caption.get())
                    .setAssignment(assignmentId)
                    .setOutletId(outletId);

            int videoCount = 0;
            int imageCount = 0;
            for (MediaItemViewModel mediaItem : mediaItems) {
                if (mediaItem.getMimeType().contains("video")) {
                    videoCount++;
                }
                if (mediaItem.getMimeType().contains("image")) {
                    imageCount++;
                }
                request.addPost(new PostCreateRequest()
                        .setLatitude(mediaItem.getLat())
                        .setLongitude(mediaItem.getLng())
                        .setCapturedAt(mediaItem.getDate())
                        .setUri(mediaItem.getUri()));
            }
//            analyticsManager.photosSubmitted(imageCount);
//            analyticsManager.videosSubmitted(videoCount);
            analyticsManager.submittedContent(imageCount, videoCount, assignmentId);
            TranscodingUtils.verifyStoragePermissions(getActivity());

            uploadManager.create(request);

            getActivity().finish();
            Intent intent = new Intent(getActivity(), HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            getActivity().startActivity(intent);
        }
    };
}
