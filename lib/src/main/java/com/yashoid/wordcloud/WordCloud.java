package com.yashoid.wordcloud;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by Yashar on 02/24/2018.
 */

public class WordCloud<T> {

    private static final double cloudRadians = Math.PI / 180;
    private static final int cw = 1 << 11 >> 5;
    private static final int ch = 1 << 11;

    private static final Paint CLEAR_PAINT = new Paint();

    static {
        CLEAR_PAINT.setColor(0);
        CLEAR_PAINT.setStyle(Paint.Style.FILL);
    }

    public interface EventListener<T> {

        void onWordPlaced(T word);

        void onAllWordsPlaced();

    }

    private EventListener<T> mEventListener = null;

    private Point size = new Point(256, 256);
    private TextProvider<T> text = TextProvider.DEFAULT;
    private FontProvider<T> font = FontProvider.DEFAULT;
    private TextSizeProvider<T> fontSize = TextSizeProvider.DEFAULT;
    private RotationProvider<T> rotate = RotationProvider.DEFAULT;
    private PaddingProvider<T> padding = PaddingProvider.DEFAULT;
    private SpiralProvider spiral = SpiralProvider.ARCHIMEDEAN_SPIRAL;

    private T[] words = null;

    private long timeInterval = 0;
    private Handler timer = null;

    private Random random = new Random();

    private Canvas canvas = null;
    private Bitmap bitmap = null;


    private int[] board = new int[(size.x >> 5) * size.y];
    private RectF bounds = null;

    private int n;
    private int i;
    private ArrayList<ProcessedWord> processedWords;

    private ArrayList<ProcessedWord> tags;

    public void setEventListener(EventListener<T> listener) {
        mEventListener = listener;
    }

    public void setCanvas(Canvas canvas, Bitmap bitmap) {
        this.canvas = canvas;
        this.bitmap = bitmap;
    }

    public WordCloud<T> start() {
        board = new int[(size.x >> 5) * size.y];
        bounds = null;

        n = words.length;
        i = -1;

        tags = new ArrayList();
        processedWords = processWords(words);
        Collections.sort(processedWords);

        if (timer != null) {
            timer.removeCallbacks(step);
        }

        timer = new Handler();
        timer.post(step);

        step.run();

        return this;
    }

    public WordCloud<T> stop() {
        if (timer != null) {
            timer.removeCallbacks(step);
            timer = null;
        }

        return this;
    }

    private Runnable step = new Runnable() {

        @Override
        public void run() {
            long start = System.currentTimeMillis();

            while ((timeInterval == 0 || System.currentTimeMillis() - start < timeInterval) && ++i < n && timer != null) {
                ProcessedWord word = processedWords.get(i);
                word.x = (int) (size.x * (random.nextFloat() + .5)) >> 1;
                word.y = (int) (size.y * (random.nextFloat() + .5)) >> 1;
                cloudSprite(word, i);
                if (word.hasText() && place(word)) {
                    tags.add(word);

                    if (mEventListener != null) {
                        mEventListener.onWordPlaced(words[i]);
                    }

                    if (bounds != null) {
                        cloudBounds(word);
                    }
                    else {
                        bounds = new RectF(word.x + word.x0, word.y + word.y0, word.x + word.x1, word.y + word.y1);
                    }

                    // Temporary hack
                    word.x -= size.x >> 1;
                    word.y -= size.y >> 1;
                }
            }

            if (i >= n) {
                stop();

                if (mEventListener != null) {
                    mEventListener.onAllWordsPlaced();
                }
            }
            else if (timer != null) {
                timer.post(step);
            }
        }

    };

    private ArrayList<ProcessedWord> processWords(T[] words) {
        if (words == null) {
            return new ArrayList<>();
        }

        ArrayList<ProcessedWord> processedWords = new ArrayList(words.length);

        for (int i = 0; i < words.length; i++) {
            ProcessedWord pw = new ProcessedWord();

            pw.text = text.getText(words[i], i);
            pw.font = font.getFont(words[i], i);
            pw.rotate = rotate.getRotation(words[i], i);
            pw.size = fontSize.getTextSize(words[i], i);
            pw.padding = padding.getPadding(words[i], i);

            processedWords.add(pw);
        }

        return processedWords;
    }

