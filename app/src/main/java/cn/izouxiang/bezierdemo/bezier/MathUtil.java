package cn.izouxiang.bezierdemo.bezier;

import android.graphics.Point;

/**
 * Created by mayq on 2016/11/28.
 */
public class MathUtil {

    // 4个坐标系
    private enum COORD {
        LOGICAL_TABLE,
        INNER_VISIBLE_TABLE,
        OUTER_VISIBLE_TABLE,
        CANVAS
    }

    private int viewWidth;
    private int viewHeight;

    // 水平参考线数量
    private static final int HORIZONTIAL_LINE_NUM = 5;
    // x轴标签数量
    private static final int X_LABEL_NUM = 7;

    private Point3[] in; //收入
    private Point3[] out; //支出

    // 所有点y轴的最小和最大值
    private float min;
    private float max;

    // 物理内表距离物理外表的留白（单位px）
    public static final int marginLeft = 96;
    public static final int marginTop = 80;
    public static final int marginRight = 32;
    public static final int marginBottom = 60;

    public void setMinMax(float min, float max) {
        this.min = min;
        this.max = max;
    }

    /**
     * 设置View的大小
     */
    public void setViewSize(int w, int h) {
        viewWidth = w;
        viewHeight = h;
    }

    /**
     * 检查2个数组中每个arr[i]的x坐标是否重合
     *
     * @return
     */
    private boolean checkData() {
        if (in.length != out.length)
            return false;

        for (int i = 0; i < in.length; i++) {
            if (in[i].x != out[i].x)
                return false;

            if (in[i].x != i)
                return false;
        }

        return true;
    }


    public Point3[] getParsedIn() {
        Point3[] result = new Point3[in.length];
        for (int i = 0; i < in.length; i++) {
            result[i] = map(in[i]);
        }
        return result;
    }

    public Point3[] getParsedOut() {
        Point3[] result = new Point3[out.length];
        for (int i = 0; i < out.length; i++) {
            result[i] = map(out[i]);
        }
        return result;
    }

    /**
     * 设置2条曲线所有通过点的信息
     *
     * @param in
     * @param out
     */
    public void setData(Point3[] in, Point3[] out) {
        this.in = in;
        this.out = out;

        if (!checkData()) {
            throw new AssertionError("invalid data");
        }

    }

    /**
     * 获取所有通过点相对于View的x坐标
     *
     * @return
     */
    public float[] getAxisXPoints() {
        // TODO
        return null;
    }

    /**
     * 获取所有通过点相对于的y坐标
     *
     * @return
     */
    public float[] getAxisYPoints() {
        // TODO
        return null;
    }

    /**
     * 获取所有水平参考线的y坐标
     *
     * @return
     */
    public float[] getLabelY() {
        float[] minMax = minMax();
        float min = minMax[0];
        float max = minMax[1];
        float diff = max - min;

        float bottom = getBottom(min);
        float step = roundUpper(diff / (HORIZONTIAL_LINE_NUM - 1));

        float y0 = bottom;
        float y1 = y0 + step * 1;
        float y2 = y0 + step * 2;
        float y3 = y0 + step * 3;
        float y4 = y0 + step * 4;

        return new float[]{y0, y1, y2, y3, y4};
    }

    /**
     * 计算最低一条参考线的数值
     */
    private float getBottom(float min) {
        return roundLower(min);
    }

    /**
     * 向下取整，且返回值必须是100的倍数（包括0）
     *
     * @param f
     * @return
     */
    private float roundLower(float f) {
        if (f < 100)
            return 0;

        int n = (int) f;
        String str = String.valueOf(n);

        int magnitude = 1;
        for (int l = str.length() - 1; l > 0; l--) {
            magnitude = magnitude * 10;
        }

        return (int) (f / magnitude) * magnitude;
    }

    /**
     * 向上取整，且返回值必须是100的倍数
     *
     * @param f
     * @return
     */
    private float roundUpper(float f) {
        if (f < 100)
            return 100;

        int n = (int) f;
        String str = String.valueOf(n);

        int magnitude = 1;
        for (int l = str.length() - 1; l > 0; l--) {
            magnitude = magnitude * 10;
        }

        return ((int) (f + magnitude) / magnitude) * magnitude;
    }


    /**
     * 获取一个数组所有元素中的最小y值和最大y值
     *
     * @param arr
     * @return
     */
    private float[] minMax(Point3[] arr) {
        if (!(arr.length > 0)) {
            throw new AssertionError("invalid array");
        }

        float min = arr[0].y;
        float max = arr[0].y;

        for (Point3 p : arr) {
            if (p.y < min)
                min = p.y;

            if (p.y> max)
                max = p.y;
        }

        return new float[]{min, max};
    }

    /**
     * 获取两个曲线所有通过点中最小的y值和最大的y值
     *
     * @return
     */
    private float[] minMax() {
        float[] minMax1 = minMax(in);
        float[] minMax2 = minMax(out);

        float min = Math.min(minMax1[0], minMax2[0]);
        float max = Math.max(minMax1[1], minMax2[1]);

        return new float[]{min, max};
    }

    private int getInnerTableWidth() {
        return viewWidth - marginLeft - marginRight;
    }

    private int getInnerTableHeight() {
        return viewHeight - marginTop - marginBottom;
    }

    public Point3 map(Point3 p) {
        Point p2 = new Point(p.x, p.y);
        Point p3 = map(p2);
        return new Point3(p.getName(), p3.x, p3.y);
    }

    public Point map(Point p) {
        return bottomUp(translate(scale(translateY(p))));
    }

    /**
     * y轴刻度可能不是从0开始的，每个点的y坐标需要做相应的偏移调整；
     *
     * @param p
     * @return
     */
    private Point translateY(Point p) {
        int x = p.x;
        int y = p.y - (int) min;
        return new Point(x, y);
    }

    /**
     * 将任意点从一个逻辑上的表格映射到一个View（就是网格所在的区域），
     * 此时，假设坐标位于这个View的左下角，
     * 每个点的单位变成了像素;
     *
     * @param p
     * @return
     */
    private Point scale(Point p) {
        // 7个点的x值为0,1,2, 3, 4, 5, 6
        float x = p.x * getInnerTableWidth() / (X_LABEL_NUM - 1);
        float y = p.y * getInnerTableHeight() / (max - min);

        return new Point((int) x, (int) y);
    }

    /**
     * 将任意点从网格所在的View投影到整个View上，
     * 假设此时坐标还是位于左下角
     *
     * @param p
     * @return
     */
    private Point translate(Point p) {
        int x = p.x + marginLeft;
        int y = p.y + marginBottom;
        return new Point(x, y);
    }

    /**
     * 将坐标从左下角转换成左上角，相当于上下颠倒
     *
     * @param p
     * @return
     */
    private Point bottomUp(Point p) {
        int x = p.x;
        int y = viewHeight - p.y;
        return new Point(x, y);
    }


}
