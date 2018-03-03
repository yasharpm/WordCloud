package com.yashoid.wordcloud;

import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.Typeface;

import java.util.ArrayList;

/**
 * Created by Yashar on 03/03/2018.
 */

public abstract class WordAdapter {

    private float mMinimumTextSize = Resources.getSystem().getDisplayMetrics().density * 10;
    private float mMaximumTextSize = Resources.getSystem().getDisplayMetrics().density * 30;

    private ArrayList<DataSetObserver> mObservers = new ArrayList<>();

    public abstract int getCount();

    public abstract String getText(int position);

    public Typeface getFont(int position) {
        return null;
    }

    public float getPadding(int position) {
        return 0;
    }

    public void setMinimumTextSize(float textSize) {
        mMinimumTextSize = textSize;

        notifyDataSetChanged();
    }

    public void setMaximumTextSize(float textSize) {
        mMaximumTextSize = textSize;

        notifyDataSetChanged();
    }

    public float getImportance(int position) {
        return 1 - (float) position / getCount();
    }

    public float getTextSize(int position) {
        return mMinimumTextSize + getImportance(position) * (mMaximumTextSize - mMinimumTextSize);
    }

    public int getTextColor(int position) {
        switch ((int) (Math.random() * 7)) {
            case 0:
                return Color.BLUE;
            case 1:
                return Color.CYAN;
            case 2:
                return Color.GRAY;
            case 3:
                return Color.GREEN;
            case 4:
                return Color.MAGENTA;
            case 5:
                return Color.RED;
            case 6:
                return Color.YELLOW;
        }

        return 0;
    }

    public void registerDataSetObserver(DataSetObserver observer) {
        mObservers.remove(observer);
        mObservers.add(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mObservers.remove(observer);
    }

    public void notifyDataSetChanged() {
        ArrayList<DataSetObserver> observers = new ArrayList<>(mObservers);

        for (DataSetObserver observer: observers) {
            observer.onChanged();
        }
    }

}
