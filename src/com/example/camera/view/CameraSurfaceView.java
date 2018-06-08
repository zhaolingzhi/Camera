package com.example.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.camera.utils.Internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.opengl.GLES20;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.format.Time;

public class CameraSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback,Camera.PreviewCallback{
	
	private Context mContext;
	private SurfaceHolder holder;
	private Camera mCamera;
	private int mScreenWidth;
	private int mScreenHeight;
	private double ratio;
	private DrawRect mDrawRect;
	private long lastTime;
	private File externalpath;
	private Thread thread;
	private Internet mInternet;

	private enum Tag {pic,prev;}
	
	public interface DrawRect{
		public void drawR(PointF p,double distance,double ratio);
	}
	
	public CameraSurfaceView(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		initView();
		mContext=context;
		getScreenMetrix(context);  //屏幕的长宽
	}
	private void initView() {
		// TODO Auto-generated method stub
		holder=getHolder();
		holder.addCallback(this);
		lastTime=System.currentTimeMillis();
		externalpath=Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES);
		mInternet=new Internet();
	}



	private void getScreenMetrix(Context context) {
		// TODO Auto-generated method stub
		WindowManager WM=(WindowManager)context.getSystemService(Context.WINDOW_SERVICE); 
		DisplayMetrics outMetrics=new DisplayMetrics();
		WM.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth=outMetrics.widthPixels;
		mScreenHeight=outMetrics.heightPixels;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if(mCamera==null) {
			mCamera=Camera.open(1);//打开前置摄像头
			try {
				mCamera.setPreviewDisplay(holder);
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		setCameraParams(mCamera,mScreenWidth,mScreenHeight);
		setBuffer();
		mCamera.startPreview();
	}
	private void setBuffer() {
		// TODO Auto-generated method stub
		mCamera.setPreviewCallbackWithBuffer(this);
		Parameters parameters=mCamera.getParameters();
		mCamera.addCallbackBuffer(
				new byte[parameters.getPreviewSize().width*parameters.getPreviewSize().height
				         *ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8]);
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera.stopPreview();
		mCamera.release();
		mCamera=null;
		holder=null;
	}

	private void setCameraParams(Camera mCamera2, int width, int height) {
		// TODO Auto-generated method stub
		Parameters parameters=mCamera.getParameters();
		List<Size> pictureSizeList=parameters.getSupportedPictureSizes();//获取支持的照片尺寸
		Size picSize=getProperSize(pictureSizeList,(float)height/width,Tag.pic);
		if(picSize==null)
			picSize=parameters.getPictureSize();
		parameters.setPictureSize(picSize.width,picSize.height);
		//预览尺寸必须为屏幕尺寸的倍数
		Size prevSize=getProperSize(pictureSizeList,(float)height/width,Tag.prev); 
		if(prevSize==null)
			prevSize=parameters.getPictureSize();
		parameters.setPreviewSize(prevSize.width, prevSize.height);
		mCamera.setParameters(parameters);  //相机设定参数
		ratio=(double)mScreenHeight/prevSize.width;
		parameters.setJpegQuality(100);
		mCamera.setDisplayOrientation(90); //默认横屏显示，并强行拉伸
	}

	private Size getProperSize(List<Size> pictureSizeList, float screenRatio,Tag tag) {
		// TODO Auto-generated method stub
		Size result=null;
		switch(tag) {
		case pic:
			for(Size size:pictureSizeList) {
				float currentRatio=(float)size.width/size.height;
				if(Math.abs(currentRatio-screenRatio)<0.00001) {
					result=size;
					break;
				}
			}
			if(result==null) {
				for(Size size:pictureSizeList) {
					float curRatio=(float)size.width/size.height;
					if(Math.abs(curRatio-4f/3)<0.00001) {
						result=size;
						break;
					}
				}
			}
			break;
		case prev:
			for(Size size:pictureSizeList) {
				float currentRatio=(float)size.width/size.height;
				if(size.width<1000&&Math.abs(currentRatio-screenRatio)<0.00001) {
					result=size;
					break;
				}
			}
		}	
		return result;
	}




	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		
		// TODO Auto-generated method stub
		if(System.currentTimeMillis()-lastTime<=500) {	
			mCamera.addCallbackBuffer(data);
			return;
		}
		
		mCamera.addCallbackBuffer(data);
		lastTime=System.currentTimeMillis();
		thread=new Thread(new DetectThread(data,camera));
		thread.start();
		return;
	}

	private class DetectThread implements Runnable{
		private byte[] mdata;
		private Camera mThCamera;
		
		public DetectThread(byte[] data,Camera camera) { 
			mdata=data;
			mThCamera=camera;
		} 

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Bitmap bm=null;
			Size preSize = mThCamera.getParameters().getPreviewSize();
			YuvImage mYuvImg=new YuvImage(mdata,mThCamera.getParameters().getPreviewFormat(),
					preSize.width,preSize.height,null);
			ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream(); 
			mYuvImg.compressToJpeg(new Rect(0,0,preSize.width,preSize.height), 
					100, mByteArrayOutputStream);
			byte[] mdata=mByteArrayOutputStream.toByteArray();
			BitmapFactory.Options bmInfo=new BitmapFactory.Options();
			bmInfo.inPreferredConfig=Bitmap.Config.RGB_565;
			bm=BitmapFactory.decodeByteArray(mdata, 0, mdata.length,bmInfo);
			bm = BitmapRotate(bm);
			
			FaceDetector fd=new FaceDetector(bm.getWidth(),bm.getHeight(),1);
			FaceDetector.Face[] arrayofFace=new FaceDetector.Face[1];
			if(fd.findFaces(bm, arrayofFace)>0){
				for(FaceDetector.Face face : arrayofFace) {
					PointF mMiddlePoint=new PointF();
					face.getMidPoint(mMiddlePoint);
					float distance=face.eyesDistance();
					mDrawRect.drawR(mMiddlePoint, distance,ratio);
					distance=(float) (distance*1.8);
					int x=(int)(mMiddlePoint.x-distance);
					int y=(int)(mMiddlePoint.y-distance);
					int len=(int)(2.0*distance);
					if(x>0&&y>0&&x+len<preSize.height&&y+len<preSize.width) {
						bm=Bitmap.createBitmap(bm,x,y,len,len,null,true);
						mInternet.sendImage(bm);
					}
				}
			}else {
				mDrawRect.drawR(new PointF(0,0), 0, ratio);
			}
		}
	}
	private PictureCallback jpeg=new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
			BufferedOutputStream bos=null;
			Bitmap bm=null;
			try {
				bm=BitmapFactory.decodeByteArray(data, 0, data.length);
				bm = BitmapRotate(bm);
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{//外部存储状态
					String filePath=externalpath.getPath()+"/"+System.currentTimeMillis()+".jpeg";
					File file=new File(filePath);
					if(!file.exists()) {
						file.createNewFile();
					}
					bos=new BufferedOutputStream(new FileOutputStream(file));
					bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

				}else {
					Toast.makeText(mContext, "没有检测到内存卡",Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}finally { //出错后会也会继续执行的代码
				try {
					bos.flush();
					bos.close();
					bm.recycle();
					mCamera.stopPreview();
					setBuffer();
			        mCamera.startPreview();
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

	};
	
	public Bitmap BitmapRotate(Bitmap bm) {
		// TODO Auto-generated method stub
		Matrix matrix = new Matrix();
	    matrix.reset();
	    matrix.postRotate(-90);
	    matrix.postScale(-1, 1);
	    return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
	}

	public void takePicture() {
		// TODO Auto-generated method stub
		mCamera.takePicture(null, null, jpeg);
	}
	
	public void setDrawRect(DrawRect mDrawRect) {
		this.mDrawRect=mDrawRect;
	}

}
