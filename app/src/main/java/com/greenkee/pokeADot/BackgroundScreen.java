package com.greenkee.pokeADot;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;


/**
 * Created by Gateway Oct 2010 on 8/8/2014.
 */
public class BackgroundScreen {
    private float BASE_SPEED;
    float maxWidth;
    float maxHeight;
    protected class GamePoint extends android.graphics.Point{
        float x;
        float y;

        int direction = 0; // 0 is right (+x), 1 is down (+y), 2 is left (-x), 3 is up (-y)

        public GamePoint(float x, float y){
            this.x =x;
            this.y = y;
        }

        public void set(float x, float y){
            this.x = x;
            this.y = y;
        }

        public void update(double e){
            float speed = (float)(Math.random()*BASE_SPEED*e);

            determineDirection();
            switch(direction){
                case 0:
                    x += speed;
                    break;
                case 1:
                    y += speed;
                    break;
                case 2:
                    x -= speed;
                    break;
                case 3:
                    y -= speed;
                    break;
            }
            checkBounds();

        }

        protected void checkBounds(){
            if(x > maxWidth){
                x = maxWidth;
            }else if (x < 0){
                x = 0;
            }
            if(y > maxHeight){
                y = maxHeight;
            }else if (y < 0){
                y = 0;
            }
        }

        protected void determineDirection(){
            if(y == 0 && (x >= 0 && x< maxWidth)){
                direction = 0;
            }else if(x == maxWidth && (y >= 0 && y < maxHeight)){
                direction = 1;
            }else if(y == maxHeight && (x <= maxWidth && x > 0)){
                direction = 2;
            }else if(x == 0 && (y <= maxHeight && y > 0)){
                direction = 3;
            }else{
                System.out.println("DIRECTION ERROR");
            }

        }
    }

    protected class BackgroundPolygon {
        private GamePoint point1;
        private GamePoint point2;
        private GamePoint corner;
        private Paint polyPaint;
        private Path polygonDrawer;
        private int targetR;
        private int currentR;
        private int targetB;
        private int currentB;
        private int targetG;
        private int currentG;
        private final int COLOR_MAX = 190;
        private final int DEADBAND = 10;




        public BackgroundPolygon(GamePoint p1, GamePoint p2) {
            point1 = p1;
            point2 = p2;
            polyPaint = new Paint();
            polyPaint.setStyle(Paint.Style.FILL);
            polyPaint.setAntiAlias(true);

            currentR = getRandomColor();
            targetR = getRandomColor();
            currentB = getRandomColor();
            targetB = getRandomColor();
            currentG = getRandomColor();
            targetG = getRandomColor();

            polygonDrawer = new Path();
        }

        protected void updateMovement(double e){
            point1.update(e);
            point2.update(e);
        }

        protected void updateColors(double e){
            if(Math.abs(currentR - targetR) < DEADBAND){
                targetR = getRandomColor();
                //System.out.println("TARGET R:" + targetR);
            }
            if(Math.abs(currentG - targetG) < DEADBAND){
                targetG = getRandomColor();
               // System.out.println("TARGET G:" + targetG);
            }
            if(Math.abs(currentB - targetB) < DEADBAND){
                targetB = getRandomColor();
               // System.out.println("TARGET B:" + targetB);
            }
            if(currentR > targetR){
                currentR -= e*50;
            }else{
                currentR += e*50;
            }
            if(currentG > targetG){
                currentG -= e*50;
            }else{
                currentG += e*50;
            }
            if(currentB > targetB){
                currentB -= e*50;
            }else{
                currentB += e*50;
            }
            //System.out.println("R:"+ currentR + " G:" + currentG + " B:"+currentB+" E:"+ e);
            checkColorBounds();
        }

        public void update(double e){
            updateColors(e);

            updateMovement(e);
        }

        protected void checkColorBounds(){
            if(currentR < 0 || currentR > COLOR_MAX){
                if(currentR < 0){
                    currentR = 0;
                }else{
                    currentR = COLOR_MAX;
                }
            }
            if(currentG < 0 || currentG > COLOR_MAX){
                if(currentG < 0){
                    currentG = 0;
                }else{
                    currentG = COLOR_MAX;
                }
            }
            if(currentB < 0 || currentB > COLOR_MAX){
                if(currentB < 0){
                    currentB = 0;
                }else{
                    currentB = COLOR_MAX;
                }
            }
        }

        public void draw(Canvas c) {
            //System.out.println("POLYGON DRAWN");
            polyPaint.setColor(Color.argb(255, currentR, currentB, currentG));
            polygonDrawer.reset();
            polygonDrawer.moveTo(center.x, center.y);
            polygonDrawer.lineTo(point1.x, point1.y);
            if(findCorner() >= 0){
                if(findCorner() < 9){
                    corner = cornerArray[findCorner()];
                    polygonDrawer.lineTo(corner.x, corner.y);
                }else{
                    int number = findCorner();
                    GamePoint c1 = cornerArray[((int)(number/10)) % 10];
                    GamePoint c2 = cornerArray[number % 10];
                    polygonDrawer.lineTo(c1.x, c1.y);
                    polygonDrawer.lineTo(c2.x, c2.y);
                }

            }
            polygonDrawer.lineTo(point2.x, point2.y);
            c.drawPath(polygonDrawer, polyPaint);

        }

        public int getRandomColor() {
            return (int) (Math.random() * COLOR_MAX);
        }

