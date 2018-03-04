package com.yashoid.wordcloud.sample;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;

import com.yashoid.wordcloud.WordAdapter;

import java.util.Random;

/**
 * Created by Yashar on 03/03/2018.
 */

public class SampleWordAdapter extends WordAdapter {

    private static final String[] WORDS = { "Maya", "Yashar", "Benjamin", "Maithe", "Ben Albu", "Wouter", "Shyvana", "Daenerys Targaryen", "Jon Snow", "Tyrion Lannister", "Hodor", "Hodor", "Hodor", "Hodor", "Hodor", "Ramin", "Bahram", "Seyyed", "Amir hossein", "Arashk", "Amin", "Navid", "Ehsan", "Pedram" };

    private Random mRandom;

    private float[] hsv = new float[] { 0, 0.88f, 0.88f };

    public SampleWordAdapter() {
        mRandom = new Random();

        setMaximumTextSize(Resources.getSystem().getDisplayMetrics().density * 30);
        setMinimumTextSize(Resources.getSystem().getDisplayMetrics().density * 5);
    }

    @Override
    public int getCount() {
        return WORDS.length;
    }

    @Override
    public String getText(int position) {
        return WORDS[position];
    }

    @Override
    public float getImportance(int position) {
        float importance = super.getImportance(position);

        return importance * importance;
    }

    @Override
    public int getTextColor(int position) {
        hsv[0] = mRandom.nextFloat() * 360;

        return Color.HSVToColor(hsv);
    }

    @Override
    public Typeface getFont(int position) {
        if (getText(position).equalsIgnoreCase("hodor")) {
            return Typeface.defaultFromStyle(Typeface.BOLD);
        }

        return super.getFont(position);
    }

}
