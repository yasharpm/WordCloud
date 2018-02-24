package com.yashoid.wordcloud;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface RotationProvider<T> {

    RotationProvider DEFAULT = new RotationProvider() {

        @Override
        public double getRotation(Object word, int index) {
            return Math.floor(((Math.random() * 6) - 3) * 30);
        }

    };

    double getRotation(T word, int index);

}
