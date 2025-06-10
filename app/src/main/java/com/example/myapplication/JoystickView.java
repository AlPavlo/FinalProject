package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private Paint circlePaint;
    private Paint handlePaint;
    private float centerX, centerY;
    private float handleX, handleY;
    private float radius;
    private float handleRadius;
    private JoystickListener listener;

    public interface JoystickListener {
        void onJoystickMoved(float xPercent, float yPercent);
    }

    public JoystickView(Context context) {
        super(context);
        init();
    }

    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setColor(Color.GRAY);
        circlePaint.setAlpha(100);
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        handlePaint = new Paint();
        handlePaint.setColor(Color.BLUE);
        handlePaint.setAlpha(150);
        handlePaint.setStyle(Paint.Style.FILL);

        handleRadius = 80;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        handleX = centerX;
        handleY = centerY;
        radius = Math.min(w, h) / 3f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Рисуем основу джойстика
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
        // Рисуем ручку джойстика
        canvas.drawCircle(handleX, handleY, handleRadius, handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // Вычисляем расстояние от центра
                float dx = touchX - centerX;
                float dy = touchY - centerY;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // Если касание внутри круга
                if (distance <= radius) {
                    handleX = touchX;
                    handleY = touchY;
                } else {
                    // Если за пределами, ограничиваем ручку границей
                    float ratio = radius / distance;
                    handleX = centerX + dx * ratio;
                    handleY = centerY + dy * ratio;
                }

                // Нормализуем значения от -1 до 1
                float xPercent = (handleX - centerX) / radius;
                float yPercent = (handleY - centerY) / radius;

                if (listener != null) {
                    listener.onJoystickMoved(xPercent, yPercent);
                }
                break;
            case MotionEvent.ACTION_UP:
                // Возвращаем ручку в центр
                handleX = centerX;
                handleY = centerY;
                if (listener != null) {
                    listener.onJoystickMoved(0, 0);
                }
                break;
        }
        invalidate(); // Перерисовываем
        return true;
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }
}
