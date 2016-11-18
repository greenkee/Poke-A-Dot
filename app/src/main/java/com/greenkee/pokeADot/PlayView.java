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

package com.greenkee.pokeADot;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;


public class PlayView extends CanvasView {
    SherlockCanvasActivity act;

    public class TitleThread extends CanvasThread implements Runnable {
        public TitleThread(SurfaceHolder surfaceHolder, Context context,
                          Handler handler) {
            super(surfaceHolder, context, handler);
        }

        @Override
        protected void doDraw(Canvas canvas) {
        }

    }

    public void setSherlockActivity(SherlockCanvasActivity a){
        act = a;
    }



    public PlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        uiHandler = new Handler();
        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        setFocusable(true); // make sure we get key events

        thread = new TitleThread(holder, context, new Handler() {
        });
        addThread(thread);

    }

    public void startTutorialThread(){
        System.out.println("DIALOG OPEN:" + dialogOpen);
        if(!dialogOpen){
            dialogOpen = true;
            System.out.println("START Tutorial THREAD");
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    System.out.println("Tutorial running");
                    uiHandler.post(new Runnable() { // This thread runs in the UI
                        @Override
                        public void run() {

                            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);

                            alert.setTitle("First Time?");
                            alert.setMessage("This looks like your first time playing. Would you like to play the tutorial?");

                            alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogOpen = false;
                                    ((PlayActivity)act).startTutorial();
                                }
                            });
                            alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialogOpen = false;
                                    //createConfirmationAlert();

                                }
                            });
                            alert.show();
                        }
                    });

                }
            };
            new Thread(runnable).start();
        }

    }



}

