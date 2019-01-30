package com.test.cropview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

public class TestActivity extends AppCompatActivity {
    ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
    }

    private void initView() {
        iv=findViewById(R.id.iv);
        iv.setImageBitmap(TestBean.bitmap);
        Log.i("=====","====="+TestBean.bitmap.getWidth());
        Log.i("=====","====="+TestBean.bitmap.getHeight());
    }
}
