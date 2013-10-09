package com.example.smartscreen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebView;

public class WebActivity extends Activity
{
public WebView webView;
	
	public void onCreate(Bundle savedInstanceState) 
	{ 
	        super.onCreate(savedInstanceState); 
	        
	        webView = new WebView(this); 
	        
	        webView.getSettings().setJavaScriptEnabled(true);  
	        
	        Intent intent = getIntent();
	        
	        webView.loadUrl(getUrl(intent)); 
	        
	        setContentView(webView); 
	 } 
	
	
	public String getUrl(Intent intent)
	{
		String url = intent.getStringExtra("url");
		return url;
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{ 
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) 
        { 
            webView.goBack(); 
            return true; 
        }
        else
        	finish();
        
        return false;
    }
}
