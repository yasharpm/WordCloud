package com.yashoid.wordcloud;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface SpiralProvider {

    SpiralProvider ARCHIMEDEAN_SPIRAL = new SpiralProvider() {

        @Override
        public void getSpiralPoint(float t, Point size, PointF dest) {
            float e = (float) size.x / size.y;

            dest.x = (float) (e * (t *= 0.1f) * Math.cos(t));
            dest.y = (float) (t * Math.sin(t));
        }

    };

    SpiralProvider RECTANGULAR_SPIRAL = new SpiralProvider() {

        @Override
        public void getSpiralPoint(float t, Point size, PointF dest) {
            float dy = Resources.getSystem().getDisplayMetrics().density * 4;
            float dx = dy * size.x / size.y;

            float x = 0;
            float y = 0;

            int sign = t < 0 ? -1 : 1;

            // See triangular numbers: T_n = n * (n + 1) / 2.
            switch ((int) (Math.sqrt(1 + 4 * sign * t) - sign) & 3) {
                case 0:  x += dx; break;
                case 1:  y += dy; break;
                case 2:  x -= dx; break;
                default: y -= dy; break;
            }

            dest.set(x, y);
        }

    };

    void getSpiralPoint(float t, Point size, PointF dest);

}
