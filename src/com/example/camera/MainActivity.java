package com.example.camera;

import com.example.camera.view.CameraSurfaceView;
import com.example.camera.view.RectOnCamera;
import com.example.camera.utils.Internet;

import android.app.Activity;
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
	implements View.OnClickListener,RectOnCamera.IAutoFocus ,Internet.Info,CameraSurfaceView.DrawRect{
	
	private CameraSurfaceView mCameraSurfaceView;
	private RectOnCamera mRectOnCamera;
	private Internet internet;
	private Button takePicBtn;
	private boolean isClicked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			requestWindowFeature(Window.FEATURE_NO_TITLE);//无标题
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
			setContentView(R.layout.real_time);
		}catch(Exception e) {
			Log.e("error","error in code:"+e.toString());
		}

		mCameraSurfaceView=(CameraSurfaceView)findViewById(R.id.cameraSurfaceView);
		mRectOnCamera=(RectOnCamera)findViewById(R.id.rectOnCamera);
		takePicBtn=(Button)findViewById(R.id.qiezi);
		mRectOnCamera.setIAutoFocus(this);
		mCameraSurfaceView.setDrawRect(this);
		takePicBtn.setOnClickListener(this);
	}

//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.real_time, menu);
//		return true;
//	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public void autoFocus() {   //接口函数的重写
		// TODO Auto-generated method stub
		mCameraSurfaceView.setAutoFocus();
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
	public void setInfo(String sex, String age) {
		// TODO Auto-generated method stub
		mRectOnCamera.setPersonInfo(sex, age);
	}

}
