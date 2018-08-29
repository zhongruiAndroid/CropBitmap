package com.test.cropview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    GestureView gestureView;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onDown(MotionEvent e) {
                Log.i("===","===onDown");
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                Log.i("===","===onLongPress====");
                super.onLongPress(e);
            }

            @Override
            public void onShowPress(MotionEvent e) {
                Log.i("===","===onShowPress====");
                super.onShowPress(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                Log.i("===","==="+distanceX+"===="+distanceY);
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Log.i("===","===onFling"+velocityX+"=="+velocityY);
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        } );

        gestureView=findViewById(R.id.view);

        gestureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

    }
}
