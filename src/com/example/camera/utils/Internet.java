package com.example.camera.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Base64;

public class Internet {
	private String picture;
	private Info info;
	
	public interface Info{
		void setInfo(String sex,String age);
	}
	
	public void sendImage(Bitmap bitmap) {
	
		ByteArrayOutputStream baos=new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		picture=Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
//		Thread submit=new Thread(new SubmitThread());
//		submit.start();
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
		// TODO Auto-generated method stub
		String path="";
		try {
			URL url=new URL(path);
			String content=String.valueOf(object);
			HttpURLConnection conn=(HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(1000);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "Fiddler");
			conn.setRequestProperty("Content-Type", "application/json");
			OutputStream os=conn.getOutputStream();
			os.write(content.getBytes()); //内容写到输出流
			os.close();
			if(conn.getResponseCode()==200) {
				BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
				String result;
				result=in.readLine();
				in.close();
				JSONObject rInfo=new JSONObject(result);
				info.setInfo(rInfo.getString("sex"), rInfo.getString("age"));
			}
		}catch(MalformedURLException e) {
			e.printStackTrace();
		}catch(IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
