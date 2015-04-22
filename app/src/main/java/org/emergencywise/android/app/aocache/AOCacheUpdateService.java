package org.emergencywise.android.app.aocache;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AOCacheUpdateService
	extends IntentService
{
	private static final String TAG = AOCacheUpdateService.class.getName();
	
	public AOCacheUpdateService()
	{
		super( "AOCacheUpdateService" );
	}
    
	public static void startUpdate( Context context )
	{
		Intent i = new Intent( context, AOCacheUpdateService.class );
		context.startService( i );
	}
	
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "Created CacheUpdateService");
	}

	public void onDestroy()
	{
		super.onDestroy();
		Log.d( TAG, "Destroyed CacheUpdateService" );
	}
	
    // run in its own thread
	protected void onHandleIntent( Intent intent )
	{
		new AOCacheUpdateWorker( this ).syncAll();
	}
}