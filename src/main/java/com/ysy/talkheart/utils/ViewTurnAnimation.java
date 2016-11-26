package com.ysy.talkheart.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

/**
 * Created by 姚圣禹 on 2016/2/17.
 */

public class ViewTurnAnimation {

    // SATo: StartAnimationTo

    public ScaleAnimation sATo0 = new ScaleAnimation(1, 0, 1, 1, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
    public ScaleAnimation sATo1 = new ScaleAnimation(0, 1, 1, 1, Animation.RELATIVE_TO_PARENT, 0.5f, Animation.RELATIVE_TO_PARENT, 0.5f);
    private View layoutOne;
    private View layoutTwo;

    public ViewTurnAnimation(final View layoutOne, final View layoutTwo) {
        this.layoutOne = layoutOne;
        this.layoutTwo = layoutTwo;
        showWidgetOne();
        sATo0.setDuration(250);
        sATo1.setDuration(250);

        sATo0.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (layoutOne.getVisibility() == View.VISIBLE) {
                    layoutOne.setAnimation(null);
                    showWidgetTwo();
                    layoutTwo.startAnimation(sATo1);
                } else {
                    layoutTwo.setAnimation(null);
                    showWidgetOne();
                    layoutOne.startAnimation(sATo1);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public ScaleAnimation getSATo(int number) {
        if (number == 0) {
            return this.sATo0;
        } else {
            return this.sATo1;
        }
    }

    private void showWidgetOne() {
        layoutOne.setVisibility(View.VISIBLE);
        layoutTwo.setVisibility(View.GONE);
    }

    private void showWidgetTwo() {
        layoutOne.setVisibility(View.GONE);
        layoutTwo.setVisibility(View.VISIBLE);
    }

}