    // Fetches a monochrome sprite bitmap for the specified text.
    // Load in batches for speed.
    private void cloudSprite(ProcessedWord d, int di) {
        if (d.sprite != null) {
            return;
        }

        Canvas c = canvas;
        float ratio = (float) c.getWidth() / c.getHeight();

        c.drawRect(0, 0, (cw << 5) / ratio, ch / ratio, CLEAR_PAINT);

        int x = 0;
        int y = 0;
        int maxh = 0;

        --di;

        while (++di < n) {
            c.save();

            d.paint.setTypeface(d.font);
            d.paint.setTextSize((d.size + 1) / ratio);

            int w = (int) (d.paint.measureText(d.text + "m") * ratio);
            int h = (int) d.size << 1;

            if (d.rotate != 0) {
                double sr = Math.sin(d.rotate * cloudRadians);
                double cr = Math.cos(d.rotate * cloudRadians);
                double wcr = w * cr;
                double wsr = w * sr;
                double hcr = h * cr;
                double hsr = h * sr;

                w = ((int) Math.max(Math.abs(wcr + hsr), Math.abs(wcr - hsr)) + 0x1f) >> 5 << 5;
                h = (int) Math.max(Math.abs(wsr + hcr), Math.abs(wsr - hcr));
            } else {
                w = (w + 0x1f) >> 5 << 5;
            }
            if (h > maxh) maxh = h;
            if (x + w >= (cw << 5)) {
                x = 0;
                y += maxh;
                maxh = 0;
            }
            if (y + h >= ch) break;
            c.translate((x + (w >> 1)) / ratio, (y + (h >> 1)) / ratio);
            if (d.rotate != 0) {
                c.rotate((float) (d.rotate * cloudRadians));
            }
            c.drawText(d.text, 0, 0, d.paint);

            if (d.padding != 0) {
                d.paint.setStyle(Paint.Style.STROKE);
                d.paint.setStrokeWidth(2 * d.padding);
                c.drawText(d.text, 0, 0, d.paint);
                d.paint.setStyle(Paint.Style.FILL);
            }
            c.restore();
            d.width = w;
            d.height = h;
            d.xoff = x;
            d.yoff = y;
            d.x1 = w >> 1;
            d.y1 = h >> 1;
            d.x0 = -d.x1;
            d.y0 = -d.y1;
            d.hasText = true;
            x += w;
        }

        int pixelsWidth = (int) ((cw << 5) / ratio);
        int pixelsHeight = (int) (ch / ratio);
        int[] pixels = new int[pixelsWidth * pixelsHeight];
        bitmap.getPixels(pixels, 0, 0, 0, 0, pixelsWidth, pixelsHeight);

        int w = (int) d.width;
        int w32 = w >> 5;
        int h = (int) (d.y1 - d.y0);

        int[] sprite = new int[h * w32];

        while (--di >= 0) {
            if (!d.hasText) continue;

            x = (int) d.xoff;
            //if (x == null) return;
            y = (int) d.yoff;
            int seen = 0;
            int seenRow = -1;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int k = w32 * j + (i >> 5);
                    int m = pixels[((y + j) * (cw << 5) + (x + i)) << 2] != 0 ? 1 << (31 - (i % 32)) : 0;
                    sprite[k] = sprite[k] | m;
                    seen |= m;
                }
                if (seen != 0) seenRow = j;
                else {
                    d.y0++;
                    h--;
                    j--;
                    y++;
                }
            }
            d.y1 = d.y0 + seenRow;

