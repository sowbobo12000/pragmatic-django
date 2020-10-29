package com.fresconews.fresco.v2.utils;

import android.graphics.Point;

import com.fresconews.fresco.framework.persistence.models.Post;

import java.util.List;

/**
 * Created by mauricewu on 11/11/16.
 */
public class ResizeUtils {

    public static Point calculateAndSetViewPagerHeight(List<Post> posts) {
        if (posts == null) {
            return null;
        }

        //Get mean height of posts
        double totalAspectRatio = 0;
        for (Post post : posts) {
            if (post.getHeight() != 0) {
                totalAspectRatio += (double) post.getWidth() / (double) post.getHeight();
            }
        }
        double meanAspectRatio = totalAspectRatio / posts.size();

        //Now correct the mean height in pixels to fit the aspect ratio size of the screen.
        Point size = DimensionUtils.getScreenDimensions();
        int screenWidth = size.x; // width of screen in pixels
        double screenHeight4 = (double) screenWidth * (4.0 / 3.0);  //Find the 3/4 value of screen in pixels

        int meanHeightPixels = (int) (screenWidth / meanAspectRatio);
        //Check to make sure the meanAspectRatio isn't too tall
        if (meanAspectRatio < 3.0 / 4.0) {
            meanHeightPixels = (int) screenHeight4; //if it is, set the height to the 3/4 height
        }

        return new Point(screenWidth, meanHeightPixels);
    }
}
