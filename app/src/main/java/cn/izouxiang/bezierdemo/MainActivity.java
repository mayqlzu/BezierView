package cn.izouxiang.bezierdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import cn.izouxiang.bezierdemo.bezier.BezierView;
import cn.izouxiang.bezierdemo.bezier.Point3;

public class MainActivity extends AppCompatActivity {
    private BezierView mBezierView;
    private EditText mEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBezierView = (BezierView) findViewById(R.id.bezier);
        mEditText = (EditText) findViewById(R.id.editText);

        final Point3[] in = new Point3[]{
                new Point3("0", 0, 100),
                new Point3("1", 1, 200),
                new Point3("2", 2, 500),
                new Point3("3", 3, 300),
                new Point3("4", 4, 800),
                new Point3("5", 5, 500),
                new Point3("6", 6, 100),
        };

        final Point3[] out = new Point3[]{
                new Point3("0", 0, 200),
                new Point3("1", 1, 100),
                new Point3("2", 2, 300),
                new Point3("3", 3, 500),
                new Point3("4", 4, 500),
                new Point3("5", 5, 800),
                new Point3("6", 6, 200),
        };

        mBezierView.setMinMax(100, 1000);
        mBezierView.setYLabels(new String[]{"4", "3", "2", "1", "0"});
        mBezierView.setPointList(in, out);
        mBezierView.startAnimation(2000);
    }

    public void onClick(View v) {
        mBezierView.startAnimation(2000);
    }
}
