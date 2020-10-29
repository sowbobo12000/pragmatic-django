package com.fresconews.fresco.v2.gallery.gallerydetail;

import android.databinding.Bindable;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.CommentViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.UserAutoCompleteViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.CommentViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserAutoCompleteViewModel;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SearchManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Comment;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.recyclerview.PagingRecyclerViewBindingAdapter;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.utils.DimensionUtils;
import com.fresconews.fresco.v2.utils.KeyboardUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by wumau on 11/10/2016.
 */

public class GalleryDetailCommentsViewModel extends ViewModel {
    private static final String TAG = GalleryDetailViewModel.class.getSimpleName();

    private static final int PAGE_SIZE = 10;
    private static final float AUTO_COMPLETE_PREVIEW_HEIGHT = 160f;
    private static final int STATE_NOT_EXPANDED = 0;
    private static final int STATE_EXPANDING = 1;
    private static final int STATE_EXPANDED = 2;

    @Inject
    GalleryManager galleryManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    UserManager userManager;

    @Inject
    SearchManager searchManager;

    public BindableView<RecyclerView> commentsRecyclerView = new BindableView<>();
    public BindableView<ViewGroup> commentBottomSheet = new BindableView<>();
    public BindableView<BackKeyEditText> commentEditText = new BindableView<>();
    public BindableView<RecyclerView> autoCompleteRecyclerView = new BindableView<>();
    public BindableView<FrameLayout> expandableLayout = new BindableView<>();

    private BottomSheetBehavior commentBottomSheetBehavior;
    private IDataSource<CommentViewModel> commentsDataSource;
    private IDataSource<UserAutoCompleteViewModel> autoCompleteDataSource;
    private PagingRecyclerViewBindingAdapter<CommentViewModel> adapter;
    private RecyclerViewBindingAdapter<UserAutoCompleteViewModel> autoCompleteAdapter;
    private List<User> autoCompleteUsers;
    private Subscription autoCompleteSubscription;
    private Gallery gallery;
    private BaseActivity activity;
    private String commentToDeleteId;
    private String addCommentButtonText;
    private String currentCommentId;
    private boolean hasBeenBound;
    private boolean showOverlay;
    private boolean keyBoardUp;
    private boolean showCommentEditText;
    private boolean loadingComments = false;
    private int currentState;
    private int screenHeight;

    private AppBarLayout appBarLayout;
    private NestedScrollView scrollView;

    public GalleryDetailCommentsViewModel(GalleryDetailActivity activity, String currentCommentId) {
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.activity = activity;
        this.currentCommentId = currentCommentId;
        autoCompleteUsers = new ArrayList<>();

        Point size = DimensionUtils.getScreenDimensions();
        screenHeight = size.y;
    }

    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        hasBeenBound = true;

