package com.greenkee.pokeADot;

/**
 * Created by Gateway Oct 2010 on 8/16/2014.
 */
/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;


public class CanvasView extends SurfaceView implements SurfaceHolder.Callback {
    protected int myCanvas_w, myCanvas_h;
    protected Bitmap myCanvasBitmap = null;
    protected Canvas myCanvas = null;
    protected Matrix identityMatrix;

    protected CanvasActivity activity;

    public boolean dialogOpen = false; //tells if dialog box is open

    public static boolean soundEnabled;

    public static float screenWidth = 0, screenHeight = 0;
    public static float touchBuffer;

    public boolean piecesCreated = false;

    public abstract class CanvasThread extends Thread implements Runnable {


        protected Object mPauseLock;
        protected boolean mPaused;
        protected boolean mFinished;

        public static final int STATE_LOSE = 1;
        public static final int STATE_PAUSE = 2;
        public static final int STATE_READY = 3;
        public static final int STATE_RUNNING = 4;
        public static final int STATE_RESUMING = 5;
        public static final int STATE_SCORE = 6;


        protected float sideBuffer;
        public float bottomBuffer;
        public float screenBottom;

        protected int mCanvasHeight = 1;

        protected int mCanvasWidth = 1;


        /**
         * Message handler used by thread to interact with TextView
         */
        protected Handler mHandler;

        /**
         * Used to figure out elapsed time between frames
         */
        protected long mLastTime;

        // The state of the game. One of READY, RUNNING, PAUSE, LOSE, or WIN
        protected int mMode;

        /**
         * Indicate whether the surface has been created & is ready to draw
         */
        protected boolean mRun = false;

        protected final Object mRunLock = new Object();

        /**
         * Handle to the surface manager object we interact with
         */
        protected SurfaceHolder mSurfaceHolder;

        //private boolean drawEnabled = false;

        protected Canvas c;

        protected SharedPreferences sharedPrefs;

        protected BackgroundScreen background;

        public CanvasThread(SurfaceHolder surfaceHolder, Context context,
                           Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;

            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);

            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;



            bottomBuffer = screenHeight / 10;

            sideBuffer = 0;
            bottomBuffer = 0;


            mPauseLock = new Object();
            mPaused = false;
            mFinished = false;

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        }

        public void screenSizeObtained(float buffer) {
            bottomBuffer = buffer;
            screenBottom = screenHeight - bottomBuffer;
            background = new BackgroundScreen(screenWidth, screenHeight);
            piecesCreated = true;
        }


