package com.example.smartscreen;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageDetection
{
	private static final String TAG = "ImageDetection";
	protected ArrayList<DetectionInfo> mInfos ;
	protected OnDetectionListener mListener;
	protected double              mScaleFactor = 1.4;
	protected int                 mMinNeighbors = 3;
	public ImageDetection(ArrayList<DetectionInfo> infos)
	{
		mInfos = infos;
	}
	
	public void detection(final Mat src)
	{
		detection(src , mInfos , mScaleFactor , mMinNeighbors);
	}
	
	protected void detection(final Mat src , final ArrayList<DetectionInfo> infos , final double scaleFactor , final int neighbors)
	{
		new Thread()
    	{
    		public void run()
    		{
    			int index = -1;
    			int subIndex = -1;
    			MatOfRect targets = new MatOfRect();
    			MatOfRect subTargets = new MatOfRect();
    			for(int i = 0 ; i < infos.size() ; i++)
    			{
    				index = -1;
        			subIndex = -1;
    				DetectionInfo one = infos.get(i);
    				CascadeClassifier detector = one.getDetector();
    				
    				detector.detectMultiScale(src, targets , scaleFactor , neighbors , 2|1, one.getDectionSize() , new Size());
    				
    				//can't find any target
    				if(targets.empty())
    				{
    					continue;
    				}
    				Log.d(TAG , "detection find main target");
    				//need detect sub target , for example cctv 1 , cctv 2......
    				if(!one.bSingleDetection())
    				{
    					Log.d(TAG , "detection enter sub target");
    					int size = one.getSubDetectors().size();
    					for(int j = 0 ; j < size ; j++)
    					{
    						CascadeClassifier sub = one.getSubDetector(j);
    						Size min = one.getDectionSize();
    						sub.detectMultiScale(src, subTargets , scaleFactor , neighbors , 2|1, min , new Size());
    						if(subTargets.empty())
    						{
    							Log.d(TAG , "dont't find sub target");
    							continue;
    						}
    						else
    						{
    							Log.d(TAG , "find sub target");
    							//find sub target , exit from loop
    							index = i;
    							subIndex  = j;
    							break;//break inner loop
    						}
    					}
    					
    					if(subIndex != -1)
    					{
    						Log.d(TAG , "find main index: "+index + " " + "sub index: " + subIndex);
    						break;//break out loop
    					}
    				}
    				else
    				{
    					index = i ;
    					break;
    				}
    			}
    			
    			//detection succ
    			Message msg = Message.obtain();
    			if(index  != -1)
    			{
    				//fill result 
    				DetectInfoResult result = new DetectInfoResult();
    				result.setIdx(index);
    				result.setSubIdx(subIndex);
    				result.setRect(targets);
    				msg.what = 0;
    				msg.obj = result;
    			}
    			else
    			{
    				msg.what = 1;
    			}
    	
    			//sync to main thread
    			mHandler.sendMessage(msg);
    		}
    	}.start();
	}
	
	public void setOnDetectionListener(OnDetectionListener l)
	{
		mListener = l;
	}
	
	
	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case 0:
				onDetectionSucc((DetectInfoResult)msg.obj);
				break;
			case 1:
				onDetectionFail();
				break;
			}
		}
	};
	
	protected void onDetectionSucc(DetectInfoResult result)
	{
		Log.d(TAG , "onDetectionSucc");
		if(mListener != null)
		{
			mListener.onDetectionCallback(mInfos.get(result.mIdx), result);
		}
	}
	
	private void onDetectionFail()
	{
		Log.d(TAG , "onDetectionFail");
		if(mListener != null)
		{
			mListener.onDetectionFailCallback();
		}
	}
	
	
	public interface OnDetectionListener
	{
		public void onDetectionCallback(DetectionInfo info , DetectInfoResult result);
		public void onDetectionFailCallback();
	}
	

}
