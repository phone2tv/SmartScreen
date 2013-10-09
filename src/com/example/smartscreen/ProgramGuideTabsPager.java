package com.example.smartscreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.widget.TabHost;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ProgramGuideTabsPager extends SherlockFragmentActivity implements ActionBar.TabListener
{
	public static int THEME = R.style.Theme_Sherlock;
	private TabHost mTabHost;
    private ViewPager  mViewPager;
    private TabsAdapter mTabsAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		setTheme(THEME); 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.programguide_tabs_pager);
		
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();
		
		mViewPager = (ViewPager)findViewById(R.id.pager);
		
		mTabsAdapter = new TabsAdapter(this , mTabHost , mViewPager);
		
		Intent intent = getIntent();
		Bundle extraBundle = intent.getExtras();
		
		Bundle bundle = new Bundle();
		bundle.putParcelable(getString(R.string.bundle_program), 
				extraBundle.getParcelable(getString(R.string.bundle_program)));
        bundle.putInt(getString(R.string.bundle_dayoffset), 0);
		mTabsAdapter.addTab(mTabHost.newTabSpec("today").setIndicator("今天"),
				ProgramGuideFragment.class, bundle);
		
		bundle = new Bundle();
		bundle.putParcelable(getString(R.string.bundle_program), 
				extraBundle.getParcelable(getString(R.string.bundle_program)));
		bundle.putInt(getString(R.string.bundle_dayoffset), 1);
		mTabsAdapter.addTab(mTabHost.newTabSpec("today+1").setIndicator("明天"),
				ProgramGuideFragment.class, bundle);
		
		bundle = new Bundle();
		bundle.putParcelable(getString(R.string.bundle_program), 
				extraBundle.getParcelable(getString(R.string.bundle_program)));
		bundle.putInt(getString(R.string.bundle_dayoffset), 2);
		mTabsAdapter.addTab(mTabHost.newTabSpec("today+2").setIndicator("后天"),
				ProgramGuideFragment.class, bundle);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft)
	{
		// TODO Auto-generated method stub
		
	}

}
