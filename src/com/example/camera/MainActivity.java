package com.example.camera;

import com.example.camera.view.CameraSurfaceView;
import com.example.camera.view.RectOnCamera;
import com.example.camera.utils.Internet;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class MainActivity extends Activity 
	implements View.OnClickListener,Internet.Info,CameraSurfaceView.DrawRect{
	
	private CameraSurfaceView mCameraSurfaceView;
	private RectOnCamera mRectOnCamera;
	private Button takePicBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.real_time);


		mCameraSurfaceView=(CameraSurfaceView)findViewById(R.id.cameraSurfaceView);
		mRectOnCamera=(RectOnCamera)findViewById(R.id.rectOnCamera);
		takePicBtn=(Button)findViewById(R.id.qiezi);
		mCameraSurfaceView.setDrawRect(this);
		Internet.setInternet(this);
		takePicBtn.setOnClickListener(this);
		takePicBtn.getBackground().setAlpha(100);
	}

	
	@Override
	public void drawR(PointF p, double distance, double ratio) {
		// TODO Auto-generated method stub
		mRectOnCamera.setRectParam(p, distance,ratio);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.qiezi:
			mCameraSurfaceView.takePicture();
			break;
		}
	}

	@Override
	public void setInfo(String gender, String age) {
		// TODO Auto-generated method stub
		mRectOnCamera.setPersonInfo(gender, age);
	}

}
