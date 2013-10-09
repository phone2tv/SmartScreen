package com.example.smartscreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

public class NowPlayingFragment extends SherlockFragment implements OnScrollListener , OnItemClickListener
{
	public static final String TAG = "NowPlayingFragment";
	public ListView mNowPlayingList;
	public ArrayList<HashMap<String , Object>> mItems;
	NowPlayingAdapter  mAdapter;
	NetworkAdapter     mHttpSession;
	public ProgressBar mLoadingBar;
	int                mCatalog;
	int                mScrollState;
	ArrayList<TvStationProgram>  mTvs;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHttpSession = new NetworkAdapter("tvshow.ap01.aws.af.cm", "80");
		mCatalog = getArguments().getInt(getString(R.string.bundle_catalog));
	}
		
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
    {  
    	this.setHasOptionsMenu(true);
        // Inflate the layout for this fragment
    	View rootView = inflater.inflate(R.layout.smartscreen_list, container, false); 

    	mLoadingBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
    	 
    	mNowPlayingList = (ListView)rootView.findViewById(R.id.listview);
    	mItems = new ArrayList<HashMap<String , Object>>();
    	mAdapter = new NowPlayingAdapter(getActivity(), 
    			mItems,
    			R.layout.item_nowplaying,
    			null,
    			null);
        mNowPlayingList.setAdapter(mAdapter);
        mNowPlayingList.setOnScrollListener(this);
        mNowPlayingList.setOnItemClickListener(this);

        asyncLoadNowPlayingProgram(mCatalog);
        return rootView;
    } 
    
    protected void asyncLoadNowPlayingProgram(final int catalog)
    {
    	new Thread()
    	{
    		public void run()
    		{
    			Message msg = Message.obtain();
    			try
    			{
    				JSONArray  json = mHttpSession.requestAllTvStations(catalog);
    				ArrayList<TvStationProgram> tvs = mHttpSession.getAllTvStations(json);
					msg.what = Phone2TvComm.HAHNDLE_NOTIFY.NOTIFY_SUCC.ordinal();
					msg.obj = tvs;
					mHandler.sendMessage(msg);
    			}
    			catch(Exception e)
    			{
    				msg.what = Phone2TvComm.HAHNDLE_NOTIFY.NOTIFY_FAIL.ordinal();
    				msg.obj = null;
    				mHandler.sendMessage(msg);
    			}
    		}
    	}.start();
    }
    
	Handler mHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Phone2TvComm.HAHNDLE_NOTIFY what = Phone2TvComm.HAHNDLE_NOTIFY.values()[msg.what];
			switch(what)
			{
			case NOTIFY_SUCC:
				onLoadNowPlayingProgramSucc(msg);
				break;
			default:
				onLoadNowPlayingProgramFail(msg);
				break;
			}
			mLoadingBar.setVisibility(View.GONE);
		}
	};
	
	void onLoadNowPlayingProgramSucc(Message msg)
	{
		mTvs = (ArrayList<TvStationProgram>)msg.obj;
		fillItemsFromData( mTvs , mItems);
		mAdapter.notifyDataSetChanged();
	}
	
	void onLoadNowPlayingProgramFail(Message msg)
	{
		//switch to fail UI
		//click fail UI to reload
	}
	
	void fillItemsFromData(ArrayList<TvStationProgram> tvs , ArrayList<HashMap<String , Object>> items)
	{
		TvStationProgram tv = null;
		HashMap<String , Object> tmp = null;
		for(int i = 0 ; i < tvs.size() ; i++)
		{
			tv = tvs.get(i);
			tmp = new HashMap<String , Object>();
			fillItem(tv , tmp);
			items.add(tmp);
		}
	}
	
	void fillItem(TvStationProgram tv , HashMap<String , Object> item )
	{
		if(tv.getPrograms().size() != 0)
		{
			item.put(getString(R.string.item_nowplaying_programname) , tv.getPrograms().get(0).mName);
			item.put(getString(R.string.item_nowplaying_tvname) , tv.getTvName());
			item.put(getString(R.string.item_nowplaying_tvid) , tv.getTvIndex());
		}
	}
    
    void testData()
    {
    	HashMap<String , Object> tmp = null;
    	tmp = new HashMap<String , Object>();
    	tmp.put(getString(R.string.item_nowplaying_tvname), "中央一套");
    	tmp.put(getString(R.string.item_nowplaying_programname), "足球之夜");
    	
    	mItems.add(tmp);
    	
    	mAdapter.notifyDataSetChanged();
    }
    
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount)
	{
		// TODO Auto-generated method stub
		if (visibleItemCount == 0) 
        {
			mLoadingBar.setVisibility(View.VISIBLE);
        } 
		
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState)
	{
		// TODO Auto-generated method stub
		mScrollState = scrollState;
	}
    
    public class NowPlayingAdapter extends SimpleAdapter
    {
    	private final static String TAG="NowPlayingAdapter";
    	private ArrayList<HashMap<String , Object>> mItemData;
    	private Context mContext;
    	private int     mResource;
    	
    	public class Holder
    	{
    		ImageView mTvLogo;
    		TextView  mProgamName;
    		TextView  mTvName;
    		RatingBar mHot;
    	}

		public NowPlayingAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to)
		{
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
			mContext = context ;
			mItemData = (ArrayList<HashMap<String , Object>>)data;
			mResource = resource;
		}
		
		
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
			 holder.mTvLogo     = (ImageView)convertView.findViewById(R.id.logo_img);
			 holder.mProgamName = (TextView)convertView.findViewById(R.id.program_name_text);
			 holder.mTvName     = (TextView)convertView.findViewById(R.id.tvname_text);
			 holder.mHot        = (RatingBar)convertView.findViewById(R.id.hot_ratingbar);
			 convertView.setTag(holder);
		 }
		 
		 public void fillViewFromData(int position , View convertView , HashMap<String , Object> data)
		 {
			 Holder holder =(Holder)convertView.getTag();
			 
			 holder.mTvLogo.setImageResource(R.drawable.cctv5);
			 String tmp = (String)data.get(getString(R.string.item_nowplaying_programname));
			 holder.mProgamName.setText(tmp);
			 tmp = (String)data.get(getString(R.string.item_nowplaying_tvname));
			 holder.mTvName.setText(tmp);
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
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putParcelable(getString(R.string.bundle_program), mTvs.get(arg2));
		intent.putExtras(bundle);
		intent.setClass(getActivity(), ProgramGuideTabsPager.class);
		startActivity(intent);
	}


}
