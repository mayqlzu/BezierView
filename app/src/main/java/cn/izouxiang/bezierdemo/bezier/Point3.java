package cn.izouxiang.bezierdemo.bezier;

import android.graphics.Point;

/**
 * Created by mayq on 2016/11/28.
 */

public class Point3 extends Point{
    private String name; // 比如："11-14", 或者 "2016年11月"

    public Point3(String name, float x, float y) {
        super((int)x, (int)y);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
