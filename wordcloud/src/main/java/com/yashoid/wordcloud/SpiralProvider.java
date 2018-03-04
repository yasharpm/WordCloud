package com.yashoid.wordcloud;

import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by Yashar on 02/24/2018.
 */

public interface SpiralProvider {

    SpiralProvider DEFAULT_SPIRAL = new ArchimedeanSpiral();

    class ArchimedeanSpiral implements SpiralProvider {

        private int mSpiralTurns;

        public ArchimedeanSpiral(int turns) {
            mSpiralTurns = turns;
        }

        public ArchimedeanSpiral() {
            this(20);
        }

        @Override
        public void getSpiralPoint(float t, float width, float height, PointF dest) {
            // plot(x(t)=360*t*cos(t*2*pi), y(t)=240*t*sin(t*2*pi)),t=0..10

            dest.x = width / 2 + (float) (width / 2 * t * Math.cos(t * 2 * Math.PI * mSpiralTurns));
            dest.y = height / 2 + (float) (height / 2 * t * Math.sin(t * 2 * Math.PI * mSpiralTurns));
        }

    }

//    SpiralProvider RECTANGULAR_SPIRAL = new SpiralProvider() {
//
//        @Override
//        public void getSpiralPoint(float t, Point size, PointF dest) {
//            float dy = Resources.getSystem().getDisplayMetrics().density * 4;
//            float dx = dy * size.x / size.y;
//
//            float x = 0;
//            float y = 0;
//
//            int sign = t < 0 ? -1 : 1;
//
//            // See triangular numbers: T_n = n * (n + 1) / 2.
//            switch ((int) (Math.sqrt(1 + 4 * sign * t) - sign) & 3) {
//                case 0:  x += dx; break;
//                case 1:  y += dy; break;
//                case 2:  x -= dx; break;
//                default: y -= dy; break;
//            }
//
//            dest.set(x, y);
//        }
//
//    };

    void getSpiralPoint(float t, float width, float height, PointF dest);

}
