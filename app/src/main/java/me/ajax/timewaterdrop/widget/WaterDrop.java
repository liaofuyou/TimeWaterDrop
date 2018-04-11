package me.ajax.timewaterdrop.widget;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;

/**
 * Created by aj on 2018/4/10
 */

public class WaterDrop {

    private PointF[] pointF1 = new PointF[3];
    private PointF[] pointF2 = new PointF[3];
    private PointF[] pointF3 = new PointF[3];
    private PointF[] pointF4 = new PointF[3];
    private Path path = new Path();
    private float circleRadius;
    private PathMeasure pathMeasure = new PathMeasure();

    //左边切点
    private float[] leftTanPoint = new float[2];
    //右边切点
    private float[] rightTanPoint = new float[2];

    //水滴最后的切点
    private float[] lastTanPoint = new float[2];

    public WaterDrop(float circleRadius) {
        this.circleRadius = circleRadius;

        //实例化点
        for (int i = 0; i < 3; i++) {
            pointF1[i] = new PointF();
            pointF2[i] = new PointF();
            pointF3[i] = new PointF();
            pointF4[i] = new PointF();
        }
    }

    private void computePosition(float offsetX, float offsetY) {

        pointF1[0].x = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointF1[0].y = -circleRadius;
        pointF1[1].x = 0;
        pointF1[1].y = -circleRadius;
        pointF1[2].x = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointF1[2].y = -circleRadius;

        pointF2[0].x = circleRadius;
        pointF2[0].y = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointF2[1].x = circleRadius;
        pointF2[1].y = 0;
        pointF2[2].x = circleRadius;
        pointF2[2].y = (float) (Math.tan(Math.toRadians(30)) * circleRadius);

        pointF3[0].x = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointF3[0].y = circleRadius;
        pointF3[1].x = 0;
        pointF3[1].y = circleRadius;
        pointF3[2].x = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointF3[2].y = circleRadius;

        pointF4[0].x = -circleRadius;
        pointF4[0].y = (float) (Math.tan(Math.toRadians(30)) * circleRadius);
        pointF4[1].x = -circleRadius;
        pointF4[1].y = 0;
        pointF4[2].x = -circleRadius;
        pointF4[2].y = -(float) (Math.tan(Math.toRadians(30)) * circleRadius);

        pointF1[1].y -= circleRadius / 3;

        //实例化点
        for (int i = 0; i < 3; i++) {
            pointF1[i].y += offsetY;
            pointF1[i].x += offsetX;
            pointF2[i].y += offsetY;
            pointF2[i].x += offsetX;
            pointF3[i].y += offsetY;
            pointF3[i].x += offsetX;
            pointF4[i].y += offsetY;
            pointF4[i].x += offsetX;
        }
    }


    void draw(Canvas canvas, Paint mPaint, float offsetX, float offsetY, float bezierFraction) {

        //计算位置
        computePosition(offsetX, offsetY - circleRadius);

        //路径
        path.reset();
        path.moveTo(pointF1[1].x, pointF1[1].y);
        path.cubicTo(pointF1[2].x, pointF1[2].y, pointF2[0].x, pointF2[0].y, pointF2[1].x, pointF2[1].y);
        path.cubicTo(pointF2[2].x, pointF2[2].y, pointF3[0].x, pointF3[0].y, pointF3[1].x, pointF3[1].y);
        path.cubicTo(pointF3[2].x, pointF3[2].y, pointF4[0].x, pointF4[0].y, pointF4[1].x, pointF4[1].y);
        path.cubicTo(pointF4[2].x, pointF4[2].y, pointF1[0].x, pointF1[0].y, pointF1[1].x, pointF1[1].y);

        //路径测量
        pathMeasure.setPath(path, false);

        float distance = pathMeasure.getLength() / 2 * (1 - bezierFraction);
        if (distance > 0) {
            pathMeasure.getPosTan(pathMeasure.getLength() - distance, leftTanPoint, null);
            pathMeasure.getPosTan(distance, rightTanPoint, null);
            pathMeasure.getPosTan(0, lastTanPoint, null);

        }

        //mPaint.setColor(Color.GREEN);
        canvas.drawPath(path, mPaint);
    }

    public float[] getLeftTanPoint() {
        return leftTanPoint;
    }

    public float[] getRightTanPoint() {
        return rightTanPoint;
    }

    public float[] getLastTanPoint() {
        return lastTanPoint;
    }
}
