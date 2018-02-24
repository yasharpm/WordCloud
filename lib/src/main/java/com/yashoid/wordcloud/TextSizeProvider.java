package com.yashoid.wordcloud;

import android.content.res.Resources;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface TextSizeProvider<T> {

    TextSizeProvider DEFAULT = new TextSizeProvider() {

        @Override
        public float getTextSize(Object word, int index) {
            return Resources.getSystem().getDisplayMetrics().density * 16;
        }

    };

    float getTextSize(T word, int index);

}
