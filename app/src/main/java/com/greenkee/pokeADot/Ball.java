package com.greenkee.pokeADot;

/**
 * Created by Gateway Oct 2010 on 7/24/2014.
 */
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Ball {
    public static float radius;

    protected Paint ballPaint;
    protected float xValue; //center
    protected float yValue; //center

    protected float speedX;
    protected float speedY;
    protected int color;
    protected int displayColor;

    protected final float BASE_SPEED = GameView.screenHeight;
    protected final float MIN_SPEED = GameView.screenHeight/3;
    protected final float MAX_SPEED = GameView.screenHeight;

    protected float speedIncrement = (GameView.screenHeight)/10;
    protected float currentSpeed;

    protected boolean vulnerable;
    protected boolean destroyed;

    public Ball(float x, float y, int c){
        xValue = x;
        yValue = y;

        color = c;

        ballPaint = new Paint();
        ballPaint.setAntiAlias(true);
        ballPaint.setColor(color);

        currentSpeed = BASE_SPEED;

        speedX = (BASE_SPEED * (float)(.5 + Math.random()));
        speedY = (BASE_SPEED * (float)(.5 + Math.random()));

        int random = (int)(Math.random()*2);
        if(random == 0){
            speedX *= -1;
        }

        random = (int)(Math.random()*2);
        if(random == 0){
            speedY *= -1;
        }

        checkMinSpeeds();
        normalizeSpeed();

        vulnerable = false;
        destroyed = false;
    }


    public void update(double elapsed){
        xValue += elapsed* speedX;
        yValue += elapsed* speedY;
        vulnerable = true;
    }

    public void draw(Canvas canvas){
       displayColor = color;

        ballPaint.setColor(displayColor);
        canvas.drawCircle(xValue, yValue, radius, ballPaint);
    }

    public void setPosition(float x, float y){
        xValue = x;
        yValue = y;
    }

    public void changeColor(){
        if(color == Color.RED){
            color = Color.GREEN;
        }else{
            color = Color.RED;
        }
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
    protected void checkMinSpeeds(){
        if(Math.abs(speedX) < MIN_SPEED){
            if(speedX > 0){
                speedX = MIN_SPEED;
            }else{
                speedX = -MIN_SPEED;
            }
        }
        if(Math.abs(speedY) < MIN_SPEED){
            if(speedY > 0){
                speedY = MIN_SPEED;
            }else{
                speedY = -MIN_SPEED;
            }
        }
    }

    protected void increaseSpeed(){
        speedX *= (.5 + Math.random() );
        speedY *= (.5 + Math.random() );
        checkMinSpeeds();

        //currentSpeed += speedIncrement; //INCREMENT ENABLED?
        normalizeSpeed();
    }

    public void bounceX(float boundary){
        increaseSpeed();

        speedX *= -1;
        xValue = boundary;

        /*
        if (GameView.soundEnabled && GameActivity.bounceSound != -1){
            GameActivity.audio.play(GameActivity.bounceSound, 1, 1, 1, 0, 1);
        }
        */
    }

    public void bounceY(float boundary){
        increaseSpeed();

        speedY *= -1;
        yValue = boundary;

        /*
        if (GameView.soundEnabled && GameActivity.bounceSound != -1){
            GameActivity.audio.play(GameActivity.bounceSound, 1, 1, 1, 0, 1);
        }
        */
    }


    protected void normalizeSpeed() {
      //  if( (Math.abs(speedX) + Math.abs(speedY)) > MAX_SPEED){
            double vectorLength = Math.sqrt(Math.pow(speedX, 2) + Math.pow(speedY, 2));
            speedX /=vectorLength;
            speedY /= vectorLength;

       // }
        speedX *= currentSpeed;
        speedY *= currentSpeed;
    }

    public void setSpeedX(float x){
        speedX = x;
    }

    public void setSpeedY(float y){
        speedY = y;
    }

    public float getSpeedX(){
        return speedX;
    }

    public float getSpeedY(){
        return speedY;
    }

    public float getRadius(){
        return radius;
    }

    public boolean contactLine(float x1, float y1, float x2, float y2){
        String slope; float origC;
        if (x2-x1 != 0){
            slope = Float.toString((y2-y1)/(x2-x1));
            origC = y1 - (Float.parseFloat(slope)*x1);
        } else{
            slope = "undef";
            origC = -1; //should never be reached
        }


        float perpSlope;

        if (slope.equals("undef")){
            perpSlope = 0;
        } else {
            perpSlope = (1/ (Float.parseFloat(slope)));
        }

        float perpC = yValue - (perpSlope*xValue);
        float intersectX, intersectY;

        if (slope.equals("undef")){
            intersectX = x1;
            intersectY = yValue;
        } else {
            intersectX = (perpC - origC)/ (Float.parseFloat(slope) - perpSlope);
            intersectY = Float.parseFloat(slope)*intersectX + origC;
        }

        {if (intersectX > x1 && intersectX > x2){
            if (x1 > x2){
                intersectX = x1;
            }else{
                intersectX = x2;
            }
        }
        else if (intersectX < x1 && intersectX < x2){
            if (x1 < x2){
                intersectX = x1;
            } else{
                intersectX = x2;
            }
        }}

        {if (intersectY > y1 && intersectY > y2){
            if (y1 > y2){
                intersectY = y1;
            }else{
                intersectY = y2;
            }
        }
        else if (intersectY < y1 && intersectY < y2){
            if (y1 < y2){
                intersectY = y1;
            } else{
                intersectY = y2;
            }
        }}


        if (Math.sqrt(Math.pow((intersectX - xValue) , 2) + Math.pow((intersectY - yValue) , 2)) <= radius){
            //System.out.println("Slope:  " + slope + " IX: "+ intersectX + " IY: "+ intersectY + " xV: " + xValue + " yV: " +yValue + " P1:" + x1 + "," + y1 + " P2:" + x2 +"," + y2);
            return true;
        }else{
            return false;
        }
    }


    public boolean isVulnerable(){
        return vulnerable;
    }
    public void changeVulnerability(boolean b){
        vulnerable = b;
    }

    public void destroy(){
        destroyed = true;
    }

    public boolean isDestroyed(){
        return destroyed;
    }

    public boolean isTouching(float x, float y){
        double distance = Math.sqrt(  Math.pow( (x - getX()), 2) +  Math.pow( (y - getY()), 2)  );
        return distance < (getRadius()+ GameView.touchBuffer);
    }

    public void setSpeedIncrement(boolean b){
        if(b == true){
            speedIncrement = (GameView.screenHeight)/10;
        } else{
            speedIncrement = 0;
        }
    }

    public float getSpeedIncrement(){
        return speedIncrement;
    }
}