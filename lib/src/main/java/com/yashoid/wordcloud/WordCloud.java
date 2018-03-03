package com.yashoid.wordcloud;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Yashar on 02/24/2018.
 */

public class WordCloud extends View {

    private static final int ROTATION_TRIES = 7;
    private static final float SPIRAL_PROGRESS_STEP = 0.003f;

    private SpiralProvider mSpiralProvider = SpiralProvider.DEFAULT_SPIRAL;
    private RotationProvider mRotationProvider = RotationProvider.DEFAULT;

    private WordAdapter mAdapter = null;
    private ArrayList<Word> mWords = null;

    private PointF mHPoint = new PointF();

    private boolean mIsAttached = false;

    public WordCloud(Context context) {
        super(context);
        initialize(context, null, 0, 0);
    }

    public WordCloud(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0, 0);
    }

    public WordCloud(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public WordCloud(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialize(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initialize(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {

    }

    public void setSpiralProvider(SpiralProvider spiralProvider) {
        mSpiralProvider = spiralProvider;

        requestLayout();
    }

    public void setRotationProvider(RotationProvider rotationProvider) {
        mRotationProvider = rotationProvider;

        requestLayout();
    }

    public void setAdapter(WordAdapter adapter) {
        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mAdapter = null;
        }

        mAdapter = adapter;

        if (mIsAttached) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }

        processWords();

        requestLayout();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mIsAttached = true;

        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mIsAttached = false;

        if (mAdapter != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {

        @Override
        public void onChanged() {
            processWords();
        }

    };

    private void processWords() {
        mWords = new ArrayList<>(mAdapter.getCount());

        for (int i = 0; i < mAdapter.getCount(); i++) {
            mWords.add(new Word(mAdapter.getText(i), mAdapter.getFont(i), mAdapter.getTextSize(i), mAdapter.getPadding(i), mAdapter.getTextColor(i)));
        }

        Collections.sort(mWords);

        placeWords();

        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (getWidth() > 0) {
            placeWords();
        }
    }

    private void placeWords() {
        int width = getWidth();
        int height = getHeight();

        if (width == 0 || height == 0 || mWords == null) {
            return;
        }

        float t = 0;

        for (int i = 0; i < mWords.size(); i++) {
            Word word = mWords.get(i);

            t = placeWord(word, i, 0);
        }
    }

    private float placeWord(Word word, int index, float t) {
        while (t < 1) {
            mSpiralProvider.getSpiralPoint(t, getWidth(), getHeight(), mHPoint);

            word.x = mHPoint.x;
            word.y = mHPoint.y;

            for (int i = 0; i < ROTATION_TRIES; i++) {
                word.rotation = mRotationProvider.getRotation(index, (float) index / mWords.size());

                word.isMeasured = false;

                boolean success = true;

                for (int wIndex = 0; wIndex < index; wIndex++) {
                    if (word.collides(mWords.get(wIndex))) {
                        success = false;
                        break;
                    }
                }

                if (success) {
                    return t;
                }
            }

            t += SPIRAL_PROGRESS_STEP;
        }

        return t;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mWords != null) {
            for (Word word: mWords) {
                word.draw(canvas);
            }
        }
    }

    private static class Word implements Comparable {

        private String text;

        private Paint paint;

        private float width;
        private float height;

        private float x;
        private float y;
        private float rotation;

        private boolean isMeasured = false;
        private PointF[] points = new PointF[4];

        private Matrix matrix = new Matrix();
        private float[] matPoints = new float[8];

        protected Word(String text, Typeface font, float textSize, float padding, int textColor) {
            this.text = text;

            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);

            paint.setColor(textColor);
            paint.setTextSize(textSize);

            if (font != null) {
                paint.setTypeface(font);
            }

            width = paint.measureText(text) + 2 * padding;
            height = paint.getFontMetrics().bottom - paint.getFontMetrics().top + 2 * padding;

            for (int i = 0; i < points.length; i++) {
                points[i] = new PointF();
            }
        }

        protected boolean collides(Word word) {
            if (!isMeasured) {
                measure();
                isMeasured = true;
            }

            if (!word.isMeasured) {
                word.measure();
                word.isMeasured = true;
            }

            return arePolygonsIntersecting(points, word.points);
        }

        private void measure() {
            points[0].set(-width/2, -height/2);
            points[1].set(-points[0].x, points[0].y);
            points[2].set(points[1].x, -points[1].y);
            points[3].set(-points[2].x, points[2].y);

            Matrix matrix = new Matrix();
            matrix.setRotate(rotation);

            for (int i = 0; i < 4; i++) {
                matPoints[i*2] = points[i].x;
                matPoints[i*2 + 1] = points[i].y;
            }

            matrix.mapPoints(matPoints);

            for (int i = 0; i < 4; i++) {
                points[i].x = x + matPoints[i*2];
                points[i].y = y + matPoints[i*2 + 1];
            }
        }

        protected void draw(Canvas canvas) {
            Paint.FontMetrics fm = paint.getFontMetrics();

            canvas.save();
            canvas.translate(x, y);
            canvas.rotate(rotation);
            canvas.drawText(text, 0, -(fm.ascent + fm.descent) / 2, paint);
//            canvas.drawRect(-width/2, -height/2, width/2, height/2, p);
            canvas.restore();
        }

        @Override
        public int compareTo(@NonNull Object o) {
            return (int) ((((Word) o).paint.getTextSize()) - paint.getTextSize());
        }

    }

    private static PointF[][] polygons = new PointF[2][];

    private static boolean arePolygonsIntersecting(PointF[] a, PointF[] b) {
        polygons[0] = a;
        polygons[1] = b;

        for (int i = 0; i < polygons.length; i++) {

            // for each polygon, look at each edge of the polygon, and determine if it separates
            // the two shapes
            PointF[] polygon = polygons[i];

            for (int i1 = 0; i1 < polygon.length; i1++) {

                // grab 2 vertices to create an edge
                int i2 = (i1 + 1) % polygon.length;
                PointF p1 = polygon[i1];
                PointF p2 = polygon[i2];

                // find the line perpendicular to this edge
                PointF normal = new PointF(p2.y - p1.y, p1.x - p2.x);

                float minA = Float.MAX_VALUE;
                float maxA = Float.MIN_VALUE;

                // for each vertex in the first shape, project it onto the line perpendicular to the edge
                // and keep track of the min and max of these values
                for (int j = 0; j < polygons[0].length; j++) {
                    float projected = normal.x * polygons[0][j].x + normal.y * polygons[0][j].y;
                    if (projected < minA) {
                        minA = projected;
                    }
                    if (projected > maxA) {
                        maxA = projected;
                    }
                }

                // for each vertex in the second shape, project it onto the line perpendicular to the edge
                // and keep track of the min and max of these values
                float minB = Float.MAX_VALUE;
                float maxB = Float.MIN_VALUE;

                for (int j = 0; j < polygons[1].length; j++) {
                    float projected = normal.x * polygons[1][j].x + normal.y * polygons[1][j].y;
                    if (projected < minB) {
                        minB = projected;
                    }
                    if (projected > maxB) {
                        maxB = projected;
                    }
                }

                // if there is no overlap between the projects, the edge we are looking at separates the two
                // polygons, and we know there is no overlap
                if (maxA < minB || maxB < minA) {
                    // polygons don't intersect!
                    return false;
                }
            }
        }
        return true;
    }

}
