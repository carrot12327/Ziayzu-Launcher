package net.kdt.pojavlaunch.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class GridBackgroundView extends View {
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float offset;
    private int cellSizePx;
    private ValueAnimator animator;

    public GridBackgroundView(Context context) {
        super(context);
        init();
    }

    public GridBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GridBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(1));
        gridPaint.setColor(0x1A64C8FF); // rgba(100,200,255,0.1)
        cellSizePx = (int) dp(50);
        setWillNotDraw(false);
    }

    private float dp(float v) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // translate for diagonal motion
        canvas.translate(-offset, -offset);

        // draw vertical lines
        for (int x = -cellSizePx; x <= w + cellSizePx; x += cellSizePx) {
            canvas.drawLine(x, 0, x, h + cellSizePx * 2, gridPaint);
        }
        // draw horizontal lines
        for (int y = -cellSizePx; y <= h + cellSizePx; y += cellSizePx) {
            canvas.drawLine(0, y, w + cellSizePx * 2, y, gridPaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        start();
    }

    @Override
    protected void onDetachedFromWindow() {
        stop();
        super.onDetachedFromWindow();
    }

    public void start() {
        if (animator != null && animator.isRunning()) return;
        animator = ValueAnimator.ofFloat(0f, cellSizePx);
        animator.setDuration(20000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(a -> {
            offset = (float) a.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }
    }
}