            d.sprite = Arrays.copyOfRange(sprite, 0, (int) (d.y1 - d.y0) * w32);
        }
    }

    private boolean place(ProcessedWord tag) {
        Rect perimeter = new Rect(0, 0, size.x, size.y);
        float startX = tag.x;
        float startY = tag.y;

        double maxDelta = PointF.length(size.x, size.y);

//        s = spiral(size),
        float dt = Math.random() < .5 ? 1 : -1;
        float t = -dt;
        PointF dxdy = new PointF();
        int dx;
        int dy;

        spiral.getSpiralPoint(t += dt, size, dxdy);

        while (dxdy != null) { // TODO WHAT?!
            dx = (int) dxdy.x;
            dy = (int) dxdy.y;

            if (Math.min(Math.abs(dx), Math.abs(dy)) >= maxDelta) break;

            tag.x = startX + dx;
            tag.y = startY + dy;

            if (tag.x + tag.x0 < 0 || tag.y + tag.y0 < 0 ||
              tag.x + tag.x1 > size.x || tag.y + tag.y1 > size.y) continue;
            // TODO only check for collisions within current bounds.
            if ((bounds == null || bounds.isEmpty()) || !cloudCollide(tag, board, size.x)) {
                if ((bounds == null || bounds.isEmpty()) || collideRects(tag, bounds)) {
                    int[] sprite = tag.sprite;
                    int w = (int) tag.width >> 5;
                    int sw = size.x >> 5;
                    int lx = (int) tag.x - (w << 4);
                    int sx = lx & 0x7f;
                    int msx = 32 - sx;
                    float h = tag.y1 - tag.y0;
                    int x = (int) (tag.y + tag.y0) * sw + (lx >> 5);
                    int last;
                    for (int j = 0; j < h; j++) {
                    last = 0;
                    for (int i = 0; i <= w; i++) {
                      board[x + i] |= (last << msx) | (i < w ? (last = sprite[j * w + i]) >>> sx : 0);
                    }
                    x += sw;
                    }
                    tag.sprite = null;
                    return true;
                }
            }

            spiral.getSpiralPoint(t += dt, size, dxdy);
        }
        return false;
    }

    // Use mask-based collision detection.
    private boolean cloudCollide(ProcessedWord tag, int[] board, int sw) {
        sw >>= 5;
        int[] sprite = tag.sprite;
        int w = (int) tag.width >> 5;
        int lx = (int) tag.x - (w << 4);
        int sx = lx & 0x7f;
        int msx = 32 - sx;
        float h = tag.y1 - tag.y0;
        int x = (int) (tag.y + tag.y0) * sw + (lx >> 5);
        int last;
        for (int j = 0; j < h; j++) {
            last = 0;
            for (int i = 0; i <= w; i++) {
                int aa = ((last << msx) | (i < w ? (last = sprite[j * w + i]) >>> sx : 0))
                        & board[x + i];
                if (aa != 0) return true;
            }
            x += sw;
        }
        return false;
    }

    private boolean collideRects(ProcessedWord a, RectF b) {
        return a.x + a.x1 > b.left && a.x + a.x0 < b.right && a.y + a.y1 > b.top && a.y + a.y0 < b.bottom;
    }

    private void cloudBounds(ProcessedWord d) {
        if (d.x + d.x0 < bounds.left) bounds.left = d.x + d.x0;
        if (d.y + d.y0 < bounds.top) bounds.top = d.y + d.y0;
        if (d.x + d.x1 > bounds.right) bounds.right = d.x + d.x1;
        if (d.y + d.y1 > bounds.bottom) bounds.bottom = d.y + d.y1;
    }

    public void setWords(T... words) {
        this.words = words;
    }

    private static class ProcessedWord implements Comparable {

        private String text;
        private Typeface font;
        private double rotate;
        private float size;
        private float padding;

        private int[] sprite = null;

        private Paint paint;

        private float x;
        private float y;

        private boolean hasText = false;

        private float width;
        private float height;

        private float xoff;
        private float yoff;

        private float x0;
        private float y0;

        private float x1;
        private float y1;

        protected ProcessedWord() {
            paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
        }

        protected boolean hasText() {
            return hasText;
        }

        @Override
        public int compareTo(@NonNull Object o) {
            return (int) ((((ProcessedWord) o).size) - size);
        }

    }

}
