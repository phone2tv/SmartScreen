package com.example.smartscreen;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

public class DetectionInfo
{
	public boolean mSingleDetection ;
	public ArrayList<CascadeClassifier> mClassifilers;
	public ArrayList<Integer>           mIds;
	public ArrayList<Size>              mSizes;
	public String                       mName;
	DetectionInfo()
	{
		mSingleDetection = true;
		mIds = new ArrayList<Integer>();
		mSizes = new ArrayList<Size>();
		mClassifilers = new ArrayList<CascadeClassifier>();
	}
	
	public boolean bSingleDetection()
	{
		return mSingleDetection;
	}
	
	
	public CascadeClassifier getDetector()
	{
		return mClassifilers.get(0);
	}
	
	public Size getDectionSize()
	{
		return mSizes.get(0);
	}
	
	public ArrayList<CascadeClassifier> getSubDetectors()
	{
		ArrayList<CascadeClassifier> subs = new ArrayList<CascadeClassifier>();
		List<CascadeClassifier> subList = mClassifilers.subList(1, mClassifilers.size());
		subs.addAll(subList);
		return subs;
	}
	
	public CascadeClassifier getSubDetector(int index)
	{
		return mClassifilers.get(index + 1);
	}
	
	public int getId()
	{
		return mIds.get(0);
	}
	
	public Size getSubSize(int index)
	{
		return mSizes.get(index + 1);
	}
	
	public String getName()
	{
		return mName;
	}
}
