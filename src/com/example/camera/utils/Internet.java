package com.example.camera.utils;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.util.Base64;

public class Internet {
	private String picture;
	
	public void sendImage(Bitmap bitmap) {
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		picture=Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		
	}
	
	class SubmitThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
