package cn.izouxiang.bezierdemo.bezier;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by zouxiang on 2016/9/22.
 */

public class BezierView extends View {
    private final static float LINE_SMOOTHNESSS = 0.2f;

    private Point3[] line1;
    private Point3[] line2;

    private Point3[] parsedLine1;
    private Point3[] parsedLine2;

    private String[] yLabels;
    private Point3 axisOrigin; // 坐标原点

    private PathMeasure mPathMeasure1;
    private PathMeasure mPathMeasure2;

    private Paint mPaintPath1;
    private Paint mPaintPath2;
    private Paint mPaintShadow1;
    private Paint mPaintShadow2;
    private Paint mPaintGrid;
    private Paint mPaintLabel;
    private Paint mPaintStick;
    private Paint mPaintDot;
    private Paint mPaintFlagBkg;
    private Paint mPaintFlagText;

    private float drawScale = 1f;
    private int currentPntIndex = -1;

    // 旗子比旗杆顶端低多少
    private static final int FLAG_BELOW_STICK = 20;
    // 旗子和旗杆的间距
    private static final int MARGIN_FLAG_STICK = 20;
    // 旗子文字的边距
    private static final int FLAG_TEXT_PADDING = 15;
    // 旗子文字行间距
    private static final int FLAG_TEXT_LINE_SPACE = 10;

    private final MathUtil utils = new MathUtil();

    public BezierView(Context context) {
        super(context);

        init();
    }

