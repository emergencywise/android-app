package org.emergencywise.android.app.util;

import java.io.IOException;
import java.net.URL;

public class UrlTool
{	
	public static String fixupServerUrl( String base, String path )
	{
		if( base == null ) return null;
		
		// do we need to add the protocol?
		String url = base.trim();
		if( !url.startsWith( "http://" ) && !url.startsWith( "https://" ) )
		{
			url = "http://" + url;
		}
		
		// do we need to add the path?
		int slashes = 0;
		for( char ch : url.toCharArray() ) if( ch == '/' ) slashes++;
		if( slashes == 2 )	// i.e. http://localhost:8888
		{
			return url + '/' + path;
		}
		else if( slashes == 3 && url.endsWith( "/" ) ) // i.e. http://localhost:8888/
		{
			return url + path;
		}
		else return url;	
	}
	
	private static final String BLUETOOTH_SCHEME = "bluetooth:";
	
	// if spec is relative, make absolute using context
	public static String resolveUrl( String context, String spec )
		throws IOException
	{
		// horrible, but necessary
		String scheme = null;
		if( context.startsWith( BLUETOOTH_SCHEME ) )
		{
			scheme = BLUETOOTH_SCHEME;
			context = "http:" + context.substring( BLUETOOTH_SCHEME.length() );
		}
		
		URL base = new URL(context);
		String url = new URL( base, spec ).toString();
		if( scheme != null )
		{
			url = scheme + url.substring( "http:".length() );
		}
		return url;
	}
}