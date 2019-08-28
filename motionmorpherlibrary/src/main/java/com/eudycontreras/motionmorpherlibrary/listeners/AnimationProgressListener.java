package com.eudycontreras.motionmorpherlibrary.listeners;

import androidx.annotation.NonNull;

/**
 * @author Eudy Contreras.
 * @Project MotionMorpher
 * @since August 28 2019
 */

@FunctionalInterface
public interface AnimationProgressListener {
    void onProgress(@NonNull Float fraction);
}
