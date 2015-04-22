package com.citizen911.android.util;

import java.net.URLDecoder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.citizen911.android.SurveyActivity;
import com.citizen911.android.R;
import com.citizen911.android.json.JsonWriter;
import com.citizen911.android.log.Logr;
import com.citizen911.android.report.OutpostReportManager;
import com.citizen911.android.report.WebviewReportManager;
import com.citizen911.common.util.StringTool;

public class WebViewActivity
	extends Activity
{
	private Locator locator;			// used for tracking during reports
	private WebView mWebView; 
	private Dialog progressDialog;		// progress bar/spinner over webview during page loads
	private static final String TAG = WebViewActivity.class.getName();
	//private static final List<String> IGNORE = Arrays.asList( "_c911_report.js" );
	
	private WebviewReportManager reportManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate( savedInstanceState );
        
        // do a quick redirect?
        Intent intent = getIntent();
        Uri uri = intent.getData();
        if( handleActivityUri( uri ) )
        {
        	finish();
        	return;
        }
        
        mWebView = new WebView(this);
        setContentView( mWebView );
        
        // micro tracking?
        if( intent.getBooleanExtra( "tracking", false ) )
        {
        	locator = new Locator( this );
        	locator.start( 10, 5, null );
        }

		reportManager = new WebviewReportManager( this );
        mWebView.addJavascriptInterface( reportManager, "C911Report" );
        mWebView.addJavascriptInterface( new OutpostReportManager( this ), "C911Outpost" );
        //mWebView.setFocusableInTouchMode( true );
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword( false );
        webSettings.setSaveFormData( false );
        webSettings.setJavaScriptEnabled( true );
        //webSettings.setSupportZoom( false );
        
        String ua = webSettings.getUserAgentString() + " Citizen911/1.0";
        webSettings.setUserAgentString( ua );

        // more hooks
        WebViewClient mWebViewClient = new MyWebViewClient();
        mWebView.setWebViewClient( mWebViewClient );
        //mWebView.setWebChromeClient( new MyWebChromeClient() );
        
        // load the url
    	mWebView.loadUrl( Filer.toUrl( uri, this ) );	// async
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	
    	if( locator != null )
    	{
    		locator.stop();
    		locator = null;
    	}
    }
    
    public String getUrl()
    {
    	return mWebView.getUrl();
    }
    
    public void loadUrl( String url )
    {
    	mWebView.loadUrl( url );
    }
    
    private boolean handleActivityUri( Uri uri )
    {
    	if( uri == null )
    	{
    		Toast.makeText( this, R.string.missing_uri, Toast.LENGTH_LONG ).show();
    		return true;
    	}
    	
    	String scheme = uri.getScheme();
        if( "activity".equals( scheme ) )
        {	        
	    	String authority = uri.getAuthority();
	    	if( "map".equals( authority ) )
	    	{
	    		Intent i = new Intent( WebViewActivity.this, SurveyActivity.class );
	    		addQuery( i, uri.getQuery() );
	    		startActivity( i );
	    		return true;
	    	}
	    	
	    	// ugh, unknown activity
	    	Toast.makeText( this, "Unknown activity " + authority, Toast.LENGTH_LONG ).show();
	    	return true;
        }
        else if( "submit".equals( scheme ) )
        {
        	// TODO handle "report" and "form" paths in the future
        	String query = uri.getQuery();
        	reportManager.saveReport( query );
        	return true;
        }
        
        return false;
    }
    
    private void addQuery( Intent i, String query )
    {
    	if( query == null ) return;
    	
		for( String pair : query.split( "&" ) )
		{
			int pEquals = pair.indexOf( "=" );
			if( pEquals > -1 )
			{
				String key = pair.substring( 0, pEquals );
				String value = pair.substring( pEquals + 1 );
				value = StringTool.clean( URLDecoder.decode( value ) );
				if( value != null )
				{
					i.putExtra( key, value );
				}
			}
		}    	
    }
    
    //
    // Capture "Back" key so it doesn't switch to previous activity
	//
    @Override
	public boolean onKeyDown( int keyCode, KeyEvent event )
	{
	    if( ( keyCode == KeyEvent.KEYCODE_BACK ) && mWebView.canGoBack() )
	    {
	        mWebView.goBack();
	        return true;
	    }
	    return super.onKeyDown( keyCode, event );
	} 

    //
    // Handle the progress spinner between each webview page
    //
    
    private void startProgress()
    {
    	showDialog( PROGRESS_DIALOG );
    }
    
    private static final int PROGRESS_DIALOG = 0;
    @Override
	protected Dialog onCreateDialog( int id )
    {
        switch (id)
        {
            case PROGRESS_DIALOG:
            {
            	View view = View.inflate( this, R.layout.progress_dialog, null ); 	
            	progressDialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
            	progressDialog.setContentView( view );
            	return progressDialog;
            }
        }
        
        return null;
    }
    
    private void dismissProgress()
    {    	
    	// if there's a webview progress... dismiss it
    	if( progressDialog != null )
    	{
    		this.runOnUiThread( progressDialogDismisser );
    	}
    }
    Runnable progressDialogDismisser = new Runnable()
    {
    	public void run()
    	{
        	if( progressDialog != null ) progressDialog.dismiss();   		
    	}
    };
	
    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
    	reportManager.onActivityResult( requestCode, resultCode, data );
    }

	//
	// Track progress during opening splash page
	/**
    final class MyWebChromeClient
    	extends WebChromeClient
    {
        @Override
        public boolean onJsAlert( WebView view, String url, String message, JsResult result )
        {
            Logr.d( TAG, message );
            result.confirm();
            return true;
        }
        
		@Override
        public void onProgressChanged( WebView view, int newProgress )
		{
			//WebViewActivity.this.setProgress( newProgress * 1000 );
			
			/*
        	if( newProgress == 100 && lastPageProgress == 10 && !firstPageLoaded )
        	{
        		// bogus entry so ignore
        	}
        	else
        	{
            	splashProgress.setProgress( newProgress );
            	progressMonitor.describeProgress( Integer.toString( newProgress ) + "%" );
        	}
	        lastPageProgress = newProgress;	// for debouncing; androids first page load happens twice :(
        	Logr.i( TAG, "Page progress... " + newProgress );
        	/
		}
    }*/
    
    /*private boolean isExcluded( Uri uri )
    {
    	if( uri == null ) return true;	// might as well fail fast
    	
    	if( "file".equals( uri.getScheme() ) != true ) return false;
    	
    	List<String> path = uri.getPathSegments();
    	if( path == null || path.isEmpty() ) return false;
    	String filename = path.get( path.size() - 1 );
    	return IGNORE.contains( filename );
    }*/
      
    /**
     * Also determines when the first page has been loaded so splash can be dismissed.
     */
    private class MyWebViewClient
    	extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading( WebView view, String url )
        {
            Logr.i( TAG, "shouldOverrideUrlLoading: " + url );
            
            // support "activity" scheme
            Uri uri = Uri.parse( url );
            if( handleActivityUri( uri ) ) return true;
            
            url = Filer.toUrl( uri, WebViewActivity.this );	// convert any custom schemes
            view.loadUrl( url );
            return true;
        }
        
        /*
        @Override public void onLoadResource( WebView view, String url )
        {
            Uri uri = Uri.parse( url );
            
            // exclude this file?
            if( isExcluded( uri ) )
            {
            	return;
            }
            else
            {
            	super.onLoadResource( view, url );
            }
        }*/
        
        @Override
        public void onPageStarted( WebView webview, String url, Bitmap favicon )
        {
        	Logr.i( TAG, "Page started: " + url );
        	startProgress();
        }
        
        @Override
        public void onPageFinished( WebView webview, String url )
        { 
        	CookieSyncManager.getInstance().sync();
        	dismissProgress();
        }
        
        @Override
        public void onReceivedError( WebView view, int errorCode, String description, String failingUrl )
        {
        	dismissProgress();
        }
    }
    
	public void setWebFormField( String formName, String name, String value )
	{
		String s = JsonWriter.toJson( value );
		String javascript = "javascript:var i=document.forms." + formName + "." + name + ";if(i.type == 'checkbox'){i.checked=true;} else i.value=" + s + ";";
		Logr.d( TAG, javascript );
		loadUrl( javascript );		
	}
}