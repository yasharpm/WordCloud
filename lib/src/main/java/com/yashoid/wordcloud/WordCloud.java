package com.yashoid.wordcloud;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.SparseIntArray;

import java.util.ArrayList;
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

                    // TODO event.call("word", cloud, d);

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

                // TODO event.call("end", cloud, tags, bounds);
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

        SparseIntArray sprite = new SparseIntArray();

        while (--di >= 0) {
            if (!d.hasText) continue;

            int w = (int) d.width;
            int w32 = w >> 5;
            int h = (int) (d.y1 - d.y0);
            // Zero the buffer
            for (int i = 0; i < h * w32; i++) sprite.put(i, 0);
            x = (int) d.xoff;
            //if (x == null) return;
            y = (int) d.yoff;
            int seen = 0;
            int seenRow = -1;
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    int k = w32 * j + (i >> 5);
                    int m = pixels[((y + j) * (cw << 5) + (x + i)) << 2] != 0 ? 1 << (31 - (i % 32)) : 0;
                    sprite.put(k, sprite.get(k) | m);
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

            d.sprite = sprite.slice(0, (d.y1 - d.y0) * w32);
        }
    }

    private boolean place(ProcessedWord tag) {
        return true;
        /*
          function place(board, tag, bounds) {
    var perimeter = [{x: 0, y: 0}, {x: size[0], y: size[1]}],
        startX = tag.x,
        startY = tag.y,
        maxDelta = Math.sqrt(size[0] * size[0] + size[1] * size[1]),
        s = spiral(size),
        dt = random() < .5 ? 1 : -1,
        t = -dt,
        dxdy,
        dx,
        dy;

    while (dxdy = s(t += dt)) {
      dx = ~~dxdy[0];
      dy = ~~dxdy[1];

      if (Math.min(Math.abs(dx), Math.abs(dy)) >= maxDelta) break;

      tag.x = startX + dx;
      tag.y = startY + dy;

      if (tag.x + tag.x0 < 0 || tag.y + tag.y0 < 0 ||
          tag.x + tag.x1 > size[0] || tag.y + tag.y1 > size[1]) continue;
      // TODO only check for collisions within current bounds.
      if (!bounds || !cloudCollide(tag, board, size[0])) {
        if (!bounds || collideRects(tag, bounds)) {
          var sprite = tag.sprite,
              w = tag.width >> 5,
              sw = size[0] >> 5,
              lx = tag.x - (w << 4),
              sx = lx & 0x7f,
              msx = 32 - sx,
              h = tag.y1 - tag.y0,
              x = (tag.y + tag.y0) * sw + (lx >> 5),
              last;
          for (var j = 0; j < h; j++) {
            last = 0;
            for (var i = 0; i <= w; i++) {
              board[x + i] |= (last << msx) | (i < w ? (last = sprite[j * w + i]) >>> sx : 0);
            }
            x += sw;
          }
          delete tag.sprite;
          return true;
        }
      }
    }
    return false;
  }
         */
    }

    private void cloudBounds(ProcessedWord d) {
        /*
        function cloudBounds(bounds, d) {
  var b0 = bounds[0],
      b1 = bounds[1];
  if (d.x + d.x0 < b0.x) b0.x = d.x + d.x0;
  if (d.y + d.y0 < b0.y) b0.y = d.y + d.y0;
  if (d.x + d.x1 > b1.x) b1.x = d.x + d.x1;
  if (d.y + d.y1 > b1.y) b1.y = d.y + d.y1;
}
         */
    }

    private static class ProcessedWord implements Comparable {

        private String text;
        private Typeface font;
        private double rotate;
        private float size;
        private float padding;

        private SparseIntArray sprite = null;

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
