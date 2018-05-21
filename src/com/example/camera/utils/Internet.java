package com.example.camera.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Base64;

public class Internet {
	private static final String end="END" ;
	private String picture;
	private static Info minfo;
	
	public interface Info{
		void setInfo(String gender,String age);
	}
	
	public static void setInternet(Info info) {
		minfo=info;	
	}
	

	public void sendImage(Bitmap bitmap) {
	    bitmap=BitmapCompress(bitmap);
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		picture=Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
		Thread submit=new Thread(new SubmitThread());
		submit.start();
	}
	
	public Bitmap BitmapCompress(Bitmap bitmap) {
		float scaleWidth = ((float) 227) / bitmap.getWidth();  
	    float scaleHeight = ((float) 227) / bitmap.getHeight();  
	    Matrix matrix = new Matrix();  
	    matrix.reset();
	    matrix.postScale(scaleWidth, scaleHeight);  
	    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix,  
	      true); 
	}
	
	class SubmitThread implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub	
			JSONObject object=new JSONObject();
			try {
				object.put("image", picture);
				server(object);
			}catch(JSONException e) {
				e.printStackTrace();
			}
		}
	}

	public int server(JSONObject object) {
		String ip="192.168.1.100";
		int port=8080;
		try {
			Socket s=new Socket();  
			SocketAddress socAddress = new InetSocketAddress(ip, port); 
			s.connect(socAddress,5000);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            String temp=String.valueOf(object);
            out.write(String.valueOf(object));
            out.write(end);
            out.flush();
            
            BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String result;
            result=in.readLine();
            in.close();
            s.close();
            JSONObject rInfo=new JSONObject(result);
            String gender=rInfo.getString("gender");
            String age=rInfo.getString("age");
			minfo.setInfo(gender, age);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
