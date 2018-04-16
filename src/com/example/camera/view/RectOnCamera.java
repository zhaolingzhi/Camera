package com.example.camera.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class RectOnCamera extends View{
	private int mScreenWidth;
	private int mScreenHeight;
	private Paint mPaint;
	private RectF mRectF;
	private PointF centerPoint;
	private double distance;
	private IAutoFocus mIAutoFocus;
	private Canvas canvas;

	public interface IAutoFocus{
		void autoFocus();
	}

	public RectOnCamera(Context context,AttributeSet attr) {
		super(context,attr);
		// TODO Auto-generated constructor stub
		getScreenMetrix(context);
		initView(context);
	}
	
	private void initView(Context context) {
		// TODO Auto-generated method stub
		mPaint=new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(5);
		centerPoint=new PointF(mScreenWidth/2,mScreenHeight/2);
		distance= 0.0;

	}

	private void getScreenMetrix(Context context) {
		// TODO Auto-generated method stub
		WindowManager WM=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE); 
		DisplayMetrics outMetrics=new DisplayMetrics();
		WM.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth=outMetrics.widthPixels;
		mScreenHeight=outMetrics.heightPixels;
	}
	
	public void Draw(PointF point,double dis,double ratio) {
		centerPoint.x=(float)mScreenWidth-(float)(point.x*ratio);
		centerPoint.y=(float)(point.y*ratio);
		distance=dis*ratio*1.8;
		postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setColor(Color.WHITE);
		mPaint.setColor(Color.RED);
		canvas.drawRect(new RectF((int)(centerPoint.x-distance),
								  (int)(centerPoint.y-distance),
								  (int)(centerPoint.x+distance),
								  (int)(centerPoint.y+distance)), mPaint);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_UP:
			
			if(mIAutoFocus!=null) {
				mIAutoFocus.autoFocus();//->main.autoFoce
			}
			return true;
		}
		return false;
		
	}
	
		
	public void setIAutoFocus(IAutoFocus mIAutoFocus) {
		this.mIAutoFocus=mIAutoFocus;
	}
	
}
