package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.Point;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingAdapters.ActionBindingAdapters;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableBoolean;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.Purchase;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.ResizeUtils;
import com.fresconews.fresco.v2.utils.StringUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import rx.functions.Action1;

public class PostViewModel extends ItemViewModel<Post> {

    public BindableView<LinearLayout> outletNamesLayout = new BindableView<>();
    public BindableView<SimpleDraweeView> postImageView = new BindableView<>();
    public BindableBoolean showTwitterPost = new BindableBoolean();

    private String externalSource = null;
    private Activity activity;

    public PostViewModel(Activity activity, Post item) {
        super(item);

        this.activity = activity;
        showTwitterPost.set(false);
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        // Displays the view for the purchased content in the gallery detail
        if (outletNamesLayout.get() != null && getItem() != null && postImageView.get() != null) {
            Point size = ResizeUtils.calculateAndSetViewPagerHeight(Collections.singletonList(getItem()));
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) postImageView.get().getLayoutParams();
            params.width = size.x;
            params.height = size.y;
            postImageView.get().setLayoutParams(params);

            List<Purchase> purchases = getItem().loadPurchases();
            // Dynamically add new rows for each outlet
            for (Purchase purchase : purchases) {
                View view = View.inflate(activity, R.layout.view_purchase_outlet_amount, null);
                TextView outletNameTextView = (TextView) view.findViewById(R.id.outlet_name_text_view);
                TextView amountTextView = (TextView) view.findViewById(R.id.amount_text_view);
                outletNameTextView.setText(purchase.getOutlet().getTitle());
                amountTextView.setText(activity.getString(R.string.dollar_amount, (double) purchase.getAmount() / 100.00));
                ActionBindingAdapters.customFont(outletNameTextView, activity.getString(R.string.robotoMedium));
                ActionBindingAdapters.customFont(amountTextView, activity.getString(R.string.robotoRegular));
                outletNamesLayout.get().addView(view);
            }
            postImageView.get().requestLayout();
            outletNamesLayout.get().requestLayout();
        }
    }

    @Bindable
    public String getId() {
        return getItem().getId();
    }

    @Bindable
    public String getImage() {
        return getItem().getImage();
    }

    @Bindable
    public String getStream() {
        return getItem().getStream();
    }

    @Bindable
    public String getTime() {
        Date date = getItem().getCapturedAt() == null ? getItem().getCreatedAt() : getItem().getCapturedAt();
        DateTime dateTime = new DateTime(date);
        DateTimeFormatter fmt = DateTimeFormat.shortDateTime();
        return fmt.print(dateTime);
    }

    @Bindable
    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }

    @Bindable
    public String getOwnerName() {
        User owner = getItem().getOwner().load();
        if (owner == null) {
            //Try to pull off external account from PostParent
            String externalName = getItem().getExternal_account_name();
            String externalUsername = getItem().getExternal_account_username();
            if (externalName != null && !externalName.equals("")) {
                return externalName;
            }
            else if (externalUsername != null) {
                return "@" + externalUsername;
            }
            return "";
        }
        if (StringUtils.toNullIfEmpty(owner.getFullName()) == null) {
            return "@" + owner.getUsername();
        }
        return owner.getFullName();
    }

    @Bindable
    public String getOwnerAvatar() {
        User owner = getItem().getOwner().load();
        if (owner == null) {
            //Check for social
            String source = getItem().getExternal_source();
            if (source != null && !source.equals("") && source.equals("twitter")) {
                showTwitterPost.set(true);
                return null;

//                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
//                AccountService accountService = twitterApiClient.getAccountService();
//                StatusesService statusesService = twitterApiClient.getStatusesService();
//                SearchService searchService = twitterApiClient.getSearchService();

//                Uri uri = Uri.parse("android.resource://com.fresconews.fresco/" + R.drawable.twitter);
//                Log.i("PostViewModel", "show me the money taco! - " + uri.toString());
//                return uri.toString();
//                return "https://upload.wikimedia.org/wikipedia/en/thumb/9/9f/Twitter_bird_logo_2012.svg/300px-Twitter_bird_logo_2012.svg.png";
//                return "res:///" + Integer.toString(R.drawable.twitter_logo_bird_transparent_400);
//                return "res:///" + Integer.toString(R.color.white);
            }

            return "";
        }
        return owner.getAvatar();
    }

    public Action1<View> viewProfile = view -> {
        User owner = getItem().getOwner().load();
        if (owner != null) {
            if (activity != null && activity instanceof ProfileActivity) {
                ProfileActivity profileActivity = (ProfileActivity) activity;
                if (!owner.getId().equals(profileActivity.getUserId())) {
                    ProfileActivity.start(view.getContext(), owner.getId(), false, true, false);
                }
            }
            else {
                ProfileActivity.start(view.getContext(), owner.getId(), false, true, false);
            }
        }
        else {
            //Try to load the external profile intent.
            String source = getItem().getExternal_source();
            String url = getItem().getExternal_url();
            if (source != null && !source.equals("")) {
                if (source.equals("twitter") && url != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    browserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Fresco2.getContext().startActivity(browserIntent);
                }
            }
        }
    };

    @Bindable
    public String getAddress() {
        return getItem().getAddress();
    }
}