        commentEditText.get().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setKeyBoardUp(true);
                setShowCommentEditText(true);
                if (getRoot() != null) {
                    getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                            else {
                                getRoot().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }
                            if (currentState == STATE_EXPANDED) {
                                view.postDelayed(GalleryDetailCommentsViewModel.this::expandAutoComplete, 200);
                            }
                        }
                    });
                }
            }
        });

        autoCompleteRecyclerView.get().setOnTouchListener(new View.OnTouchListener() {
            float startY = 0;
            boolean firstTouch = true;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (currentState == STATE_EXPANDING || currentState == STATE_EXPANDED) {
                    return false;
                }
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = motionEvent.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (firstTouch) {
                            startY = motionEvent.getY();
                            firstTouch = false;
                        }
                        float y = motionEvent.getY();
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) view.getLayoutParams();
                        float top = Math.max(lp.topMargin + (y - startY) - scrollView.getScrollY(), getVisibleScreenHeight());
                        view.setLayoutParams(lp);
                        lp.topMargin = (int) top;
                        break;
                    case MotionEvent.ACTION_UP:
                        firstTouch = true;
                        if (((FrameLayout.LayoutParams) view.getLayoutParams()).topMargin < getAutoCompleteMarginTop()) {
                            expandAutoComplete();
                        }
                        else {
                            currentState = STATE_NOT_EXPANDED;
                        }
                        break;
                }
                return true;
            }
        });

        commentBottomSheetBehavior = BottomSheetBehavior.from(commentBottomSheet.get());
        commentBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    hideEditText();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        setComments();
        hideCommentBottomSheet();

        commentEditText.get().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String currentWord = getCurrentWord();
                if (currentWord.length() > 1 && currentWord.startsWith("@")) {
                    if (autoCompleteSubscription != null) {
                        autoCompleteSubscription.unsubscribe();
                    }
                    autoCompleteSubscription = searchManager.autocompleteUsers(currentWord.substring(1))
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribeOn(Schedulers.io())
                                                            .onErrorReturn(throwable -> null)
                                                            .subscribe(users -> {
                                                                updateAutoCompleteUsers(users);
                                                            });
                }
                else {
                    cancelAutoComplete();
                }
            }
        });

        commentEditText.get().setOnBackKeyClickListener(() -> {
            if (currentState == STATE_EXPANDED && isKeyBoardUp()) {
                autoCompleteRecyclerView.get().postDelayed(this::expandAutoComplete, 200);
            }
            setKeyBoardUp(false);
        });

        autoCompleteDataSource = new UserAutoCompleteViewModelDataSource(activity, new ListDataSource<>(autoCompleteUsers), username -> {
            String currentWord = getCurrentWord();
            String currentText = commentEditText.get().getText().toString();
            StringBuilder builder = new StringBuilder(currentText);
            int start = commentEditText.get().getSelectionStart();
            builder.delete(start - currentWord.length(), start);
            builder.insert(start - currentWord.length(), username);
            commentEditText.get().setText(builder.toString());
            autoCompleteUsers.clear();
            commentEditText.get().setSelection(start - currentWord.length() + username.length());
        });
    }

    private String getCurrentWord() {
        final int selection = commentEditText.get().getSelectionStart();
        final Pattern pattern = Pattern.compile("\\S+");
        final Matcher matcher = pattern.matcher(commentEditText.get().getText());
        int start;
        int end;
        String currentWord = "";
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            if (start <= selection && selection <= end) {
                currentWord = commentEditText.get().getText().subSequence(start, end).toString();
            }
        }

        return currentWord;
    }

    private void updateAutoCompleteUsers(List<User> users) {
        if (autoCompleteUsers.isEmpty()) {
            ((FrameLayout.LayoutParams) autoCompleteRecyclerView.get().getLayoutParams()).topMargin = getAutoCompleteMarginTop();
            currentState = STATE_NOT_EXPANDED;
        }

        if (users == null || users.isEmpty()) {
            cancelAutoComplete();
        }
        else {
            autoCompleteUsers.clear();
            autoCompleteUsers.addAll(users);
            setShowOverlay(true);
        }
        if (autoCompleteAdapter == null) {
            autoCompleteAdapter = new RecyclerViewBindingAdapter<>(R.layout.item_auto_complete_user, autoCompleteDataSource);
            autoCompleteRecyclerView.get().setLayoutManager(new LinearLayoutManager(activity));
            autoCompleteRecyclerView.get().setAdapter(autoCompleteAdapter);
        }
        else {
            autoCompleteAdapter.notifyDataSetChanged();
        }
    }

    private void cancelAutoComplete() {
        if (autoCompleteSubscription != null) {
            autoCompleteSubscription.unsubscribe();
        }
        autoCompleteUsers.clear();
        if (autoCompleteAdapter != null) {
            autoCompleteAdapter.notifyDataSetChanged();
        }
        setShowOverlay(false);
    }

    private int getAutoCompleteMarginTop() {
        if (getRoot() == null) {
            return 0;
        }

        Rect rectangle = new Rect();
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        return screenHeight - DimensionUtils.convertDpToPixel(AUTO_COMPLETE_PREVIEW_HEIGHT, activity);
    }

    private int getVisibleScreenHeight() {
        if (getRoot() == null) {
            return screenHeight;
        }
        Rect rect = new Rect();
        getRoot().getWindowVisibleDisplayFrame(rect);

        return screenHeight - rect.height();
    }

    private void expandAutoComplete() {
        int topMargin = ((FrameLayout.LayoutParams) autoCompleteRecyclerView.get().getLayoutParams()).topMargin;
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    ((FrameLayout.LayoutParams) autoCompleteRecyclerView.get().getLayoutParams()).topMargin = getVisibleScreenHeight();
                    currentState = STATE_EXPANDED;
                }
                else {
                    currentState = STATE_EXPANDING;
                    ((FrameLayout.LayoutParams) autoCompleteRecyclerView.get().getLayoutParams()).topMargin = topMargin - ((int) ((topMargin - getVisibleScreenHeight()) * interpolatedTime));
                }
                expandableLayout.get().requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setDuration((int) (Math.max(topMargin - getVisibleScreenHeight(), 1) / expandableLayout.get().getContext().getResources().getDisplayMetrics().density));
        expandableLayout.get().startAnimation(a);
    }

    public void setGallery(Gallery gallery) {
        this.gallery = gallery;
    }

    protected void deleteCommentViewHandler() {
        gallery.setCommentCount(gallery.getCommentCount() - 1);
        gallery.save();
        notifyPropertyChanged(BR.commentCount);
    }

    public boolean isKeyBoardUp() {
        return keyBoardUp;
    }

    public void setKeyBoardUp(boolean keyBoardUp) {
        this.keyBoardUp = keyBoardUp;
    }

    @Bindable
    public boolean isShowCommentEditText() {
        return showCommentEditText;
    }

    public void setShowCommentEditText(boolean showCommentEditText) {
        this.showCommentEditText = showCommentEditText;
        notifyPropertyChanged(BR.showCommentEditText);
    }

    public void setAppBarLayout(AppBarLayout appBarLayout) {
        this.appBarLayout = appBarLayout;
    }

    public void setScrollView(NestedScrollView scrollView) {
        this.scrollView = scrollView;
    }

    public void hideEditText() {
        if (autoCompleteUsers != null && !autoCompleteUsers.isEmpty() && !isKeyBoardUp()) {
            cancelAutoComplete();
        }
        else {
            commentEditText.get().setText("");
            setShowCommentEditText(false);
            setShowOverlay(false);
        }
        KeyboardUtils.hideKeyboard(activity);
    }

    public void addComment(String commentText) {
        //If user is not logged in, they can't comment, send them to log in.
        if (!sessionManager.isLoggedIn()) {
            OnboardingActivity.start(activity);
            return;
        }

        commentEditText.get().setHorizontallyScrolling(false);
        commentEditText.get().setMaxLines(5);

        commentEditText.get().setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                handled = true;
                setKeyBoardUp(false);
                setShowCommentEditText(false);
                setShowOverlay(false);
                KeyboardUtils.hideKeyboard(activity);
                sendComment(commentEditText.get().getText().toString());
            }
            return handled;
        });

        //Prepare editText to set focus and change visibility
        commentEditText.get().clearFocus();
        setKeyBoardUp(true);
        setShowCommentEditText(true);

        //Once editText is ready, request focus to bring the keyboard up
        commentEditText.get().post(() -> {
            if (commentEditText.get().requestFocus()) {
                KeyboardUtils.bringUpKeyboard(activity, commentEditText.get());
                commentEditText.get().setText("");

                if (commentText != null) {
                    commentEditText.get().append(commentText);
                }
            }
        });

        //Create a new listener to see if CustomEdit set the visibility to 0.
        commentEditText.get().setTag(commentEditText.get().getVisibility());

        commentEditText.get().getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int newVis = commentEditText.get().getVisibility();
            if ((int) commentEditText.get().getTag() != newVis) {
                commentEditText.get().setTag(commentEditText.get().getVisibility());
                //visibility has changed
                if (commentEditText.get().getVisibility() == View.GONE) {
                    setKeyBoardUp(false);
                    setShowCommentEditText(false);
                    setShowOverlay(false);
                }
            }
        });
    }

    private void sendComment(String comment) {
        Comment myNewComment = new Comment();

        if (sessionManager.isLoggedIn()) {
            String userId = sessionManager.getCurrentSession().getUserId();
            userManager.getUser(userId)
                       .observeOn(AndroidSchedulers.mainThread())
                       .subscribeOn(Schedulers.io())
                       .onErrorReturn(throwable -> null)
                       .subscribe(user -> {
                           if (TextUtils.isEmpty(myNewComment.getId())) {
                               myNewComment.setId("TEMP");
                               if (user != null) {
                                   myNewComment.setUserId(user.getId());
                                   myNewComment.setAvaterUrl(user.getAvatar());
                                   myNewComment.setUsername(user.getUsername());
                               }
                               myNewComment.setComment(comment);
                               myNewComment.setCreatedAt(new Date());
                               myNewComment.setUpdatedAt(new Date());
                               myNewComment.setGalleryId(gallery.getId());
                               myNewComment.save();
                               adapter.notifyDataSetChanged();
                           }
                           appBarLayout.setExpanded(false);
                           scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                               @Override
                               public void onGlobalLayout() {
                                   scrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                                   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                       scrollView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                   }
                                   else {
                                       scrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                   }
                               }
                           });
                           galleryManager.comment(gallery.getId(), comment)
                                         .delay(500, TimeUnit.MILLISECONDS)
                                         .observeOn(AndroidSchedulers.mainThread())
                                         .subscribeOn(Schedulers.io())
                                         .onErrorReturn(throwable -> {
                                             SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_posting_comment);
                                             if (myNewComment.exists()) {
                                                 myNewComment.delete();
                                                 adapter.notifyDataSetChanged();
                                             }
                                             return null;
                                         })
                                         .subscribe(networkComment -> {
                                             if (networkComment == null) {
                                                 SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_posting_comment);
                                                 myNewComment.delete();
                                             }
                                             else {
                                                 if (myNewComment.exists()) {
                                                     myNewComment.delete();
                                                 }
                                                 myNewComment.setCommentFrom(networkComment);
                                                 myNewComment.save();
                                                 galleryManager.saveCommentEntities(networkComment);

                                                 addCommentViewHandler();
                                                 commentEditText.get().setText("");
                                             }
                                             adapter.notifyItemChanged(adapter.getItemCount() - 1);
                                         });
                       });

        }
    }

    private void addCommentViewHandler() {
        gallery.setCommentCount(gallery.getCommentCount() + 1);
        gallery.save();
        notifyPropertyChanged(BR.commentCount);
    }

    public void setComments() {
        if (!hasBeenBound || gallery == null) {
            return;
        }

        commentsDataSource = new CommentViewModelDataSource(activity, galleryManager.getCommentsDataSource(gallery.getId()));
        adapter = new PagingRecyclerViewBindingAdapter<>(R.layout.item_comment, commentsDataSource);

        //LOAD FIRST TEN
        if (TextUtils.isEmpty(currentCommentId)) {
            galleryManager.downloadComments(gallery.getId(), PAGE_SIZE)
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribeOn(Schedulers.io())
                          .onErrorReturn(throwable -> null)
                          .subscribe(comments -> {
                              notifyProperitesOnFirstCommentLoad();
                              adapter.notifyDataSetChanged();
                          });
        }
        //LOAD FIRST TEN AROUND COMMENT AND THE COMMENT ITSELF
        else {
            //FIRST COMMENT
            galleryManager.downloadComment(gallery.getId(), currentCommentId)
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribeOn(Schedulers.io())
                          .onErrorReturn(throwable -> null)
                          .subscribe(comments -> {
                              notifyProperitesOnFirstCommentLoad();
                              adapter.notifyDataSetChanged();
                          });
            galleryManager.downloadNewerComments(gallery.getId(), PAGE_SIZE, currentCommentId)
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribeOn(Schedulers.io())
                          .onErrorReturn(throwable -> null)
                          .subscribe(comments -> {
                              notifyProperitesOnFirstCommentLoad();
                              adapter.notifyDataSetChanged();
                          });
        }

        //adapter.getNewPageObservable does not work in a NestedScrollView.
        //Inside the GalleryDetailActivity we set the listener for the NestedScrollView.
        //That is where we call the method to load more comments.
        loadNewerComments();

        if (commentsRecyclerView == null) {
            commentsRecyclerView = new BindableView<>();
        }

        commentsRecyclerView.get().setLayoutManager(new LinearLayoutManager(activity));
        commentsRecyclerView.get().setAdapter(adapter);

        if (!TextUtils.isEmpty(currentCommentId)) {
            // TODO find a way to not rely on postdelayed
            new Handler().postDelayed(() -> {
                appBarLayout.setExpanded(false);
                new Handler().postDelayed(() -> {
                    scrollView.smoothScrollTo(0, commentsRecyclerView.get().getTop());
                }, 400);
            }, 50);
        }

        notifyProperitesOnFirstCommentLoad();
    }

    public void expandCommentBottomSheet(String commentId) {
        this.commentToDeleteId = commentId;
        commentBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void setCommentToDeleteId(String commentId) {
        this.commentToDeleteId = commentId;
    }

    public void hideCommentBottomSheet() {
        commentBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    public boolean isCommentBottomSheetHidden() {
        return commentBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN;
    }

    public void loadNewerComments() {
        if (commentsDataSource == null || commentsDataSource.getItemCount() == 0 || loadingComments) {
            return;
        }
        loadingComments = true;
        CommentViewModel last = commentsDataSource.get(commentsDataSource.getItemCount() - 1);
        if (last != null) {
            if (last.getPosition() == gallery.getCommentCount()) {
                return;
            }
            galleryManager.downloadNewerComments(gallery.getId(), PAGE_SIZE, last.getId())
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribeOn(Schedulers.io())
                          .onErrorReturn(throwable -> {
                              loadingComments = false;
                              return null;
                          })
                          .subscribe(comments -> {
                              loadingComments = false;
                              notifyProperitesOnFirstCommentLoad();
                              adapter.notifyDataSetChanged();
                          });
        }
    }

    private void notifyProperitesOnFirstCommentLoad() {
        notifyPropertyChanged(BR.commentsRemainingCountString);
        notifyPropertyChanged(BR.commentsRemainingCount);
        notifyPropertyChanged(BR.showMoreButtonText);
        notifyPropertyChanged(BR.commentCount);
    }

    private View getRoot() {
        if (activity != null && activity.getDataBinding() != null && activity.getDataBinding().getRoot() != null) {
            return activity.getDataBinding().getRoot();
        }
        return null;
    }

    @Bindable
    public String getAddCommentButtonText() {
        if (gallery == null || getCommentCount() == 0) {
            return activity.getString(R.string.add_comment);
        }

        if (addCommentButtonText == null || addCommentButtonText.equals("")) {
            this.addCommentButtonText = activity.getString(R.string.num_comments, gallery.getCommentCount());
        }
        return addCommentButtonText;
    }

    public void setAddCommentButtonText(boolean commentsAreVisible) {
        if (gallery == null) {
            return;
        }

        if (addCommentButtonText == null) {
            this.addCommentButtonText = activity.getString(R.string.num_comments, gallery.getCommentCount());
        }

        String oldButtonText = addCommentButtonText;
        if (commentsAreVisible) {
            this.addCommentButtonText = activity.getString(R.string.add_comment);
        }
        else {
            this.addCommentButtonText = activity.getString(R.string.num_comments, gallery.getCommentCount());
        }
        if (!oldButtonText.equals(addCommentButtonText)) {
            notifyPropertyChanged(BR.addCommentButtonText);
        }
    }

    @Bindable
    public int getCommentCount() {
        if (gallery == null) {
            return 0;
        }
        return gallery.getCommentCount();
    }

    @Bindable
    public int getCommentsRemainingCount() {
        if (gallery == null || commentsDataSource == null || commentsDataSource.getItemCount() == 0) {
            return 0;
        }
        return commentsDataSource.get(0).getPosition() - 1;
    }

    @Bindable
    public String getCommentsRemainingCountString() {
        return activity.getString(R.string.num_previous_comments, getCommentsRemainingCount());
    }

    @Bindable
    public String getShowMoreButtonText() {
        if (getCommentsRemainingCount() > PAGE_SIZE) {
            return activity.getString(R.string.show_more);
        }
        return activity.getString(R.string.show_all);
    }

    @Bindable
    public boolean isShowOverlay() {
        return showOverlay;
    }

    public void setShowOverlay(boolean showOverlay) {
        this.showOverlay = showOverlay;
        notifyPropertyChanged(BR.showOverlay);
    }

    public Action1<View> loadMoreComments = view -> {
        Comment last = (commentsDataSource.get(0).getItem());

        galleryManager.downloadComments(gallery.getId(), PAGE_SIZE, last.getId())
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribeOn(Schedulers.io())
                      .onErrorReturn(throwable -> null)
                      .subscribe(comments -> {
                          notifyPropertyChanged(BR.commentsRemainingCount);
                          notifyPropertyChanged(BR.commentsRemainingCountString);
                          notifyPropertyChanged(BR.showMoreButtonText);
                          adapter.notifyDataSetChanged();
                      });

    };

    public Action1<View> addComment = view -> {
        addComment(null);
    };

    public Action1<View> deleteComment = view -> {
        hideCommentBottomSheet();

        Comment commentToDelete = galleryManager.getComment(commentToDeleteId);
        if (commentToDelete != null) {
            commentToDelete.delete();
            galleryManager.deleteComment(gallery.getId(), commentToDeleteId)
                          .onErrorReturn(throwable -> {
                              SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_deleting_comment);
                              Log.e(TAG, throwable.getMessage());
                              commentToDelete.save();
                              return null;
                          })
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribeOn(Schedulers.io())
                          .subscribe(networkSuccessResult -> {
                              if (networkSuccessResult == null) {
                                  SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_deleting_comment);
                                  commentToDelete.save();
                              }
                              else {
                                  deleteCommentViewHandler();
                              }
                              adapter.notifyDataSetChanged();
                          });
        }
        else {
            LogUtils.i(TAG, "comment was null");
        }
    };

    public Action1<View> dismissAutoComplete = view -> {
        cancelAutoComplete();
    };

    protected void updateAdapter() {
        adapter.notifyDataSetChanged();
    }
}
