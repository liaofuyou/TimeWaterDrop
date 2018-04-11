package me.ajax.timewaterdrop.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import static me.ajax.timewaterdrop.utils.GeometryUtils.computeBezierPath;
import static me.ajax.timewaterdrop.utils.GeometryUtils.polarX;
import static me.ajax.timewaterdrop.utils.GeometryUtils.polarY;


/**
 * Created by aj on 2018/4/2
 */

public class TimeWaterDropView extends View {

    Path[] wavePath = new Path[3];

    //粘性效果路径
    Path stickPath = new Path();

    //随机值
    Random random = new Random();

    //大圆半径
    float bigCircleRadius = dp2Dx(100);

    //大圆画笔
    Paint bigCirclePaint = new Paint();
    //文字画笔
    Paint textPaint = new Paint();
    //波浪画笔
    Paint[] wavePaint = new Paint[3];

    //时间文本边界
    Rect timeTextBounds = new Rect();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
    Date date = new Date();

    //水滴
    WaterDrop waterDrop = new WaterDrop(dp2Dx(15));

    //动画的值
    int waterDropAnimationValue = 0;
    int waveAnimationValue1 = dp2Dx(10);
    int waveAnimationValue2 = dp2Dx(10);
    int waveAnimationValue3 = dp2Dx(10);

    //初始偏移位置
    float waterDropInitX;
    float waterDropInitY;
    float waterDropOffsetRightX;
    float waterDropOffsetRightY;
    float waterDropOffsetLeftX;
    float waterDropOffsetLeftY;
    //初始角度
    float dropInitDegree = 90;

    ValueAnimator waterDropAnimator;
    ValueAnimator waveAnimator1;
    ValueAnimator waveAnimator2;
    ValueAnimator waveAnimator3;


    public TimeWaterDropView(Context context) {
        super(context);
        init();
    }

