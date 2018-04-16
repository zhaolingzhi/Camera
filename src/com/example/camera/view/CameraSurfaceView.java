package com.example.camera.view;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
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
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

public class CameraSurfaceView extends SurfaceView 
	implements SurfaceHolder.Callback,Camera.AutoFocusCallback,Camera.PreviewCallback{
	
	private Context mContext;
	private SurfaceHolder holder;
	private Camera mCamera;
	private int mScreenWidth;
	private int mScreenHeight;
	private double ratio;
	private DrawRect mDrawRect;
	private long lastTime;
//	private Handler mDetectHandler;
	private File externalpath;
	private Thread thread;

	private enum Tag {pic,prev;}
	
	public interface DrawRect{
		public void drawR(PointF p,double distance,double ratio);
	}
	
	private PictureCallback jpeg=new PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			BufferedOutputStream bos=null;
			Bitmap bm=null;
			try {
				bm=BitmapFactory.decodeByteArray(data, 0, data.length);
				bm = rotateToDegrees(bm, -90);
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
				{//�ⲿ�洢״̬
					String filePath=externalpath.getPath()+"/"+System.currentTimeMillis()+".jpeg";
					File file=new File(filePath);
					if(!file.exists()) {
						file.createNewFile();
					}
					bos=new BufferedOutputStream(new FileOutputStream(file));
					bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);

				}else {
					Toast.makeText(mContext, "û�м�⵽�ڴ濨",Toast.LENGTH_SHORT).show();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}finally { //�������Ҳ�����ִ�еĴ���
				try {
					bos.flush();
					bos.close();
					bm.recycle();
					mCamera.stopPreview();
					mCamera.startPreview();//���������Ԥ��
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}

	};
	public static Bitmap rotateToDegrees(Bitmap tmpBitmap, float degrees) {
	    Matrix matrix = new Matrix();
	    matrix.reset();
	    matrix.setRotate(degrees);
	    return Bitmap.createBitmap(tmpBitmap, 0, 0, tmpBitmap.getWidth(), tmpBitmap.getHeight(), matrix, true);
	}
	
	public CameraSurfaceView(Context context,AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub
		mContext=context;
		getScreenMetrix(context);  //��Ļ�ĳ���
		initView();
		lastTime=System.currentTimeMillis();
		externalpath=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
	}

	private void initView() {
		// TODO Auto-generated method stub
		holder=getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
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
			mCamera=Camera.open(1);
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
		mCamera.setPreviewCallbackWithBuffer(this);
		Parameters parameters=mCamera.getParameters();
		mCamera.addCallbackBuffer(new byte[parameters.getPreviewSize().width*parameters.getPreviewSize().height*ImageFormat.getBitsPerPixel(parameters.getPreviewFormat())/8]);
		mCamera.startPreview();
		
	}

	private void setCameraParams(Camera mCamera2, int width, int height) {
		// TODO Auto-generated method stub
		Parameters parameters=mCamera.getParameters();
		List<Size> pictureSizeList=parameters.getSupportedPictureSizes();
		Size picSize=getProperSize(pictureSizeList,(float)height/width,Tag.pic);
		if(picSize==null)
			picSize=parameters.getPictureSize();
		float w=picSize.width;
		float h=picSize.height;
		parameters.setPictureSize(picSize.width,picSize.height);
		this.setLayoutParams(new FrameLayout.LayoutParams((int)(height*(h/w)),height)); //???????
		
		Size prevSize=getProperSize(pictureSizeList,(float)height/width,Tag.prev); //Ԥ���ߴ����Ϊ��Ļ�ߴ�ı���
		if(prevSize==null)
			prevSize=parameters.getPictureSize();
		
		parameters.setPreviewSize(prevSize.width, prevSize.height);
		
		mCamera.setParameters(parameters);  //����趨����
		ratio=(double)mScreenHeight/prevSize.width;
		
		parameters.setJpegQuality(100);
		if(parameters.getSupportedFocusModes().contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
			parameters.setFocusMode((Parameters.FOCUS_MODE_CONTINUOUS_PICTURE));
		mCamera.cancelAutoFocus();
		mCamera.setDisplayOrientation(90); //????????/
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
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		mCamera.stopPreview();
		mCamera.release();
		mCamera=null;
		holder=null;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		// TODO Auto-generated method stub
		
	}
	
	public void setAutoFocus() {
		mCamera.autoFocus(this);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		if(System.currentTimeMillis()-lastTime<=500||data==null) {	
			mCamera.addCallbackBuffer(data);
			return;
		}
		thread=new Thread(new DetectThread(data,camera));
		thread.start();
		lastTime=System.currentTimeMillis();
		mCamera.addCallbackBuffer(data);
		return;
	}
	
	private Thread getDetectThread(byte[] data,Camera camera){
		thread=new Thread(new DetectThread(data,camera));
		return thread;
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
			Camera.Size preSize = mThCamera.getParameters().getPreviewSize();
			YuvImage mYuvImg=new YuvImage(mdata,mThCamera.getParameters().getPreviewFormat(),preSize.width,preSize.height,null);
			ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream(); 
			mYuvImg.compressToJpeg(new Rect(0,0,preSize.width,preSize.height), 100, mByteArrayOutputStream);
			byte[] mdata=mByteArrayOutputStream.toByteArray();
			
			BitmapFactory.Options bmInfo=new BitmapFactory.Options();
			bmInfo.inPreferredConfig=Bitmap.Config.RGB_565;
			
			bm=BitmapFactory.decodeByteArray(mdata, 0, mdata.length,bmInfo);
			bm = rotateToDegrees(bm, -90);
			
			
			FaceDetector fd=new FaceDetector(bm.getWidth(),bm.getHeight(),1);
			FaceDetector.Face[] arrayofFace=new FaceDetector.Face[1];
			if(fd.findFaces(bm, arrayofFace)>0){
				for(FaceDetector.Face face : arrayofFace) {
					PointF mMiddlePoint=new PointF();
					face.getMidPoint(mMiddlePoint);
					mDrawRect.drawR(mMiddlePoint, face.eyesDistance(),ratio);
				}
			}
		}
		
	}

	public void takePicture() {
		// TODO Auto-generated method stub
		mCamera.takePicture(null, null, jpeg);
	}
	
	public void setDrawRect(DrawRect mDrawRect) {
		this.mDrawRect=mDrawRect;
	}

}