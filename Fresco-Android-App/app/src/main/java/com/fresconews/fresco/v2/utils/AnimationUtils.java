package com.fresconews.fresco.v2.utils;

import android.animation.Animator;
import android.graphics.Point;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by wumau on 9/20/2016.
 */

public class AnimationUtils {
    public static void rotate(View view, float from, float to) {
        if (view != null) {
            final RotateAnimation rotateAnim = new RotateAnimation(from, to,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(40);
            rotateAnim.setFillAfter(true);
            view.startAnimation(rotateAnim);
        }
    }

    public static void fade(View view, boolean fadeIn) {
        fade(view, fadeIn, 200);
    }

    public static void fade(View view, boolean fadeIn, int duration) {
        if (view != null) {
            AlphaAnimation anim;
            if (fadeIn) {
                anim = new AlphaAnimation(view.getAlpha(), 1.0f);
            }
            else {
                anim = new AlphaAnimation(view.getAlpha(), 0.0f);
            }
            anim.setFillAfter(true);
            anim.setDuration(duration);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    if (fadeIn) {
                        view.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!fadeIn) {
                        view.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            view.startAnimation(anim);
        }
    }

    public static void circularReveal(View root, Point center, float radius, boolean reverse, int duration) {
        circularReveal(root, center, radius, reverse, duration, null);
    }

    public static void circularReveal(View root, Point center, float radius, boolean reverse, int duration, Animator.AnimatorListener listener) {
        Animator animator;
        if (reverse) {
            animator = ViewAnimationUtils.createCircularReveal(root, center.x, center.y, radius, 0);
        }
        else {
            animator = ViewAnimationUtils.createCircularReveal(root, center.x, center.y, 0, radius);
        }
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(duration);
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }
}