    public TimeWaterDropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TimeWaterDropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {


        //波浪路径
        for (int i = 0; i < wavePath.length; i++) {
            wavePath[i] = new Path();
        }

        //画笔
        bigCirclePaint.setShader(new LinearGradient(
                -bigCircleRadius, 0, bigCircleRadius, 0,
                0xFF52E5C3, 0xFF44E0F7, Shader.TileMode.CLAMP));

        wavePaint[0] = new Paint();
        wavePaint[0].setColor(0x4452E5C3);
        wavePaint[1] = new Paint();
        wavePaint[1].setColor(0x6644E0F7);
        wavePaint[2] = new Paint();

        textPaint.setTextSize(dp2Dx(65));
        textPaint.setColor(Color.WHITE);

        //初始化动画
        initAnimator();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waterDropAnimator != null) {
                    waterDropAnimator.cancel();
                    waterDropAnimator.start();
                }
                if (waveAnimator1 != null) {
                    waveAnimator1.cancel();
                    waveAnimator1.start();
                }
                if (waveAnimator2 != null) {
                    waveAnimator2.cancel();
                    waveAnimator2.start();
                }
                if (waveAnimator3 != null) {
                    waveAnimator3.cancel();
                    waveAnimator3.start();
                }
            }
        });
        post(new Runnable() {
            @Override
            public void run() {
                performClick();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int mWidth = getWidth();
        int mHeight = getHeight();

        canvas.save();
        canvas.translate(mWidth / 2, mHeight / 4);

        //水滴
        drawWaterDrop(canvas);

        //大圆
        canvas.drawCircle(0, 0, bigCircleRadius, bigCirclePaint);

        //时间文本
        drawTimeText(canvas);

        canvas.restore();

        //波浪
        drawWave(canvas);
    }


    protected void drawWave(Canvas canvas) {

        int mWidth = getWidth();
        int mHeight = getHeight();
        canvas.save();

        float heightOffset = mHeight - mHeight / 7;
        canvas.translate(0, heightOffset);

        for (Path path : wavePath) {
            path.reset();
        }
        for (int i = 0; i < 25; i++) {

            float x = (float) Math.toRadians(30 * i);

            wavePath[0].lineTo(x * dp2Dx(30), (float) (Math.sin(x) * waveAnimationValue1) - dp2Dx(20));
            wavePath[1].lineTo(x * dp2Dx(40), (float) (Math.sin(x) * -waveAnimationValue2) - dp2Dx(12));
            wavePath[2].lineTo(x * dp2Dx(35), (float) (Math.cos(x) * waveAnimationValue3) - dp2Dx(10));
        }

        if (wavePaint[2].getShader() == null) {
            wavePaint[2].setShader(new LinearGradient(
                    0, heightOffset, mWidth, heightOffset,
                    0xFF52E5C3, 0xFF44E0F7, Shader.TileMode.CLAMP));
        }

        for (int i = 0; i < wavePath.length; i++) {
            Path path = wavePath[i];
            path.lineTo(mWidth, mHeight);
            path.lineTo(0, mHeight);
            path.close();
            canvas.drawPath(path, wavePaint[i]);
        }

        canvas.restore();
    }

    private void drawTimeText(Canvas canvas) {
        date.setTime(System.currentTimeMillis());
        String mText = simpleDateFormat.format(date);
        textPaint.getTextBounds(mText, 0, mText.length(), timeTextBounds);
        canvas.drawText(mText, -timeTextBounds.width() / 2f, timeTextBounds.height() / 2f, textPaint);
    }

    private void drawWaterDrop(Canvas canvas) {

        float waterDropBezierFraction = waterDropAnimationValue / (float) dp2Dx(120);

        //水滴偏移位置
        waterDropInitX = polarX(bigCircleRadius, dropInitDegree);
        waterDropInitY = polarY(bigCircleRadius, dropInitDegree);

        float leftOffsetDegree = 20;
        float rightOffsetDegree = 20;
        waterDropOffsetLeftX = polarX(bigCircleRadius, dropInitDegree + leftOffsetDegree);
        waterDropOffsetLeftY = polarY(bigCircleRadius, dropInitDegree + leftOffsetDegree);
        waterDropOffsetRightX = polarX(bigCircleRadius, dropInitDegree - rightOffsetDegree);
        waterDropOffsetRightY = polarY(bigCircleRadius, dropInitDegree - rightOffsetDegree);

        //粘性贝塞尔
        if (waterDropBezierFraction < 0.5) {//下拉

            //绘制水滴
            waterDrop.draw(canvas, bigCirclePaint, waterDropInitX, waterDropInitY + waterDropAnimationValue, waterDropBezierFraction * 2);

            computeBezierPath(stickPath
                    , waterDropOffsetLeftX, waterDropOffsetLeftY
                    , waterDropOffsetRightX, waterDropOffsetRightY
                    , waterDrop.getRightTanPoint()[0], waterDrop.getRightTanPoint()[1]
                    , waterDrop.getLeftTanPoint()[0], waterDrop.getLeftTanPoint()[1]);
            canvas.drawPath(stickPath, bigCirclePaint);
        } else if (waterDropBezierFraction < 1) {//回收

            //绘制水滴
            waterDrop.draw(canvas, bigCirclePaint, waterDropInitX, waterDropInitY + waterDropAnimationValue * 2.5F, waterDropBezierFraction * 2);

            float fraction = (waterDropBezierFraction - 0.5F) * 2;
            float radius = dp2Dx(1) + dp2Dx(4) * (fraction < 0.5 ? fraction : 0.5F);
            float totalDistance = (waterDrop.getLeftTanPoint()[1] - waterDropInitY) + radius;

            //绘制临时圆
            float cx = waterDrop.getLastTanPoint()[0];
            float cy = waterDrop.getLastTanPoint()[1] - totalDistance * fraction;
            canvas.drawCircle(cx, cy, radius, bigCirclePaint);

            //绘制贝塞尔
            float tan3X = waterDropInitX + polarX(radius, 0);
            float tan3Y = cy + polarY(radius, 0);
            float tan4X = waterDropInitX + polarX(radius, 180);
            float tan4Y = cy + polarY(radius, 180);

            computeBezierPath(stickPath
                    , waterDropOffsetLeftX, waterDropOffsetLeftY
                    , waterDropOffsetRightX, waterDropOffsetRightY
                    , tan3X, tan3Y, tan4X, tan4Y);
            canvas.drawPath(stickPath, bigCirclePaint);
        }
    }

    void initAnimator() {


        waterDropAnimator = ValueAnimator.ofInt(0, dp2Dx(180));
        waterDropAnimator.setDuration(2000);
        waterDropAnimator.setInterpolator(new AccelerateInterpolator());
        waterDropAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                waterDropAnimator.setStartDelay(random.nextInt(2000));
                dropInitDegree = 75 + random.nextInt(45);
                waterDropAnimator.start();
            }
        });
        waterDropAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waterDropAnimationValue = (int) animation.getAnimatedValue();
                invalidate();
            }
        });


        waveAnimator1 = ValueAnimator.ofInt(dp2Dx(10), -dp2Dx(10), dp2Dx(10));
        waveAnimator1.setDuration(4000);
        waveAnimator1.setInterpolator(new LinearInterpolator());
        waveAnimator1.setRepeatCount(Integer.MAX_VALUE - 1);
        waveAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveAnimationValue1 = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

        waveAnimator2 = ValueAnimator.ofInt(dp2Dx(10), -dp2Dx(10), dp2Dx(10));
        waveAnimator2.setDuration(5000);
        waveAnimator2.setInterpolator(new LinearInterpolator());
        waveAnimator2.setRepeatCount(Integer.MAX_VALUE - 1);
        waveAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveAnimationValue2 = (int) animation.getAnimatedValue();
            }
        });

        waveAnimator3 = ValueAnimator.ofInt(dp2Dx(10), -dp2Dx(10), dp2Dx(10));
        waveAnimator3.setDuration(6000);
        waveAnimator3.setInterpolator(new LinearInterpolator());
        waveAnimator3.setRepeatCount(Integer.MAX_VALUE - 1);
        waveAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                waveAnimationValue3 = (int) animation.getAnimatedValue();
            }
        });
    }

    int dp2Dx(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }

    void l(Object o) {
        Log.e("######", o.toString());
    }
}
