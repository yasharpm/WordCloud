package com.yashoid.wordcloud;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface RotationProvider {

    RotationProvider DEFAULT = new RotationProvider() {

        @Override
        public float getRotation(int position, float progress) {
            return (float) Math.floor(((Math.random() * 6) - 3) * 30 * (0.66f + progress / 3));
        }

    };

    float getRotation(int position, float progress);

}
