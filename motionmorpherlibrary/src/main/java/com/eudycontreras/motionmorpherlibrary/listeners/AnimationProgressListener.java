package com.eudycontreras.motionmorpherlibrary.listeners;

import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author Eudy Contreras.
 * @Project MotionMorpher
 * @since August 28 2019
 */

@FunctionalInterface
public interface AnimationProgressListener {
    void onProgress(@NonNull Float fraction, @NonNull Long playTime);
}
