package com.fresconews.fresco.framework.databinding.adapters;

import android.databinding.BindingAdapter;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.utils.DimensionUtils;
import com.fresconews.fresco.v2.utils.ImageUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

public class ImageViewAdapters {
    private static final String TAG = ImageViewAdapters.class.getSimpleName();
    private static final int MAX_WIDTH = 1080;

    @BindingAdapter({"image"})
    public static void bindImageView(SimpleDraweeView view, String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains("fresconews.com")) {
            view.setImageURI(ImageUtils.getImageSizeV2(imageUrl, Math.min(DimensionUtils.getScreenDimensions().x, MAX_WIDTH)));
        }
        else if (imageUrl.contains("cloudfront")) {
            view.setImageURI(ImageUtils.getImageSizeV1(imageUrl, ImageUtils.MEDIUM));
        }
        else {
            bindImageResize(view, imageUrl, 800, 600);
        }
    }

    @BindingAdapter({"imageMedium50"})
    public static void bindImageViewMedium50(SimpleDraweeView view, String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains("fresconews.com")) {
            view.setImageURI(ImageUtils.getImageSizeV2(imageUrl, Math.min(DimensionUtils.getScreenDimensions().x, MAX_WIDTH) / 2));
        }
        else if (imageUrl.contains("cloudfront")) {
            view.setImageURI(ImageUtils.getImageSizeV1(imageUrl, ImageUtils.MEDIUM));
        }
        else {
            bindImageResize(view, imageUrl, 600, 450);
        }
    }

    @BindingAdapter({"imageMedium33"})
    public static void bindImageViewMedium33(SimpleDraweeView view, String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains("fresconews.com")) {
            view.setImageURI(ImageUtils.getImageSizeV2(imageUrl, Math.min(DimensionUtils.getScreenDimensions().x, MAX_WIDTH) / 3));
        }
        else if (imageUrl.contains("cloudfront")) {
            view.setImageURI(ImageUtils.getImageSizeV1(imageUrl, ImageUtils.SMALL));
        }
        else {
            bindImageResize(view, imageUrl, 500, 375);
        }
    }

    @BindingAdapter({"imageMedium25"})
    public static void bindImageViewMedium25(SimpleDraweeView view, String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains("fresconews.com")) {
            view.setImageURI(ImageUtils.getImageSizeV2(imageUrl, Math.min(DimensionUtils.getScreenDimensions().x, MAX_WIDTH) / 4));
        }
        else if (imageUrl.contains("cloudfront")) {
            view.setImageURI(ImageUtils.getImageSizeV1(imageUrl, ImageUtils.SMALL));
        }
        else {
            bindImageResize(view, imageUrl, 400, 300);
        }
    }

    @BindingAdapter({"image"})
    public static void bindImageView(SimpleDraweeView view, Uri uri) {
        if (uri == null) {
            return;
        }
        view.setImageURI(uri);
    }

    @BindingAdapter({"image", "imageWidth", "imageHeight"})
    public static void bindImageView(SimpleDraweeView view, Uri uri, int width, int height) {
        if (uri == null) {
            return;
        }
        bindImageResize(view, uri.toString(), width, height);
    }

    @BindingAdapter({"image", "imageWidth", "imageHeight"})
    public static void bindImageView(SimpleDraweeView view, String uri, int width, int height) {
        if (uri == null) {
            return;
        }
        bindImageResize(view, uri, width, height);
    }

    @BindingAdapter({"image"})
    public static void bindImageView(ImageView view, Uri uri) {
        Glide.with(Fresco2.getContext())
             .load(uri)
             .error(ContextCompat.getDrawable(view.getContext(), R.drawable.account))
             .into(view);
    }

    @BindingAdapter({"image"})
    public static void bindImageView(ImageView view, String uri) {
        if (uri == null) {
            return;
        }

        Glide.with(Fresco2.getContext())
             .load(uri)
             .error(ContextCompat.getDrawable(view.getContext(), R.drawable.open_in_new))
             .into(view);
    }

    @BindingAdapter({"image"})
    public static void bindImageView(ImageView view, Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        view.setImageBitmap(bitmap);
    }

    @BindingAdapter({"thumbnail"})
    public static void bindThumbnail(SimpleDraweeView view, String imageUrl) {
        if (imageUrl == null) {
            return;
        }

        if (!TextUtils.isEmpty(imageUrl) && imageUrl.contains("fresconews.com")) {
            view.setImageURI(ImageUtils.getImageSizeV2(imageUrl, 200));
        }
        else if (imageUrl.contains("cloudfront")) {
            view.setImageURI(ImageUtils.getImageSizeV1(imageUrl, ImageUtils.SMALL));
        }
        else {
            bindImageResize(view, imageUrl, 200, 200);
        }

    }

    @BindingAdapter({"thumbnail"})
    public static void bindThumbnail(SimpleDraweeView view, Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        bindImageResize(view, imageUri.toString(), 200, 200);
    }

    @BindingAdapter({"avatar"})
    public static void bindAvatar(SimpleDraweeView view, String imageUrl) {
        if (imageUrl == null) {
            view.setImageURI(imageUrl);
            return;
        }

        bindImageResize(view, imageUrl, 200, 200);
    }

    private static void bindImageResize(SimpleDraweeView view, String imageUrl, int width, int height) {
        ImageRequest request;
        if (TextUtils.isEmpty(imageUrl)) {
            request = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.account)
                                         .setResizeOptions(new ResizeOptions(width, height))
                                         .build();
        }
        else {
            request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl))
                                         .setResizeOptions(new ResizeOptions(width, height))
                                         .build();
        }
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                                            .setOldController(view.getController())
                                            .setImageRequest(request)
                                            .build();
        view.setController(controller);
    }

    @BindingAdapter({"colorFilter"})
    public static void bindColorFilter(ImageView view, boolean selected) {
        if (selected) {
            view.setColorFilter(ContextCompat.getColor(view.getContext(), R.color.white_27), PorterDuff.Mode.SCREEN);
        }
        else {
            view.clearColorFilter();
        }
    }
}
