package com.example.smartscreen;

import org.opencv.core.MatOfRect;

public class DetectInfoResult
{
	public int mIdx;
	public int mSubIdx ;
	public MatOfRect target;
	
	DetectInfoResult()
	{
		target = new MatOfRect();
		mSubIdx  = -1;
		mIdx = -1;
	}
	
	public void setIdx(int idx)
	{
		mIdx = idx;
	}
	
	public void setSubIdx(int idx)
	{
		mSubIdx = idx;
	}
	
	public void setRect(MatOfRect rect)
	{
		target = rect;
	}
	
}
