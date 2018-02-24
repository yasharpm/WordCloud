package com.yashoid.wordcloud;

import android.graphics.Typeface;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface FontProvider<T> {

    FontProvider DEFAULT = new FontProvider() {

        @Override
        public Typeface getFont(Object word, int index) {
            return Typeface.DEFAULT;
        }

    };

    Typeface getFont(T word, int index);

}
