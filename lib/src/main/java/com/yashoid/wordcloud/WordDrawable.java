package com.yashoid.wordcloud;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Yashar on 02/26/2018.
 */

public class WordDrawable extends Drawable implements WordCloud.EventListener<String> {

    private static final String[] WORDS  = { "Yashar", "Bahram", "Amir", "Sheida", "Ramin", "Arashk", "Shirzad", "Seyyed" };

    private WordCloud<String> mWordCloud;

    private Bitmap mBitmap = null;

    public WordDrawable() {
        mWordCloud = new WordCloud<>();
        mWordCloud.setWords(WORDS);
        mWordCloud.setEventListener(this);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (bounds.isEmpty()) {
            return;
        }

        mWordCloud.stop();

        mBitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mBitmap);
        mWordCloud.setCanvas(canvas, mBitmap);

        mWordCloud.start();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();

        canvas.save();

        canvas.translate(bounds.left, bounds.top);
        canvas.drawBitmap(mBitmap, 0, 0, null);

        canvas.restore();
    }

    @Override
    public void setAlpha(int alpha) { }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) { }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public void onWordPlaced(String word) {
        invalidateSelf();
    }

    @Override
    public void onAllWordsPlaced() {
        invalidateSelf();
    }

}
