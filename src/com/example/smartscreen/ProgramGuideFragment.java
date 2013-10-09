package com.example.smartscreen;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockFragment;

public class ProgramGuideFragment extends SherlockFragment implements OnItemClickListener
{
	public static final String TAG = "ProgramGuideFragment";
	public ListView                            mGuideList;
	public ProgramGuideAdapter                 mAdapter;
	public ArrayList<HashMap<String , Object>> mItems;
	NetworkAdapter                             mHttpSession;
	public ProgressBar                         mLoadingBar;
	public int                                 mDayOffset;
	public TvStationProgram                    mCurrentProgram;
	
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHttpSession = new NetworkAdapter("tvshow.ap01.aws.af.cm", "80");
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {
		View rootView = inflater.inflate(R.layout.activity_program_guide, container, false); 
		mGuideList  = (ListView)rootView.findViewById(R.id.listview);
		mItems = new ArrayList<HashMap<String , Object>>();
		mAdapter = new ProgramGuideAdapter(getActivity(), 
    			mItems,
    			R.layout.item_programguide,
    			null,
    			null);
		mGuideList.setAdapter(mAdapter);
		mGuideList.setOnItemClickListener(this);
		//testData();
		//asyncLoadProgramGuide(7 , 0);
		Bundle bundle = getArguments();
		mDayOffset = bundle.getInt(getString(R.string.bundle_dayoffset));
		mCurrentProgram = bundle.getParcelable(getString(R.string.bundle_program));
		
		return rootView;
    }
	
	
	@Override
	public void onStart()
	{
		super.onStart();
		asyncLoadProgramGuide(mCurrentProgram.getTvIndex() , mDayOffset);
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
	
	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Phone2TvComm.HAHNDLE_NOTIFY what = Phone2TvComm.HAHNDLE_NOTIFY.values()[msg.what];
			switch(what)
			{
			case NOTIFY_SUCC:
				onLoadProgramGuideSucc(msg);
				break;
			case NOTIFY_FAIL:
				onLoadProgramGuideFail(msg);
				break;
			default:
				break;
			}
		}
	};
	
	void onLoadProgramGuideSucc(Message msg)
	{
		fillItemsFromData((TvStationProgram)msg.obj , mItems);
		mAdapter.notifyDataSetChanged();
	}
	
	void onLoadProgramGuideFail(Message msg)
	{
		Log.d(TAG , "onLoadProgramGuideFail");
	}
	
	void fillItemsFromData(TvStationProgram tv , ArrayList<HashMap<String , Object>> items)
	{
		Program program = null;
		for(int i = 0 ; i < tv.getPrograms().size() ; i++)
		{
			HashMap<String , Object> item = new HashMap<String  , Object>();
			program = tv.getPrograms().get(i);
			fillItem(program , item);
			items.add(item);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	void fillItem(Program program , HashMap<String , Object> item )
	{
		item.put("program" , program);
	}
	
	//today :   dayOffset = 0;
	//tomorrow: dayOffset = 1;
	void asyncLoadProgramGuide(final int channel , final int dayOffset)
	{
		new Thread()
		{
			public void run()
			{
				Message msg = Message.obtain();
				try
				{
					JSONObject json = mHttpSession.requestProgramByTvstation(channel, dayOffset);
					msg.what = Phone2TvComm.HAHNDLE_NOTIFY.NOTIFY_SUCC.ordinal();
					TvStationProgram programs =  mHttpSession.getProgramsByTvStation(json);
					sortProgramByTime(programs);
					msg.obj = programs;	
				}
				catch(Exception e)
				{
					msg.what = Phone2TvComm.HAHNDLE_NOTIFY.NOTIFY_FAIL.ordinal();
				}
				mHandler.sendMessage(msg);
			}
		}.start();
	}
	
	public static class ProgramGuideAdapter extends SimpleAdapter
	{
		private final static String TAG="ProgramGuideAdapter";
    	private ArrayList<HashMap<String , Object>> mItemData;
    	private Context mContext;
    	private int     mResource;
    	
    	public class Holder
    	{
    		TextView  mBeginTime;
    		TextView  mEndTime;
    		TextView  mProrgramName;
    		TextView  mDiscussNum;
    	}

		public ProgramGuideAdapter(Context context,
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
			
			fillViewFromData(position , convertView , mItemData.get(position));
			return convertView;
		}
		
		public void bindView(View convertView)
		{
			 Holder holder = new Holder();
			 holder.mBeginTime    = (TextView)convertView.findViewById(R.id.begin_time_text);
			 holder.mEndTime      = (TextView)convertView.findViewById(R.id.end_time_text);
			 holder.mProrgramName = (TextView)convertView.findViewById(R.id.program_name_text);
			 holder.mDiscussNum   = (TextView)convertView.findViewById(R.id.discuss_num_text);
			 convertView.setTag(holder);
		}
		
		public void fillViewFromData(int position , View convertView , HashMap<String , Object> data)
		{
			Holder holder = (Holder)convertView.getTag();
			Program program =(Program)data.get("program");
			Date curDate = new Date(System.currentTimeMillis());
			int color;
			if(program.getEndTime().after(curDate))
			{
				color = mContext.getResources().getColor(R.color.program_name_text_color);
			}
			else
				color = mContext.getResources().getColor(R.color.program_name_text_def_color);
				
			holder.mBeginTime.setTextColor(color);
			holder.mEndTime.setTextColor(color);
			holder.mProrgramName.setTextColor(color);
			holder.mDiscussNum.setTextColor(color);
			
			String tmp = Phone2TvComm.DateToString(program.getBeginTime() , Phone2TvComm.mFormat[2]);
			holder.mBeginTime.setText(tmp);
			
			tmp = Phone2TvComm.DateToString(program.getEndTime() , Phone2TvComm.mFormat[2]);
			holder.mEndTime.setText(tmp);
			
			tmp = program.getProgramName();
			holder.mProrgramName.setText(tmp);
			
			tmp = "评论(";
			tmp += String.valueOf(program.getDisscusCount());
			tmp +=")";
			holder.mDiscussNum.setText(tmp);
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
	
	public class ProgramCompare implements Comparator
	{

		@Override
		public int compare(Object arg0, Object arg1)
		{
			// TODO Auto-generated method stub
			Program p1 = (Program) arg0;
			Program p2 = (Program) arg1;
			if (p2.mBeginTime.before(p1.mBeginTime))
				return 1;

			return -1;
		}

	}
	
	private void sortProgramByTime(TvStationProgram program)
	{
		ProgramCompare compare = new ProgramCompare();
		Collections.sort(program.mPrograms, compare);
		for (int i = 0; i < program.getPrograms().size(); i++)
		{
			Log.d(TAG, program.getPrograms().get(i).mName);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
	{
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		Program program = (Program)mItems.get(arg2).get("program");
		Bundle bundle = new Bundle();
		bundle.putParcelable("program" , program);
		intent.setClass(getActivity(), ProgramDetailActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
}
