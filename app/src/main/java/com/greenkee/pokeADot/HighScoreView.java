package com.greenkee.pokeADot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

/**
 * Created by Gateway Oct 2010 on 8/31/2014.
 */
public class HighScoreView extends CanvasView {
    SherlockCanvasActivity act;
        public class HighScoreThread extends CanvasView.CanvasThread implements Runnable {
            public HighScoreThread(SurfaceHolder surfaceHolder, Context context,
                               Handler handler) {
                super(surfaceHolder, context, handler);
            }

            @Override
            protected void doDraw(Canvas canvas) {
            }

        }

        public HighScoreView(Context context, AttributeSet attrs) {
            super(context, attrs);
            uiHandler = new Handler();
            // register our interest in hearing about changes to our surface
            SurfaceHolder holder = getHolder();
            holder.addCallback(this);

            setFocusable(true); // make sure we get key events

            thread = new HighScoreThread(holder, context, new Handler() {
            });
            addThread(thread);

        }

    public void setSherlockActivity(SherlockCanvasActivity a){
        act = a;
    }

}
