package com.yashoid.wordcloud.sample;

import com.yashoid.wordcloud.WordAdapter;

/**
 * Created by Yashar on 03/03/2018.
 */

public class SampleWordAdapter extends WordAdapter {

    private static final String[] WORDS = { "Yashar", "Ramin", "Bahram", "Seyyed", "Amir hossein", "Arashk", "Amin", "Navid", "Ehsan", "Pedram" };

    @Override
    public int getCount() {
        return WORDS.length;
    }

    @Override
    public String getText(int position) {
        return WORDS[position];
    }

}