        /**
         * Starts the game, setting parameters for the current difficulty.
         */
        public void doStart() {
            if(thread.isAlive()){
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    clear(c);
                    mSurfaceHolder.unlockCanvasAndPost(c);
                    mLastTime = System.currentTimeMillis() + 100;
                    setState(STATE_RUNNING);
                }
            }
        }

        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                mLastTime = System.currentTimeMillis() + 100;
            }
            //setState(STATE_RUNNING);
            setState(STATE_RESUMING);
        }

        public void onPause() {
            synchronized (mPauseLock) {
                mPaused = true;
            }
        }

        public void onResume() {
            synchronized (mPauseLock) {
                mPaused = false;
                mPauseLock.notifyAll();
            }
        }

        @Override
        public void run() {
            c = null;
            try {
                Thread.sleep(500);
            } catch (Exception ignored) {
            }
            while (!mFinished) {
                try {
                    c = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {

                        if (mMode == STATE_READY || mMode == STATE_RUNNING || mMode == STATE_RESUMING) updateGame();
                        synchronized (mRunLock) {
                            if (mRun) {
                                if (piecesCreated) {
                                    clear(c);
                                    doDraw(c);
                                }
                            }
                        }
                    }
                } finally {
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                synchronized (mPauseLock) {
                    while (mPaused) {
                        try {
                            mPauseLock.wait();
                        } catch (InterruptedException e) {

                        }
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {
                if (map != null) {
                }
            }
            return map;

        }

        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {
                setState(STATE_PAUSE);
            }
        }

        public void setRunning(boolean b) {
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                setState(mode, null);
            }
        }

        public void setState(int mode, CharSequence message) {
            synchronized (mSurfaceHolder) {
                mMode = mode;
                if (mMode == STATE_RUNNING) {
                    onResume();
                } else {
                    if (mMode == STATE_PAUSE) {
                        onPause();
                    }  else if (mMode == STATE_RESUMING){
                        onResume();
                    }
                }
            }
        }


        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
                // mBackgroundImage = Bitmap.createScaledBitmap(
                //         mBackgroundImage, width, height, true);
            }
        }

        public void resumeScreen() {
            System.out.println("RESUME SCREEN");
            try {
                c = mSurfaceHolder.lockCanvas();
                synchronized (mSurfaceHolder) {
                    synchronized (mRunLock) {
                        if (mRun) {
                            clear(c);
                            doDraw(c);
                        }
                    }
                }
            } finally {
                if (c != null) {
                    mSurfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
        protected abstract void doDraw(Canvas canvas);

        protected void clear(Canvas canvas){
            if(piecesCreated){

                background.draw(myCanvas);
            }
            //myCanvas.drawARGB(255, 0, 0, 0);

            canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);
        }


        protected void updateGame() {
            long now = System.currentTimeMillis();
            // Do nothing if mLastTime is in the future.
            // This allows the game-start to delay the start of the physics
            // by 100ms or whatever.
            if (mLastTime > now) return;
            double elapsed = (now - mLastTime) / 1000.0;
            if(piecesCreated){
                updateComponents(elapsed);

            }
            mLastTime = now;
        }

        protected void updateComponents(double e){
            background.update(e);
        }
    }


    /**
     * Handle to the application context, used to e.g. fetch Drawables.
     */
    protected Context mContext;
    /**
     * The thread that actually draws the animation
     */
    protected CanvasThread thread;
    protected Handler uiHandler;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);

        /* DO THIS IN SUBCLASS CONSTRUCTOR
        uiHandler = new Handler();
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setFocusable(true); // make sure we get key events

        //NOTE: NEED TO CREATE THREAD IN SUBCLASS
        addThread(mThread);
        */

    }

    protected void addThread(CanvasThread mThread){ //sets Thread, callled in subclass constructor
        thread = mThread;
    }

    public void setActivity(CanvasActivity cA){
        activity = cA;
    }

    /**
     * Fetches the animation thread corresponding to this LunarView.
     *
     * @return the animation thread
     */
    public CanvasThread getThread() {
        return thread;
    }

    /**
     * Standard window-focus override. Notice focus lost so we can pause on
     * focus lost. e.g. user switches to take a call.
     */
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();

    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (width != screenWidth || height != screenHeight) {
            System.out.println("POSSIBLE ERROR");
        }
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        if (screenWidth == 0) {
            myCanvas_w = getWidth();
            myCanvas_h = getHeight();
        } else {
            myCanvas_w = (int) screenWidth;
            myCanvas_h = (int) screenHeight;
        }

        myCanvasBitmap = Bitmap.createBitmap(myCanvas_w, myCanvas_h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas();
        myCanvas.setBitmap(myCanvasBitmap);

        identityMatrix = new Matrix();
        thread.setRunning(true);

        if (thread.getState() == Thread.State.NEW) {
            thread.start();
        }
        if (piecesCreated) {
            Canvas canvas = holder.lockCanvas();
            thread.clear(canvas);
            thread.doDraw(canvas);

            canvas.drawBitmap(myCanvasBitmap, identityMatrix, null);

            holder.unlockCanvasAndPost(canvas);
        }
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        //   boolean retry = true;
        System.out.println("SURFACE DESTROYED");
        thread.setRunning(false);
      /*  while (retry) {
            try {
            	System.out.println("JOINED");
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }*/
    }


}

