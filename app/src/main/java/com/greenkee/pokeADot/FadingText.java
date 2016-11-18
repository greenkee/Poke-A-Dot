package com.greenkee.pokeADot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * Created by Gateway Oct 2010 on 7/28/2014.
 */
public class FadingText {
    String text;

    private Paint textPaint;
    private float xValue; //center
    private float yValue; //center

    private float width, height;

    public float speedX;
    private float speedY;
    private int color;
    private double alpha;

    private boolean destroyed;

    private int minAlpha = 0;

    // The gesture threshold expressed in dip
    private static final float TEXT_SIZE = 32.0f;

    private int mTextSize;

    public FadingText(Context cont, float x, float y, int c, String s){


        // Convert the dips to pixels
        final float scale = cont.getResources().getDisplayMetrics().density;
        mTextSize = (int)(TEXT_SIZE * scale + 0.5f);
        height = mTextSize;

        text = s;

        color = c;
        alpha = 255;

        textPaint = new Paint();
        textPaint.setAntiAlias(true);

        textPaint.setTextSize(mTextSize);
        textPaint.setTypeface(Typeface.SANS_SERIF);

        width = textPaint.measureText(s);


        xValue = x - (width/2);
        yValue = y;

        destroyed = false;
    }


    public void update(double elapsed){
        alpha -= (elapsed*150);
        if (alpha <= minAlpha){
            destroy();
            System.out.println("DESTROY");
        }else{
            System.out.println("NEW ALPHA:" + alpha);
        }
    }

    public void draw(Canvas canvas){
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(color);
        textPaint.setAlpha((int)alpha);
        canvas.drawText(text, xValue, yValue, textPaint);

        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeWidth(mTextSize/20);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha((int)alpha);
        canvas.drawText(text, xValue, yValue, textPaint);

    }

    public void setPosition(float x, float y){
        xValue = x;
        yValue = y;
    }

    public void setColor(int c){
        color = c;
    }

    public int getColor(){
        return color;
    }

    public float getX(){
        return xValue;
    }

    public float getY(){
        return yValue;
    }

    public void destroy(){
        destroyed = true;
    }

    public float getWidth(){
        return width;
    }

    public float getHeight(){
        return height;
    }

    public boolean isDestroyed(){
        return destroyed;
    }
}