    public BezierView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void closeHardwareAcceleration() {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    private void init() {
        // 图像抖动或者缺失，关闭硬件加速，好了
        closeHardwareAcceleration();

        mPaintPath1 = new Paint();
        mPaintPath1.setColor(0xffff6b00);
        mPaintPath1.setStrokeWidth(4);
        mPaintPath1.setStyle(Paint.Style.STROKE);

        mPaintPath2 = new Paint();
        mPaintPath2.setColor(0xff2eabf1);
        mPaintPath2.setStrokeWidth(4);
        mPaintPath2.setStyle(Paint.Style.STROKE);

        mPaintShadow1 = new Paint();
        mPaintShadow1.setStyle(Paint.Style.FILL);
        mPaintShadow1.setColor(0x88CCCCCC);
        // 新建一个线性渐变，前两个参数是渐变开始的点坐标，第三四个参数是渐变结束的点的坐标。
        // 连接这2个点就拉出一条渐变线了，玩过PS的都懂。然后那个数组是渐变的颜色。
        // 下一个参数是渐变颜色的分布，如果为空，每个颜色就是均匀分布的。最后是模式，这里设置的是循环渐变
        Shader mShader = new LinearGradient(0, 0, 0, 500,
                new int[]{Color.parseColor("#5fff6b00"), Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP);
        mPaintShadow1.setShader(mShader);

        mPaintShadow2 = new Paint();
        mPaintShadow2.setStyle(Paint.Style.FILL);
        mPaintShadow2.setColor(0x88CC11CC);
        // 新建一个线性渐变，前两个参数是渐变开始的点坐标，第三四个参数是渐变结束的点的坐标。
        // 连接这2个点就拉出一条渐变线了，玩过PS的都懂。然后那个数组是渐变的颜色。
        // 下一个参数是渐变颜色的分布，如果为空，每个颜色就是均匀分布的。最后是模式，这里设置的是循环渐变
        Shader mShader2 = new LinearGradient(0, 0, 0, 500,
                new int[]{Color.parseColor("#5f2eabf1"), Color.TRANSPARENT},
                null, Shader.TileMode.CLAMP);
        mPaintShadow2.setShader(mShader2);

        mPaintDot = new Paint();
        mPaintDot.setColor(0xffff6b00);
        mPaintDot.setStrokeWidth(3);
        mPaintDot.setStyle(Paint.Style.FILL);

        mPaintLabel = new Paint();
        mPaintLabel.setColor(0xff888888);
        mPaintLabel.setStyle(Paint.Style.STROKE);
        mPaintLabel.setTextSize(20);
        mPaintLabel.setAntiAlias(true);
        mPaintLabel.setTextAlign(Paint.Align.LEFT);

        mPaintGrid = new Paint();
        mPaintGrid.setColor(0x5f888888);
        mPaintGrid.setStyle(Paint.Style.STROKE);
        mPaintGrid.setStrokeWidth(1);

        mPaintStick = new Paint();
        mPaintStick.setColor(0xffff6b00);
        mPaintStick.setStyle(Paint.Style.STROKE);
        mPaintStick.setStrokeWidth(2);

        mPaintFlagBkg = new Paint();
        mPaintFlagBkg.setColor(0xffff6b00);
        mPaintFlagBkg.setStyle(Paint.Style.FILL);

        mPaintFlagText = new Paint();
        mPaintFlagText.setColor(Color.WHITE);
        mPaintFlagText.setStyle(Paint.Style.STROKE);
        mPaintFlagText.setTextSize(20);
        mPaintFlagText.setAntiAlias(true);
        mPaintFlagText.setTextAlign(Paint.Align.LEFT);
    }

    /**
     * 设置贝塞尔曲线的通过点
     *
     * @param l1
     * @param l2
     */
    public void setPointList(Point3[] l1, Point3[] l2) {
        this.line1 = l1;
        this.line2 = l2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        utils.setViewSize(getMeasuredWidth(), getMeasuredHeight());
    }

    /**
     * 动画回调方法
     *
     * @param drawScale
     */
    public void setDrawScale(float drawScale) {
        this.drawScale = drawScale;
        postInvalidate();
    }

    /**
     * 开始动画
     *
     * @param duration
     */
    public void startAnimation(long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "drawScale", 0f, 1f);
        animator.setDuration(duration);
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (null == parsedLine1) {
            utils.setData(line1, line2);

            axisOrigin = utils.map(new Point3("", 0, 0));

            this.parsedLine1 = utils.getParsedIn();
            this.parsedLine2 = utils.getParsedOut();

            mPathMeasure1 = measurePath(parsedLine1);
            mPathMeasure2 = measurePath(parsedLine2);
        }

        if (null == mPathMeasure1 || null == mPathMeasure2)
            return;

        drawGrid(canvas);
        drawXLabel(canvas);
        drawYLabel(canvas);

        drawPath(canvas, mPathMeasure1, mPaintPath1, mPaintShadow1);
        drawPath(canvas, mPathMeasure2, mPaintPath2, mPaintShadow2);

        drawFlag(canvas);
    }

    /**
     * 画y轴上的标签
     *
     * @param canvas
     */
    private void drawYLabel(Canvas canvas) {
        if (null == yLabels)
            return;

        Paint.FontMetrics metrics = mPaintLabel.getFontMetrics();
        float fontHeight = metrics.descent - metrics.ascent;
        int fontHalf = (int) (fontHeight / 2); //文字高度的一半

        int h = getMeasuredHeight() - MathUtil.marginTop - MathUtil.marginBottom;
        int x0 = MathUtil.marginLeft / 3;
        int y0 = MathUtil.marginTop + fontHalf;
        int step = h / 4;

        for (int i = 0; i < yLabels.length; i++) {
            int x = x0;
            int y = y0 + step * i;
            canvas.drawText(yLabels[i], x, y, mPaintLabel);
            //canvas.drawCircle(x, y, 10, mPaintLabel);
        }
    }

    /**
     * 画x轴上的标签
     *
     * @param canvas
     */
    private void drawXLabel(Canvas canvas) {
        if (null == this.line1)
            return;

        Paint.FontMetrics metrics = mPaintLabel.getFontMetrics();
        float fontHeight = metrics.descent - metrics.ascent;

        String text0 = line1[0].getName();
        int x0 = MathUtil.marginLeft;
        int y0 = getMeasuredHeight() - MathUtil.marginBottom + (int) fontHeight;
        y0 += 10; // add some marginTop

        Rect bounds = new Rect();
        String text6 = line1[6].getName();
        mPaintLabel.getTextBounds(text6, 0, text6.length(), bounds);
        int x6 = getMeasuredWidth() - MathUtil.marginRight - bounds.width();
        int y6 = y0;

        //canvas.drawCircle(x6, y6, 10, mPaintLabel);
        canvas.drawText(text0, x0, y0, mPaintLabel);
        canvas.drawText(text6, x6, y6, mPaintLabel);
    }

    /**
     * 画参考线
     *
     * @param canvas
     */
    private void drawGrid(Canvas canvas) {
        int w = getMeasuredWidth() - MathUtil.marginRight;
        int h = getMeasuredHeight() - MathUtil.marginTop - MathUtil.marginBottom;
        int x0 = MathUtil.marginLeft;
        int y0 = MathUtil.marginTop;

        float[] pts = new float[]{
                x0, y0, w, y0,
                x0, y0 + 1 * h / 4, w, y0 + 1 * h / 4,
                x0, y0 + 2 * h / 4, w, y0 + 2 * h / 4,
                x0, y0 + 3 * h / 4, w, y0 + 3 * h / 4,
                x0, y0 + 4 * h / 4, w, y0 + 4 * h / 4
        };
        canvas.drawLines(pts, mPaintGrid);
    }

    /**
     * 画贝塞尔曲线
     *
     * @param canvas
     * @param pathMeasure
     * @param paintPath
     * @param paintShadow
     */
    private void drawPath(Canvas canvas, PathMeasure pathMeasure, Paint paintPath,
                          Paint paintShadow) {
        Path dst = new Path();
        dst.rLineTo(0, 0);
        float distance = pathMeasure.getLength() * drawScale;
        if (pathMeasure.getSegment(0, distance, dst, true)) {
            //绘制线
            canvas.drawPath(dst, paintPath);
            float[] pos = new float[2];
            pathMeasure.getPosTan(distance, pos, null);
            //绘制阴影 TODO 画渐变色背景为何会导致抖动？
            drawShadowArea(canvas, dst, pos, paintShadow);
        }
    }

    /**
     * 绘制阴影
     *
     * @param canvas
     * @param path
     * @param pos
     */
    private void drawShadowArea(Canvas canvas, Path path, float[] pos, Paint paintShadow) {
        path.lineTo(pos[0], axisOrigin.y);
        path.lineTo(axisOrigin.x, axisOrigin.y);
        path.close();
        canvas.drawPath(path, paintShadow);
    }

    /**
     * 根据几个通过点计算贝塞尔曲线
     *
     * @param pointList
     * @return
     */
    private PathMeasure measurePath(Point3[] pointList) {
        Path path = new Path();
        float prePreviousPointX = Float.NaN;
        float prePreviousPointY = Float.NaN;
        float previousPointX = Float.NaN;
        float previousPointY = Float.NaN;
        float currentPointX = Float.NaN;
        float currentPointY = Float.NaN;
        float nextPointX;
        float nextPointY;

        final int lineSize = pointList.length;
        for (int valueIndex = 0; valueIndex < lineSize; ++valueIndex) {
            if (Float.isNaN(currentPointX)) {
                Point point = pointList[valueIndex];
                currentPointX = point.x;
                currentPointY = point.y;
            }
            if (Float.isNaN(previousPointX)) {
                //是否是第一个点
                if (valueIndex > 0) {
                    Point point = pointList[valueIndex - 1];
                    previousPointX = point.x;
                    previousPointY = point.y;
                } else {
                    //是的话就用当前点表示上一个点
                    previousPointX = currentPointX;
                    previousPointY = currentPointY;
                }
            }

            if (Float.isNaN(prePreviousPointX)) {
                //是否是前两个点
                if (valueIndex > 1) {
                    Point point = pointList[valueIndex - 2];
                    prePreviousPointX = point.x;
                    prePreviousPointY = point.y;
                } else {
                    //是的话就用当前点表示上上个点
                    prePreviousPointX = previousPointX;
                    prePreviousPointY = previousPointY;
                }
            }

            // 判断是不是最后一个点了
            if (valueIndex < lineSize - 1) {
                Point point = pointList[valueIndex + 1];
                nextPointX = point.x;
                nextPointY = point.y;
            } else {
                //是的话就用当前点表示下一个点
                nextPointX = currentPointX;
                nextPointY = currentPointY;
            }

            if (valueIndex == 0) {
                // 将Path移动到开始点
                path.moveTo(currentPointX, currentPointY);
            } else {
                // 求出控制点坐标
                final float firstDiffX = (currentPointX - prePreviousPointX);
                final float firstDiffY = (currentPointY - prePreviousPointY);
                final float secondDiffX = (nextPointX - previousPointX);
                final float secondDiffY = (nextPointY - previousPointY);
                final float firstControlPointX = previousPointX + (LINE_SMOOTHNESSS * firstDiffX);
                final float firstControlPointY = previousPointY + (LINE_SMOOTHNESSS * firstDiffY);
                final float secondControlPointX = currentPointX - (LINE_SMOOTHNESSS * secondDiffX);
                final float secondControlPointY = currentPointY - (LINE_SMOOTHNESSS * secondDiffY);
                //画出曲线
                path.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY,
                        currentPointX, currentPointY);
            }

            // 更新值,
            prePreviousPointX = previousPointX;
            prePreviousPointY = previousPointY;
            previousPointX = currentPointX;
            previousPointY = currentPointY;
            currentPointX = nextPointX;
            currentPointY = nextPointY;
        }

        return new PathMeasure(path, false);
    }

