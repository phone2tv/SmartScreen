package com.example.smartscreen;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DoubanAdapter
{
	public static final String TAG = "DoubanAdapter";
	public static final String mCmdAddress = "https://api.douban.com/v2/movie/";
	public static final String mCommandStr[] =
		{ "search?q=",
		  "subject/"
		};
	public OnDoubanCallbackListener mListener;
	private Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case 0:
				if(mListener != null)
					mListener.onDoubanCallbackSucc((Program)msg.obj);
				break;
			case 1:
				if(mListener != null)
					mListener.onDoubanCallbackFail();
				break;
			}
		}
	};

	
	@SuppressLint("NewApi")
	public int queryProgramId(final String q) throws Exception
	{
		int idx = -1;
		String url = mCommandStr[0] + URLEncoder.encode(q ,"UTF-8");
	
			String str = requestUrl("GET" , url);
			JSONObject result = new JSONObject(str);
			JSONArray subjects = result.getJSONArray("subjects");
			JSONObject subject = null;
			if(subjects.length() != 0)
			{
				 subject = subjects.getJSONObject(0);
				 idx = subject.getInt("id");
			}

		
		return idx;
	}
	
	public JSONObject requestProgramById(int idx) throws Exception
	{
		if(idx == -1)
			return null;
		
		JSONObject  json = null;
		String url = mCommandStr[1] + String.valueOf(idx); 
		String str = requestUrl("GET" , url);
		json =  new JSONObject(str);
		
		return json;
	}
	
	
	public Program getProgramByJson(JSONObject obj) throws Exception 
	{
		Program program = new Program();

		program.mDescription = obj.getString("summary");
		program.mDiscussCount = obj.getInt("reviews_count");
		program.mUrl          = obj.getString("mobile_url");
		JSONObject json = obj.getJSONObject("rating");
		program.mRating = (float)json.getDouble("average");
		          
		return program;
	}
	
	public void setListener(OnDoubanCallbackListener l)
	{
		mListener = l;
	}
	
	public interface OnDoubanCallbackListener
	{
		public void onDoubanCallbackSucc(Program program);
		public void onDoubanCallbackFail();
	}
	
	
	public String requestUrl(String method , String url ) throws Exception
	{
		String cmdStr = mCmdAddress  + url;
		Log.d(TAG , cmdStr);
		try
		{
			URL targetUrl = new URL(cmdStr);
			HttpURLConnection conn = (HttpURLConnection) targetUrl.openConnection(); 
			conn.setDoInput(true);
			conn.setRequestMethod(method);
			conn.connect();
			InputStream is = conn.getInputStream();  
			byte[] data = readStream(is);
			String jsonStr = new String(data);
			return jsonStr;
		}
		
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	protected byte[] readStream(InputStream is) throws Exception
	{
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream(); 
		
		try
		{
		
			int ch;  
			while ((ch = is.read()) != -1) 
			{  
				bytestream.write(ch);  
			}  
			
			bytestream.close();  
			byte imgdata[] = bytestream.toByteArray();  
			return imgdata;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw e;
		}
	}
	
	public void getDoubanProgramInfo(final String q)
	{
		new Thread()
		{
			public void run()
			{
				Message msg = Message.obtain();
				msg.what = 1;
				try
				{
					
					int idx = queryProgramId(q);
					if(idx != -1)
					{
						Program program = getProgramByJson(requestProgramById(idx));
						msg.what = 0;
						msg.obj = program;
					}
					
				}
				catch(Exception e)
				{
					Log.d(TAG , "getDoubanProgramInfo");
				}
				mHandler.sendMessage(msg);
			}
		}.start();
	}
}
