package com.example.Paint;

import android.graphics.Point;

/**
 * Created with IntelliJ IDEA.
 * User: krld
 * Date: 19.12.12
 * Time: 23:35
 * To change this template use File | Settings | File Templates.
 */
public class Finger {
    public int ID;
    public Point now;
    public Point before;
    boolean enabled = false;
    public long wasDown;
    public Point startPoint;
    public boolean enabledLongTouch = true;


    public Finger(int id, int x, int y) {
        ID = id;
        now = before = startPoint = new Point(x, y);
        wasDown = System.currentTimeMillis();
    }

    public void setNow(int x, int y) {
        if (!enabled) {
            enabled = true;
            now = before = startPoint = new Point(x, y);
        } else {
            before = now;
            now = new Point(x, y);
            if (ApplicationView.checkDistance(now, startPoint) > ApplicationView.density * 25) {
                enabledLongTouch = false;
            }
        }
    }
}