    /**
     * 设置y轴上的标签
     *
     * @param arr
     */
    public void setYLabels(String[] arr) {
        this.yLabels = arr;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawFlag(event);
                return true; // I am interested line1 move and up
            case MotionEvent.ACTION_MOVE:
                drawFlag(event);
                break;
            case MotionEvent.ACTION_UP:
                clearFlag();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 画旗杆和旗帜
     *
     * @param canvas
     */
    private void drawFlag(Canvas canvas) {
        if (currentPntIndex < 0)
            return;

        // 旗杆
        float x0 = parsedLine1[currentPntIndex].x;
        float y0 = MathUtil.marginTop / 2;
        float x1 = x0;
        float y1 = getMeasuredHeight() - MathUtil.marginBottom;
        canvas.drawLine(x0, y0, x1, y1, mPaintStick);

        // 旗杆顶部的圆点
        canvas.drawCircle(x0, y0, 6, mPaintDot);

        // 旗帜背景
        String line0 = line1[currentPntIndex].getName();
        String line1 = "收入：" + this.line1[currentPntIndex].y + "元";
        String line2 = "支出：" + this.line2[currentPntIndex].y + "元";
        int allTextWidth = measureWidth(line0, line1, line2);
        int lineHeight = measureHeight(line0);

        int flagWith = allTextWidth + 2 * FLAG_TEXT_PADDING;
        // 当用户指向第一个点，左侧没有空间显示旗子了，此时应该平移到右侧
        int offset = (0 == currentPntIndex) ? flagWith + 2 * MARGIN_FLAG_STICK : 0;
        RectF rect = new RectF(x0 - allTextWidth - 2 * FLAG_TEXT_PADDING - MARGIN_FLAG_STICK,
                y0 + FLAG_BELOW_STICK,
                x0 - MARGIN_FLAG_STICK,
                y0 + +lineHeight + 3 * lineHeight + 2 * FLAG_TEXT_LINE_SPACE + 2 * FLAG_TEXT_PADDING);
        rect.offset(offset, 0);
        canvas.drawRoundRect(rect, 6, 6, mPaintFlagBkg);

        // 旗帜上的文字
        x0 = x0 - allTextWidth - FLAG_TEXT_PADDING - MARGIN_FLAG_STICK;
        x0 += offset;
        y0 = y0 + FLAG_BELOW_STICK + FLAG_TEXT_PADDING + lineHeight;
        canvas.drawText(line0, x0, y0, mPaintFlagText);
        canvas.drawText(line1, x0, y0 + lineHeight + FLAG_TEXT_LINE_SPACE, mPaintFlagText);
        canvas.drawText(line2, x0, y0 + 2 * (lineHeight + FLAG_TEXT_LINE_SPACE), mPaintFlagText);
    }


    /**
     * 计算3行文字的最大宽度
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    private int measureWidth(String a, String b, String c) {
        int w0, w1, w2;
        Rect bounds = new Rect();
        mPaintLabel.getTextBounds(a, 0, a.length(), bounds);
        w0 = bounds.width();
        mPaintLabel.getTextBounds(b, 0, b.length(), bounds);
        w1 = bounds.width();
        mPaintLabel.getTextBounds(c, 0, c.length(), bounds);
        w2 = bounds.width();

        return Math.max(Math.max(w0, w1), w2);
    }

    /**
     * 计算一行文字的高度
     *
     * @param a
     * @return
     */
    private int measureHeight(String a) {
        Rect bounds = new Rect();
        mPaintLabel.getTextBounds(a, 0, a.length(), bounds);
        return bounds.height();
    }


    /**
     * 画旗杆和旗帜
     *
     * @param e
     */
    private void drawFlag(MotionEvent e) {
        currentPntIndex = findFlagPosition(e);
        postInvalidate();
    }


    /**
     * 清除旗杆和旗帜
     */
    private void clearFlag() {
        currentPntIndex = -1;
        postInvalidate();
    }


    /**
     * 根据手指位置计算当前旗杆位置
     *
     * @param e
     * @return
     */
    private int findFlagPosition(MotionEvent e) {
        int index = 0;
        float minDistance = Float.MAX_VALUE;
        float fingerX = e.getX();
        for (int i = 0; i < parsedLine1.length; i++) {
            float x = parsedLine1[i].x;
            float distance = Math.abs(x - fingerX);
            if (distance < minDistance) {
                minDistance = distance;
                index = i;
            }
        }

        return index;
    }


    /**
     * 设置y坐标小大和最小刻度
     *
     * @param min
     * @param max
     */
    public void setMinMax(float min, float max) {
        utils.setMinMax(min, max);
    }
}
