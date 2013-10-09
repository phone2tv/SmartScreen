package com.example.smartscreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.example.smartscreen.DoubanAdapter.OnDoubanCallbackListener;

public class ProgramDetailActivity extends SherlockActivity implements OnClickListener , OnDoubanCallbackListener
{
	public static String TAG="ProgramDetailActivity";
	public static int THEME = R.style.Theme_Sherlock;
	public ImageView mCommentBt;
	private DoubanAdapter mDouban;
	private TextView  mProgramDetail;
	private TextView  mDoubanRating;
	private ImageView mMoreBt;
	private Program   mProgram;
	protected void onCreate(Bundle savedInstanceState)
	{
		setTheme(THEME); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program_detail);
		mCommentBt = (ImageView)findViewById(R.id.program_detail_comment_img);
		mCommentBt.setOnClickListener(this);
		
		Bundle bundle = getIntent().getExtras();
		Program program = (Program)bundle.getParcelable("program");
		
		mDouban = new DoubanAdapter();
		mDouban.setListener(this);
		mDouban.getDoubanProgramInfo(program.getProgramName());
		
		getSupportActionBar().setTitle(program.getProgramName());
		
		mProgramDetail = (TextView)findViewById(R.id.program_detail_introduce_content_text); 
		mDoubanRating  = (TextView)findViewById(R.id.text_douban_rating);
		
		mMoreBt = (ImageView)findViewById(R.id.image_detail_program_more);
		mMoreBt.setOnClickListener(this);
	}

	@Override
	public void onClick(View arg0)
	{
		// TODO Auto-generated method stub
		switch (arg0.getId())
		{
			case R.id.program_detail_comment_img:
				showComments();
				break;
			case R.id.image_detail_program_more:
				if(mProgram != null)
					startWebActivity(mProgram.mUrl);
				break;
			default:
				break;
		}
	}
	
	public void showComments()
	{
		Intent intent = new Intent();
		intent.setClass(this, ProgramCommentActivity.class);
		startActivity(intent);
	}

	@Override
	public void onDoubanCallbackSucc(Program program)
	{
		mProgram = program;
		mProgramDetail.setText(program.getDescription());
		String str = "∂π∞Í∆¿∑÷: ";
		str+= String.valueOf(program.mRating);
		mDoubanRating.setText(str);
		// TODO Auto-generated method stub
		Log.d(TAG , "onDoubanCallbackSucc");
		
	}

	@Override
	public void onDoubanCallbackFail()
	{
		// TODO Auto-generated method stub
		Log.d(TAG , "onDoubanCallbackFail");
		
	}
	
	protected void startWebActivity(String url)
	{
		if(url == null)
			return;
		Intent intent = new Intent();
		intent.putExtra("url", url);
		intent.setClass(this, WebActivity.class);
		startActivity(intent);
	}
}