        protected int findCorner(){ //a 9 signals 2 corners (final 2 digits are the corners)
            if(point1.x == 0 && point2.y == 0){
                return 0;
            }else if(point1.y == 0 && point2.x == maxWidth){
                return 1;
            }else if(point1.x == maxWidth && point2.y == maxHeight){
                return 2;
            }else if(point1.y == maxHeight && point2.x ==0){
                return 3;
            }else if(point1.x == 0 && point2.x == maxWidth){
                 return 901;
            }else if(point1.x == maxWidth && point2.x == 0){
                 return 923;
            }else if(point1.y == 0 && point2.y == maxHeight){
                return 912;
            }else if(point1.y == maxHeight && point2.y == 0){
                return 930;
            }
            else{
                return -1;
            }
        }
    }

    private float width;
    private float height;

    private GamePoint center;
    private float deviation;
    private float centerSpeedX;
    private float centerSpeedY;

    private final float MIN_SPEED = BASE_SPEED / 3;
    private final float MAX_SPEED = BASE_SPEED * 3 / 2;

    private GamePoint[] lineLocations;
    private GamePoint[] cornerArray;

    private BackgroundPolygon[] polygonList = new BackgroundPolygon[4];



    public BackgroundScreen(float screenWidth, float screenHeight) {
        width = screenWidth;
        height = screenHeight;
        maxWidth = screenWidth;
        maxHeight = screenHeight;
        BASE_SPEED = screenHeight / 10;

        deviation = height / 10;

        center = new GamePoint(0, 0);
        centerSpeedX = BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED;
        centerSpeedY = BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED;
        moveCenter();

        GamePoint[] tempArray2 = {new GamePoint(0, 0), new GamePoint(width, 0), new GamePoint( width,  height), new GamePoint(0,  height)};
        cornerArray = tempArray2;

        GamePoint[] tempArray1 = {new GamePoint((width / 2), 0), new GamePoint( width, (height / 2)), new GamePoint( (width / 2),  height), new GamePoint(0,  (height / 2))};
        lineLocations = tempArray1;

        //System.out.println("W:" + width+ " H:" + height);

        for(int i = 0; i < polygonList.length; i++){
            if(i == 0){
                polygonList[i] = new BackgroundPolygon(lineLocations[3], lineLocations[0]);
            }else{
                polygonList[i] = new BackgroundPolygon( lineLocations[i-1], lineLocations[i]);
            }

        }
    }


    public void update(double elapsed) {
        updateCenter(elapsed);
        updatePolygons(elapsed);
    }

    protected void updatePolygons(double e) {
        for(int i = 0; i<polygonList.length; i++){
            polygonList[i].update(e);
        }
    }

    protected void updateCenter(double e) {
      // System.out.println("SPEEDX:" + centerSpeedX + " SPEEDY:" + centerSpeedY);
        center.x += ((e * centerSpeedX) +.5);
        center.y += ((e * centerSpeedY) + .5);
      // System.out.println("X:" + center.x + " Y:" + center.y);
       // System.out.println("ELAPSED: " + e);
        if (Math.abs(center.x - width / 2) > deviation) {
           // System.out.println("BOUNCE X");
           // System.out.println("ORIG SPEED:" + centerSpeedX);
            centerSpeedX *= -(.5 + Math.random());
           // System.out.println("NEW SPEED:" + centerSpeedX);
            if (center.x > width / 2) {
                center.x = (width / 2 + (deviation - 1));
            //    System.out.println("CENTER RESET EAST");
            } else {
                center.x =  (width / 2 - (deviation - 1));
            //    System.out.println("CENTER RESET WEST");
            }

        }
        if (Math.abs(center.y - height / 2) > deviation) {
          //  System.out.println("BOUNCE Y");
           // System.out.println("ORIG SPEED:" + centerSpeedY);
            centerSpeedY *= -(.5 + Math.random());
          //  System.out.println("NEW SPEED:" + centerSpeedY);
            if (center.y > height / 2) {
                center.y =  (height / 2 + (deviation - 1));
             //   System.out.println("CENTER RESET SOUTH");
            } else {
                center.y = (height / 2 - (deviation - 1));
             //   System.out.println("CENTER RESET NORTH");
            }
        }
        checkSpeeds();
    }

    protected void checkSpeeds() {
        if (Math.abs(centerSpeedX) < MIN_SPEED) {
            if (centerSpeedX > 0) {
                centerSpeedX = BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED;
            } else if (centerSpeedX < 0) {
                centerSpeedX = -(BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED);
            }
        }
        if (Math.abs(centerSpeedX) > MAX_SPEED) {
            if (centerSpeedX > 0) {
                centerSpeedX = BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED;
            } else if (centerSpeedX < 0) {
                centerSpeedX = -(BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED);
            }
        }
        if (Math.abs(centerSpeedY) < MIN_SPEED) {
            if (centerSpeedY > 0) {
                centerSpeedY = BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED;
            } else if (centerSpeedY < 0) {
                centerSpeedY = -(BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED);
            }
        }
        if (Math.abs(centerSpeedY) > MAX_SPEED) {
            if (centerSpeedY > 0) {
                centerSpeedY = BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED;
            } else if (centerSpeedY < 0) {
                centerSpeedY = -(BASE_SPEED / 2 + (float) Math.random() * BASE_SPEED);
            }
        }
    }

    public void draw(Canvas canvas) {
        //System.out.println("DRAW ALL POLYGONS");
        for (int i = 0; i < polygonList.length; i++) {
            polygonList[i].draw(canvas);
        }
    }

    public void moveCenter() {
        float centerX = (float) (((width / 2) - (deviation / 2)) + Math.random() * deviation);
        float centerY = (float) (((height / 2) - (deviation / 2)) + Math.random() * deviation);
        center.set(centerX, centerY);
    }

}
