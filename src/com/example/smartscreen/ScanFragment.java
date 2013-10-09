package com.example.smartscreen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class ScanFragment extends SherlockFragment implements SurfaceHolder.Callback, PreviewCallback , OnClickListener , ImageDetection.OnDetectionListener
{
	 private static final String TAG = "ScanFragment";
	 private Camera mCamera;
	 private SurfaceView mSurface;
	 private SurfaceHolder  mHolder;
	 private Mat mFrameBuffer;
	 private int mFrameHeight;
	 private int mFrameWidth;
	 private ImageView mZoomImg;
	 private ImageDetection mDetection;
	 private boolean mDetecting;
	 private SeekBar mZoomBar;
	 private View    mHintFrame;
	 private Animation mLeftMove ;
	 private TextView mProgramName;
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	 {
		View rootView = inflater.inflate(R.layout.scan, container, false); 
		mSurface = (SurfaceView)rootView.findViewById(R.id.surface_view);
		
		mZoomImg = (ImageView)rootView.findViewById(R.id.zoom_img);
		mZoomImg.setOnClickListener(this);
		
		mHolder  = mSurface.getHolder();
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS); 
		mHolder.addCallback(this);
		
		mHintFrame = rootView.findViewById(R.id.left_frame);
		mHintFrame.setOnClickListener(this);
		mLeftMove = AnimationUtils.loadAnimation(getActivity(), R.anim.leftframe);
		
		mZoomBar = (SeekBar)rootView.findViewById(R.id.seekBar1);
		
		mProgramName = (TextView)rootView.findViewById(R.id.text_current_program);
		mDetecting = false;
		return rootView; 
	 }

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{
		// TODO Auto-generated method stub
		 mCamera.startPreview();  
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0)
	{
		// TODO Auto-generated method stub
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, getActivity(), mLoaderCallback);
		mCamera=Camera.open();  
		Camera.Parameters parameters=mCamera.getParameters(); 
		
		
		List<android.hardware.Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		//if portrait frameSize , width is less than height 
		Size frameSize = calculateCameraFrameSize(sizes , mSurface.getWidth() , mSurface.getHeight() , true);
		
		parameters.setPreviewSize((int)frameSize.height, (int)frameSize.width);  
		parameters.set("orientation", "portrait");
		parameters.setPreviewFormat(ImageFormat.NV21);
		
		mFrameWidth = (int)frameSize.width;
		mFrameHeight = (int)frameSize.height;
		
		mCamera.setDisplayOrientation(90);
		
		try
		{
			mCamera.setPreviewDisplay(mHolder);  
			mCamera.setParameters(parameters);  
			mCamera.setPreviewCallback(this);
		}
		catch(Exception e)
		{
			
		}
		
		mZoomBar.setMax(mCamera.getParameters().getMaxZoom());
		mZoomBar.setProgress(mCamera.getParameters().getZoom());
		mZoomBar.setOnSeekBarChangeListener(new  OnSeekBarChangeListener()
        {

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1,
					boolean arg2)
			{
				// TODO Auto-generated method stub
				//modify zoom 
				Camera.Parameters params = mCamera.getParameters();
		    	params.setZoom(arg1);
		    	mCamera.setParameters(params);
				
		    	mZoomBar.setProgress(arg1);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
        	
        });
	
		mCamera.startPreview();  
		mCamera.autoFocus(mAutoFocusCallback);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		// TODO Auto-generated method stub
		mDetection.setOnDetectionListener(null);
		mCamera.setPreviewCallback(null);
		mCamera.stopPreview();  
	    mCamera.release();  
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera)
	{
		if(mDetecting == true)
			return;
		
		mDetecting = true;
		
		mFrameBuffer.put(0, 0 , data);
		Message msg = Message.obtain();
		msg.obj = mFrameBuffer.submat(0, mFrameWidth, 0, mFrameHeight);
		mHandler.sendMessage(msg);
	}
	
	private Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Mat sub = (Mat)msg.obj;
			Mat dst = rotateImage(sub , -90);
			Mat roi = buildROI(dst);
			mDetection.detection(roi);
		}
	};
	
	@Override
	public void onClick(View arg0)
	{
		// TODO Auto-generated method stub
		switch(arg0.getId())
		{
		case R.id.zoom_img:
			mCamera.autoFocus(mAutoFocusCallback);
			mZoomImg.setBackgroundResource(R.drawable.capture);
			break;
		case R.id.left_frame:
			startWebActivity("http://wafm.sinosns.cn/");
			break;
		}
	}
	
	 private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) 
	 {
	    	
	        @Override
	        public void onManagerConnected(int status) {
	            switch (status) {
	                case LoaderCallbackInterface.SUCCESS:
	                {
	                    Log.i(TAG, "OpenCV loaded successfully");
	                    mFrameBuffer = new Mat(mFrameWidth + (mFrameWidth/2), mFrameHeight, CvType.CV_8UC1);
	                    initDetection();
	                } break;
	                default:
	                {
	                    super.onManagerConnected(status);
	                } break;
	            }
	        }
	 };
	 
	 private void initDetection()
	 { 
		 ArrayList<DetectionInfo> array = new ArrayList<DetectionInfo>();
		 //String tvName , int tvIdx , int tvRes , String fileName , Size size
		 array.add(createDetectionInfo("东方卫视" , 0 , R.raw.dfwscascade , "dfwsCascade" , new Size(36,36)));
		 array.add(createDetectionInfo("江苏卫视" , 1 , R.raw.jswscascade , "jswsCascade"));
		 array.add(createDetectionInfo("浙江卫视" , 3 , R.raw.zjwscascade , "zjwsCascade"));
		 array.add(createDetectionInfo("天津卫视" , 4 , R.raw.tjwscascade , "tjwsCascade"));
		 array.add(createDetectionInfo("山东卫视" , 5 , R.raw.sdwscascade , "sdwsCascade"));
		 array.add(createDetectionInfo("安徽卫视" , 6 , R.raw.ahwscascade , "ahwsCascade"));
		 
		 DetectionInfo info = createDetectionInfo("湖南卫视" , 2 , R.raw.hnwscascade , "hnwsCascade");
		 info.mClassifilers.add(getCascadeClassifier(R.raw.hnwscascade_sub , "hnwsCascade_sub"));
		 info.mSingleDetection = false;
		 array.add(info);
		 
		 mDetection = new ImageDetection(array);
		 mDetection.setOnDetectionListener(this);
	}
	 
	 private CascadeClassifier getCascadeClassifier(int id , String fileName)
	 {
		 try
 		{
 			InputStream is = getResources().openRawResource(id);
 			File cascadeDir = getActivity().getDir("cascade", Context.MODE_PRIVATE);
 			File cascadeFile = new File(cascadeDir, fileName);
 			FileOutputStream os = new FileOutputStream(cascadeFile);

 			byte[] buffer = new byte[4096];
 			int bytesRead;
 			while ((bytesRead = is.read(buffer)) != -1) {
 				os.write(buffer, 0, bytesRead);
 			}
 			is.close();
 			os.close();
 			
 			Log.d(TAG , "file :" + cascadeFile.getAbsolutePath());
 			CascadeClassifier cascade  = new CascadeClassifier(cascadeFile.getAbsolutePath());
            cascadeDir.delete();
            return cascade;
 		}
 		catch(IOException e) 
 		{
             e.printStackTrace();
             Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
        }
		 
		return null;
	 }
	 
	 private Mat rotateImage(Mat src , double angle)
	 {
		 Point center = new Point(src.cols()/2, src.rows()/2);
		 double scale = 1.0;
		 
		 
		 Mat rotateMat = Imgproc.getRotationMatrix2D(center , angle , scale);
		 Mat dst = new Mat();
		 Size frameSize = new Size(src.rows() , src.cols());
		 
		 //adjust dst center point
		 double m[] = new double[6];
		 rotateMat.get(0, 0 , m);
		 m[2] += (frameSize.width - src.cols())/2;
		 m[5] += (frameSize.height - src.rows())/2;
		 rotateMat.put(0 , 0 , m);
		 
		 Imgproc.warpAffine(src , dst , rotateMat , frameSize , Imgproc.INTER_LINEAR , Imgproc.BORDER_CONSTANT ,Scalar.all(0));
		 
		 return dst;
	 }
	 
	 private Size calculateCameraFrameSize(List<Camera.Size> supportedSizes , int surfaceWidth , int surfaceHeight , boolean bPortrail)
	 {
		 
		 Size size = new Size();
		 int calcWidth = 0;
		 int calcHeight = 0;
		 for(Camera.Size obj : supportedSizes)
		 {
			 if(obj.height <= surfaceWidth && obj.width <= surfaceHeight)
			 {
				 if(obj.height >= calcWidth && obj.width >= calcHeight)
				 {
					 calcWidth = obj.height;
					 calcHeight = obj.width;
				 }
			 }
		 }
		 size.height = calcHeight;
		 size.width  = calcWidth;
		 return size;
	 }
	 
	 private Mat buildROI(Mat src)
	 {
		 int offset = mZoomImg.getWidth();
		 int rows = src.rows();
	     int cols = src.cols();
	     Mat sub = src.submat(rows/2 - offset/2 , rows/2 + offset/2, cols/2 - offset/2 , cols/2 + offset/2);
	     return sub;
	 }


	
	private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback()
	{
		 public void onAutoFocus(boolean success, Camera camera) 
		 {
			 mCamera.autoFocus(null);
			 mDetecting = false;
		 }
	};
	
	@Override
	public void onDetectionCallback(DetectionInfo info, DetectInfoResult result)
	{
		Log.d(TAG , "onDetectionCallback"+ result.mSubIdx);
		mZoomImg.setBackgroundResource(R.drawable.capture_b);
		mHintFrame.setVisibility(View.VISIBLE);
		mHintFrame.startAnimation(mLeftMove);
		mProgramName.setText(info.getName());
	}
	
	//if detecting fail , continue to detect
	@Override
	public void onDetectionFailCallback()
	{
		Log.d(TAG , "onDetectionFailCallback");
		mCamera.autoFocus(mAutoFocusCallback);
	}
	
	protected void startWebActivity(String url)
	{
		Intent intent = new Intent();
		intent.putExtra("url", url);
		intent.setClass(getActivity(), WebActivity.class);
		startActivity(intent);
	}
	
	public DetectionInfo createDetectionInfo(String tvName , int tvIdx , int tvRes , String fileName )
	{
		Size size = new Size(24 , 24);
		return createDetectionInfo(tvName , tvIdx , tvRes , fileName , size);
	}
	
	public DetectionInfo createDetectionInfo(String tvName , int tvIdx , int tvRes , String fileName , Size size)
	{
		DetectionInfo info = new DetectionInfo();
		info.mSingleDetection = true;
		info.mName = tvName;
		info.mSizes.add(size);
		info.mIds.add(tvIdx);
		info.mClassifilers.add(getCascadeClassifier(tvRes , fileName));
		return info;
	}
	
	

}
