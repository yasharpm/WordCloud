package com.yashoid.wordcloud;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface TextProvider<T> {

    TextProvider DEFAULT = new TextProvider() {

        @Override
        public String getText(Object word, int index) {
            return word == null ? null : word.toString();
        }

    };

    String getText(T word, int index);

}
