package org.emergencywise.android.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.emergencywise.android.app.util.StringTool;

public class MyPreferences
{
	public static final String PREFERENCE_MAP = "map";
	public static final String PREFERENCE_NICKNAME = "nickname";
	public static final String PREFERENCE_AO = "ao";
	public static final String PREFERENCE_AOSERVER = "aoserver";
	public static final String PREFERENCE_REPORTSERVER = "reportserver";
	public static final String PREFERENCE_UID = "uid";
	public static final String PREFERENCE_ANALYTICS = "analytics";
	public static final String PREFERENCE_BACKGROUND_UPLOADS = "background_uploads";
	public static final String PREFERENCE_GPS_LOCK = "gps_lock";
	public static final String PREFERENCE_SHARE_LOCATION = "share_location";
	public static final String PREFERENCE_AUTO_HASTY = "auto_hasty";
	public static final String PREFERENCE_ZONE_LOOKAHEAD = "zone_lookahead";
	
	public MyPreferences( Context context )
	{
		this.preferences = PreferenceManager.getDefaultSharedPreferences( context );
	}
	private SharedPreferences preferences;
	
	//
	// Generic accessors and setters
	//
	
	public void setString( String key, String value )
	{
		SharedPreferences.Editor editor = preferences.edit();
		if( value == null )
		{
			editor.remove( key );
		}
		else
		{
			editor.putString( key, value );			
		}
		editor.commit();
	}
	
	public boolean contains( String key )
	{
		return preferences.contains( key );
	}
	
	public void setBoolean( String key, boolean value )
	{
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean( key, value );
		editor.commit();
	}
	
	public Boolean getBoolean( String key, boolean defvalue )
	{
		return preferences.getBoolean( key, defvalue );
	}
	
	public String getString( String key )
	{
		return preferences.getString( key, null );
	}
	
	public String getString( String key, String defvalue )
	{
		return preferences.getString( key, defvalue );
	}
	
	//
	// Named accessors and setters
	//
	
	public String getAo()
	{
		return preferences.getString( PREFERENCE_AO, null );
	}
	
	public void setAo( String ao )
	{
		setString( PREFERENCE_AO, ao );
	}
	
	public String getMap()
	{
		return preferences.getString( PREFERENCE_MAP, "google" );		
	}
	
	public String getNickname()
	{
		return preferences.getString( PREFERENCE_NICKNAME, null );
	}

    /*
	public String getAoManifestUrl()
	{
		String url = preferences.getString( PREFERENCE_AOSERVER, null );
		return UrlTool.fixupServerUrl( url, API.AO_MANIFEST_PATH );
	}
	
	public String getAoNearbyUrl()
	{
		String url = preferences.getString( PREFERENCE_AOSERVER, null );
		return UrlTool.fixupServerUrl( url, API.AO_NEARBY_PATH );
	}
	
	public String getReportServerUrl()
	{
		String url = preferences.getString( PREFERENCE_REPORTSERVER, null );
		if( url == null ) return null;
		return getReportServerUrl( url );
	}
	
	public static String getReportServerUrl( String baseUrl )
	{
		return UrlTool.fixupServerUrl( baseUrl, API.REPORT_SYNC_PATH );
	}
	*/
	
	public boolean hasBackgroundUploads()
	{
		return preferences.getBoolean( PREFERENCE_BACKGROUND_UPLOADS, false );
	}
	
	public String getUserId()
	{
		return StringTool.clean(preferences.getString(PREFERENCE_UID, null));
	}
	
	public boolean hasGpsLock()
	{
		return preferences.getBoolean( PREFERENCE_GPS_LOCK, false );
	}
	
	public void setGpsLock( boolean value )
	{
		setBoolean( PREFERENCE_GPS_LOCK, value );
	}
	
	public void setShareLocation( boolean value )
	{
		setBoolean( PREFERENCE_SHARE_LOCATION, value );
	}
	
	public void setAutoHasty( boolean value )
	{
		setBoolean( PREFERENCE_AUTO_HASTY, value );
	}
}