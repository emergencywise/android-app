package org.emergencywise.android.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TabHost;

import com.citizen911.android.BaseTabActivity;
import com.citizen911.android.R;

public class SelectAOActivity
	extends BaseTabActivity
{
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.tabbed_main );
	
	    //Resources res = getResources();  // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    
	    // a specific host?
	    Uri uri = getIntent().getData();
	
	    // Do the same for the other tabs
	    Intent intent = new Intent( this, ListNewNearbyAOActivity.class );
	    if( uri != null ) intent.setData( uri );
	    TabHost.TabSpec spec = tabHost.newTabSpec( "newnearby" )
	    			  .setIndicator( getString( R.string.tab_newnearbyao ) )
	                  .setContent( intent );
	    tabHost.addTab(spec);
	    
	    intent = new Intent( this, ListCurrentAOActivity.class );
	    if( uri != null ) intent.setData( uri );
	    spec = tabHost.newTabSpec( "current" )
	    			  .setIndicator( getString( R.string.tab_currentao ) )
	                  .setContent( intent );
	    tabHost.addTab(spec);
	    
	    /*
	    intent = new Intent().setClass( this, BrowseMenuActivity.class );
	    spec = tabHost.newTabSpec( "all" )
	    			  .setIndicator( getString( R.string.tab_allao ) )
	                  .setContent( intent );
	    tabHost.addTab(spec);
	    */
	
	    // start at main
	    tabHost.setCurrentTab( 0 );
	}
}