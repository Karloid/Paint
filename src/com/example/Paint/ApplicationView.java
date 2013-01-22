package com.example.Paint;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.os.Environment;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;

public class ApplicationView extends View {
    Paint paint = new Paint();
    static float density;
    Canvas canvas;
    Bitmap image;
    float zoom = 500;
    Point position = new Point(50, 50);
    ArrayList<Finger> fingers = new ArrayList<Finger>();
    Handler handler = new Handler();
    public boolean drawingMode;
    Runnable longPress = new Runnable() {
        @Override
        public void run() {
            if (fingers.size() > 0 && fingers.get(0).enabledLongTouch) {
                fingers.get(0).enabledLongTouch = false;
                drawingMode = !drawingMode;
            }
        }
    };
    public Point cursor;
    private long lastTapTime;
    private Point lastTapPosition;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int id = event.getPointerId(event.getActionIndex());
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
            fingers.add(event.getActionIndex(), new Finger(id, (int) event.getX(), (int) event.getY()));
            // handler.postDelayed(longPress, 1000);
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            int actionIndexRemove = event.getActionIndex();
            Finger finger = fingers.get(event.getActionIndex());
            if (System.currentTimeMillis() - finger.wasDown < 100 && finger.wasDown - lastTapTime < 200 &&
                    finger.wasDown - lastTapTime > 0 && checkDistance(finger.now, lastTapPosition) < density * 25) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                String[] items = {"Рисовать", "Перемещать", "Красный", "Зелёный", "Синий", "Голубой", "Чёрный", "Белый", "Жёлтый", "Розовый", "Сохранить картинку"};
                final AlertDialog dialog = builder.setTitle("Выберите цвет кисти").setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        int[] colors = {Color.RED, Color.GREEN, Color.BLUE, 0xFF99CCFF, Color.BLACK, Color.WHITE,
                                Color.YELLOW, 0xFFFFCC99};
                        if (which == 0) {
                            drawingMode = true;
                        } else if (which == 1) {
                            drawingMode = false;
                        } else if (which == 10) {
                            try {
                                /*
                                MediaStore.Images.Media
                                    .insertImage(context.getContentResolver(), bitmap, null, image.getSignature())
                                */
                                File dir = new File("sdcard/paint");
                                dir.mkdir();
                                FileOutputStream out = new FileOutputStream(dir + "/" +
                                        +Calendar.getInstance().getTimeInMillis() + ".png");
                                image.compress(Bitmap.CompressFormat.PNG, 90, out);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else

                        {
                            paint.setColor(colors[which - 2]);
                        }
                    }
                }).create();
                dialog.show();
            }
            lastTapPosition = finger.now;
            lastTapTime = System.currentTimeMillis();
            fingers.remove(fingers.get(actionIndexRemove));

        } else if (action == MotionEvent.ACTION_MOVE) {
            for (int n = 0; n < fingers.size(); n++) {
                fingers.get(n).setNow((int) event.getX(n), (int) event.getY(n));
            }
            checkGestures();
            invalidate();
        }
        return true;
    }

    private void checkGestures() {

        if (fingers.size() == 0)
            return;
        Finger finger = fingers.get(0);
        System.out.print("fingers.size() : = " + fingers.size());
        if (fingers.size() > 1) {
            float now = checkDistance(finger.now, fingers.get(1).now);
            float before = checkDistance(finger.before, fingers.get(1).before);
            if (!drawingMode) {
                float oldSize = zoom;                // Запоминаем старый размер картинки
                zoom = Math.max(now - before + zoom, density * 25);    // Изменяем расстояние до холста
                position.x -= (zoom - oldSize) / 2;            // Изменяем положение картинки
                position.y -= (zoom - oldSize) / 2;
            } else {
                paint.setStrokeWidth(paint.getStrokeWidth() + (now - before) / 8);
            }
        } else {
            if (!drawingMode) {
                position.x += finger.now.x - finger.before.x;
                position.y += finger.now.y - finger.before.y;
            } else {
                float x1 = (finger.before.x - position.x) * 500 / zoom;
                float x2 = (finger.now.x - position.x) * 500 / zoom;
                float y1 = (finger.before.y - position.y) * 500 / zoom;
                float y2 = (finger.now.y - position.y) * 500 / zoom;
                canvas.drawLine(x1, y1, x2, y2, paint);
                canvas.drawCircle(x1, y1, paint.getStrokeWidth() / 2, paint);
                canvas.drawCircle(x2, y2, paint.getStrokeWidth() / 2, paint);
                cursor = finger.now;

            }
        }
    }

    public static float checkDistance(Point p1, Point p2) {
        return FloatMath.sqrt((p1.x - p2.x) * (p1.x - p2.x) + (p1.y - p2.y) * (p1.y - p2.y));  //To change body of created methods use File | Settings | File Templates.
    }


    public ApplicationView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        this.setBackgroundColor(Color.GRAY);
        paint.setStrokeWidth(5 * density);
        image = Bitmap.createBitmap(1200, 500, Bitmap.Config.ARGB_4444);
        canvas = new Canvas(image);
        canvas.drawColor(Color.WHITE);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(position.x, position.y);
        canvas.scale(zoom / 500, zoom / 500);
        canvas.drawBitmap(image, 0, 0, paint);
        canvas.restore();
        if (drawingMode) {
            canvas.drawCircle(100, 100, paint.getStrokeWidth() / 2, paint);
        }
        /*
        for (int n = 0; n < fingers.size(); n++) {
            canvas.drawCircle(fingers.get(n).now.x, fingers.get(n).now.y, paint.getStrokeWidth() / 2, paint);
        } */
    }
}
