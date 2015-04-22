package org.emergencywise.android.app.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonTool
{	private static final String TAG = JsonTool.class.getSimpleName();
	private static JsonTool instance;
	private Gson gson;

	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";  // http://www.ietf.org/rfc/rfc3339.txt
    private static final DateFormat format = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );  // http://www.ietf.org/rfc/rfc3339.txt

	private JsonTool()
	{
		gson = new GsonBuilder().setDateFormat( DATE_FORMAT )
								.setPrettyPrinting()	// TODO remove later
								.create();
	}

	public static String toJson( Object obj )
	{
		if( instance == null ) instance = new JsonTool();
		return instance.gson.toJson( obj );
	}
	
	public static void toJson( Object obj, Appendable writer )
	{
		if( instance == null ) instance = new JsonTool();
		instance.gson.toJson( obj, writer );
	}
	
	public static <T> T fromJson( String json, Class<T> clazz )
	{
		if( instance == null ) instance = new JsonTool();
		return instance.gson.fromJson(json, clazz );
	}
	
	public static <T> T fromJson( Reader json, Class<T> clazz )
	{
		if( instance == null ) instance = new JsonTool();
		return instance.gson.fromJson(json, clazz );
	}

    public static Date getDate( JSONObject json, String name )
    {
        if( json == null ) return null;
        String s = json.optString( name );
        if( s == null ) return null;

        try
        {
            synchronized( format )	// bah! SimpleDateFormat is not thread safe!
            {
                return format.parse( s );
            }
        }
        catch( ParseException ex )
        {
            Log.e(TAG, "Failed to parse JSON date", ex);
            return null;
        }
    }


	/*
	public static String asRFC3339( Date time )
	{
		if( time == null ) return null;
		return (new SimpleDateFormat( DATE_FORMAT )).format( time );
	}
	*/
	
	/*
	//private static String[] SKIP_FIELDS = { "flagUsers", "subscribers", "jdoDetachedState", "metatags", "patch" };
	//private static Class<?>[] SKIP_CLASSES = {}; // VoteSet.class };
	private class MyExclusionStrategy
		implements ExclusionStrategy
	{
		private String[] skipFields;
		private Class<?>[] skipClasses;
		
		public MyExclusionStrategy( String[] skipFields, Class<?>[] skipClasses )
		{
			this.skipFields = skipFields;
			this.skipClasses = skipClasses;
		}
		
		@Override
		public boolean shouldSkipClass(Class<?> clazz)
		{
			for( Class<?> c : skipClasses )
			{
				if( c.isAssignableFrom( clazz ) )
				{
					return true;
				}
			}

			return false;
		}

		@Override
		public boolean shouldSkipField( FieldAttributes f ) 
		{
			String name = f.getName();
			for( String s : skipFields )
			{
				if( s.equals( name ) ) return true;
			}

			return false;
		}
	}
	*/
}