package com.example.smartscreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;

public class ProgramCommentActivity extends SherlockActivity implements OnItemClickListener , OnClickListener
{
	public static int THEME = R.style.Theme_Sherlock;
	public EditText mInputEditor;
	public ListView mCommentList;
	public CommentAdapter mAdapter;
	public ArrayList<HashMap<String , Object>> mItems;
	protected void onCreate(Bundle savedInstanceState)
	{
		setTheme(THEME); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.program_comment);
		mInputEditor = (EditText)findViewById(R.id.program_comment_input_editor);
		mCommentList = (ListView)findViewById(R.id.program_comment_list);
		mItems = new ArrayList<HashMap<String , Object>>();
		mAdapter = new CommentAdapter(this , mItems , R.layout.item_comment , null , null);
		mCommentList.setAdapter(mAdapter);
		testData();
		setSoftInputVisible(false);
	}
	
	void testData()
	{
	   	HashMap<String , Object> tmp = null;
    	for(int i = 0 ; i < 20 ; i++)
    	{
    	tmp = new HashMap<String , Object>();
    	tmp.put(getString(R.string.item_nowplaying_tvname), "中央一套");
    	tmp.put(getString(R.string.item_nowplaying_programname), "足球之夜");
    	
    	mItems.add(tmp);
    	}
    	mAdapter.notifyDataSetChanged();
	}
	
	 protected void setSoftInputVisible(boolean flag)
	 {
		 InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); 
		 if(flag)
		 {
			 imm.showSoftInput(mInputEditor , InputMethodManager.SHOW_FORCED);
		 }
		 else
		 {
			 imm.hideSoftInputFromWindow(mInputEditor.getWindowToken(), 0);
		 }

	 }
	 
	 public static class CommentAdapter extends SimpleAdapter
	 {
		public static class Holder
		{
			ImageView mAvatar;
			TextView  mUserName;
			TextView  mPostTime;
			ImageView mExpandBt;
			RelativeLayout mExpandRelativeLayout;
			ImageView mPostLoveBt;
			ImageView mPostUnlikeBt;
			ImageView mFolderBt;
		}
		 
		private final static String TAG="CommentAdapter";
	    private ArrayList<HashMap<String , Object>> mItemData;
	    private Context mContext;
	    private int     mResource;

		public CommentAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to)
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
			mContext = context ;
			mItemData = (ArrayList<HashMap<String , Object>>)data;
			mResource = resource;
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if(convertView == null)
			{
				LayoutInflater inflater = null;
				inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(mResource, null);
				bindView(convertView);
			}
			
			fillViewFromData(position , convertView , null);
			return convertView;
		}
		
		public void bindView(View convertView)
		{
			Holder holder = new Holder();
			
			holder.mExpandRelativeLayout = (RelativeLayout)convertView.findViewById(R.id.program_comment_love_relativelayout);
			holder.mFolderBt             = (ImageView)convertView.findViewById(R.id.program_comment_expand_love_img);
			holder.mFolderBt.setOnClickListener((View.OnClickListener)mContext);
			holder.mFolderBt.setTag(holder.mExpandRelativeLayout);
			
			holder.mExpandBt             = (ImageView)convertView.findViewById(R.id.program_comment_love_img);
			holder.mExpandBt.setOnClickListener((View.OnClickListener)mContext);
			holder.mExpandBt.setTag(holder.mExpandRelativeLayout);
			
			convertView.setTag(holder);
		}
		
		public void fillViewFromData(int position , View convertView , HashMap<String , Object> data)
		{
			Holder holder = (Holder)convertView.getTag();
			int visibility = holder.mExpandRelativeLayout.getVisibility();
			holder.mExpandRelativeLayout.setVisibility(visibility);
		}
		
		@Override
		public int getCount()
		{
			int nSize = mItemData.size();
			return nSize;
		}

		@Override
		public Object getItem(int position)
		{
			Log.d(TAG , "getItem :"+position);
			return mItemData.get(position);
		}
		
		@Override
		public long getItemId(int position) 
		{
			Log.d(TAG , "getItemId :"+position);
			return position;
		}
		 
	 }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub
		
	}
	
	protected void setVisible(RelativeLayout view , boolean bShow)
	{
		int visibility = -1;
		if(bShow)
			visibility = View.VISIBLE;
		else
			visibility = View.GONE;
		
		view.setVisibility(visibility);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.program_comment_love_img:
			setVisible((RelativeLayout)v.getTag() , true);
			break;
		case R.id.program_comment_expand_love_img:
			setVisible((RelativeLayout)v.getTag() , false);
			break;
		}
		
	}
}
