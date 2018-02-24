package com.yashoid.wordcloud;

import android.content.res.Resources;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface PaddingProvider<T> {

    PaddingProvider DEFAULT = new PaddingProvider() {

        @Override
        public float getPadding(Object word, int index) {
            return Resources.getSystem().getDisplayMetrics().density;
        }

    };

    float getPadding(T word, int index);

}
