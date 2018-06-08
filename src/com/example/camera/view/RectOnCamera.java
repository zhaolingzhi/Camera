package com.example.camera.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class RectOnCamera extends View{
	private int genderNo;
	private int ageNo;
	private Paint rectPaint;
	private Paint namePaint;
	private PointF centerPoint;
	private double distance;
	private static String genders[]= {"","man","woman"};
	private static String ages[]= {"","0~3","4~7","8~14","15~22","23~32","33~45","46~59","60~"};
	

	public RectOnCamera(Context context,AttributeSet attr) {
		super(context,attr);
		// TODO Auto-generated constructor stub
		initView(context);
	}
	
	private void initView(Context context) {
		// TODO Auto-generated method stub
		rectPaint=new Paint();
		rectPaint.setAntiAlias(true);
		rectPaint.setDither(true);
		rectPaint.setColor(Color.RED);
		rectPaint.setStyle(Style.STROKE);
		rectPaint.setStrokeWidth(5);
		
		namePaint=new Paint();
		namePaint.setAntiAlias(true);
		namePaint.setDither(true);
		namePaint.setColor(Color.RED);
		namePaint.setTextSize(100);
		namePaint.setTypeface(Typeface.DEFAULT);
		
		centerPoint=new PointF(0,0);
		distance=0.0;
		genderNo=0;
		ageNo=0;
	}

	
	public void setRectParam(PointF point,double dis,double ratio) {
		centerPoint.x=(float)(point.x*ratio);
		centerPoint.y=(float)(point.y*ratio);
		distance=dis*ratio*1.8;
		postInvalidate();
	}
	
	public void setPersonInfo(String gender,String age) {
		genderNo=Integer.parseInt(gender);
		ageNo=Integer.parseInt(age);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawRect(new RectF((int)(centerPoint.x-distance),
								  (int)(centerPoint.y-distance),
								  (int)(centerPoint.x+distance),
								  (int)(centerPoint.y+distance)), rectPaint);
		
		canvas.drawText(genders[genderNo]+" "+ages[ageNo], (int)(centerPoint.x-distance), 
				(int)(centerPoint.y-distance), namePaint);	
	}
	
}
