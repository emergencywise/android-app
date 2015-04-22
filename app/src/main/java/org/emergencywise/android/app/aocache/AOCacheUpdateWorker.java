package org.emergencywise.android.app.aocache;

import android.content.Context;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.emergencywise.android.app.MyPreferences;
import org.emergencywise.android.app.aocache.model.AOManifest;
import org.emergencywise.android.app.util.Filer;
import org.emergencywise.android.app.util.Filer.Type;
import org.emergencywise.android.app.util.HttpTool;
import org.emergencywise.android.app.util.LocalLogger;
import org.emergencywise.android.app.util.StringTool;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class AOCacheUpdateWorker
{
	private Context context;
	private static final String UTF8 = "UTF-8";
	private static final String TAG = AOCacheUpdateWorker.class.getName();
	
	public AOCacheUpdateWorker( Context context )
	{
		this.context = context;
	}
	
	public boolean syncAO( String ao, String manifestUrl, boolean dryrun, LocalLogger logr )
	{
		MyPreferences preferences = new MyPreferences( context );
		if( manifestUrl == null )
		{
			manifestUrl = preferences.getAoManifestUrl();
		}

		StringBuilder tmp = new StringBuilder( manifestUrl );
		tmp.append( tmp.indexOf( "?" ) == -1 ? "?id=" : "&id=" );
		try
		{
			tmp.append( URLEncoder.encode( ao, UTF8 ) );
		}
		catch( UnsupportedEncodingException ex )
		{
			String error = "Failed to encode " + ao;
			Log.e(TAG, error, ex);
			if( logr != null ) logr.e( error );
			return false;
		}
		
		URL url;
		try
		{
			url = new URL( tmp.toString() ); 
		}
		catch( MalformedURLException ex )
		{
			String error = "Failed to construct sync url from " + tmp; 
			Log.e( TAG, error, ex );
			if( logr != null ) logr.e( error, ex );
			return false;
		}
		
		return sync( url, dryrun, logr );		
	}
	
	public void syncAll()
	{
		MyPreferences preferences = new MyPreferences( context );
		String manifestUrl = preferences.getAoManifestUrl();
		
		// each directory in cache is the id of an AO to sync
		File cache = Filer.create(context, false, Type.AOCACHE).getDirectory();
		StringBuilder tmp = new StringBuilder( manifestUrl );
		File[] files = cache.listFiles();
		if( files.length == 0 )
		{
			Log.d( TAG, "No aos cached yet" );
			return;
		}
		for( File f : files )
		{
			tmp.append( tmp.indexOf( "?" ) == -1 ? "?id=" : "&id=" );
			try
			{
				tmp.append( URLEncoder.encode( f.getName(), UTF8 ) );
			}
			catch (UnsupportedEncodingException ex )
			{
				Log.e(TAG,  "Failed to encode " + f.getName(), ex  );
			}
		}
		URL url;
		try
		{
			url = new URL( tmp.toString() ); 
		}
		catch( MalformedURLException ex )
		{
			Log.e( TAG, "Failed to construct sync url", ex );
			return;
		}
		
		sync( url, false, null );
	}
	
	private boolean sync( URL url, boolean dryrun, LocalLogger logr )
	{
		// sync with server to see what needs to be uploaded
		JSONObject json = JsonClient.getInstance().get( url.toString(), null, null );
		JSONObject failure = json.optJSONObject( "failure" );
		if( failure != null ) 
		{
			String error = "Failed to sync with " + url + ": " + failure;
			Log.e( TAG, error );
			if( logr != null ) logr.e( error );
			return false;
		}
		JSONArray result = json.optJSONArray( "result" );
		if( result == null || result.length() == 0 )
		{
			String error = "Sync had empty result " + url;
			Log.e( TAG, error );
			if( logr != null ) logr.e( error );
			return false;			
		}
		
		boolean success = true;
		for( int i = 0; i < result.length(); i++ )
		{
			if( !sync( url, result.optJSONObject( i ), dryrun, logr ) )
			{
				success = false;
			}
		}
		
		return success;
	}
	
	private static class Package
	{
		int version;
		String name;
		String url;
		long size;
		
		Package( JSONObject p )
		{
			version = p.optInt( "version" );
			name = p.optString( "name" );
			url = p.optString( "url" );
			size = p.optLong( "size" );
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Package> getPackages( JSONArray json )
	{
		if( json == null || json.length() == 0 ) return Collections.EMPTY_LIST;
		
		List<Package> result = new ArrayList<Package>();
		for( int i = 0; i < json.length(); i++ )
		{
			JSONObject jo = json.optJSONObject( i );
			if( jo == null ) continue;
			result.add( new Package( jo ) );
		}
		
		return result;
	}
	
	private static class UnzipItem
	{
		String name;
		File zipfile;
		
		public UnzipItem( String name, File zipfile )
		{
			this.name = name;
			this.zipfile = zipfile;
		}
	}
	
	public boolean verify( AOManifest manifest, LocalLogger logr )
	{
		boolean success = true;	// optimistic		
		File aocache = Filer.create( context, false, Type.AOCACHE, manifest.getId() ).getDirectory();
		
		for( AOManifest.Package p : manifest.getPackages() )
		{
			File zipfile = AOCacheManager.asPackageFile(aocache, p.getName(), p.getVersion());
			if( !AOCacheManager.verify( context, manifest.getId(), p.getName(), zipfile, logr ) )
			{
				success = false;
			}
		}
		
		return success;
	}
	
	private boolean sync( URL contextUrl, JSONObject manifest, boolean dryrun, LocalLogger logr )
	{
		if( manifest == null ) return false;
		
		// remember the packages to unzip
		List<UnzipItem> unzipList = new ArrayList<UnzipItem>();
		
		// see which packages need updating
		String ao = manifest.optString( "id" );
		File aocache = Filer.create( context, false, Type.AOCACHE, ao ).getDirectory();
		List<Package> packages = getPackages( manifest.optJSONArray( "packages" ) );
		boolean success = true;	// optimist
		for( Package p : packages )
		{
			// if the file is missing or the wrong size, then fetch it
			File pfile = AOCacheManager.asPackageFile( aocache, p.name, p.version );
			if( !pfile.exists() || pfile.length() != p.size )
			{
				if( dryrun )
				{
					String error = "Missing package file: " + pfile;
					Log.e( TAG, error );
					if( logr != null ) logr.e( error );
					success = false;
					continue;
				}
				
				// we need to fetch it
				URL fetchUrl;
				try
				{
					fetchUrl = new URL( contextUrl, p.url );
				}
				catch( MalformedURLException ex )
				{
					String error =  "Failed to create package download url from " + contextUrl + " and " + p.url;
					Log.e( TAG, error, ex );
					if( logr != null ) logr.e( error );
					success = false;
					continue;
				}
				
				if( downloadPackage( fetchUrl.toString(), pfile, logr ) )
				{
					unzipList.add( new UnzipItem( p.name, pfile ) );
				}
				else
				{
					success = false;	// remember there was a problem
				}
			}
			else
			{
				// if we already have the package file, verify all the files uncompressed ok
				if( !AOCacheManager.verify( context, ao, p.name, pfile, logr ) )
				{
					// give the unzip another try
					String error = "Unzipped package failed verificiation; will try uncompressing again: " + pfile;
					Log.w( TAG, error );
					if( logr != null ) logr.w( error );
					
					unzipList.add( new UnzipItem( p.name, pfile ) );
				}
			}
		}
		
		if( !success )
		{
			String error = "Aborting update of " + ao + " due to missing package";
			Log.e( TAG, error );
			if( logr != null ) logr.e( error );
			return false;
		}
		
		// if it's a dry run, test unzipping packages and comparing to files on disk
		if( dryrun )
		{
			for( UnzipItem i : unzipList )
			{
				if( !AOCacheManager.verify( context, ao, i.name, i.zipfile, logr ) )
				{
					success = false;
				}
			}			
			
			return success;
		}
		
		// save manifest to cache folder
		File file = new File( aocache, "manifest.json" );
		try
		{
			OutputStream out = new FileOutputStream( file );
			out.write( manifest.toString().getBytes() );
			out.close();
		}
		catch( IOException ex )
		{
			String error = "Failed to write manifest file " + file;
			Log.e( TAG, error, ex );
			if( logr != null ) logr.e( error, ex );
			return false;
		}
		
		// remove old versions of packages and packages that are no longer used
		for( File f : aocache.listFiles() )
		{
			Matcher m = AOCacheManager.packageFilePattern.matcher( f.getName() );
			if( !m.find() ) continue;	// not the package filename pattern

			String name = m.group( 1 );
			int version = StringTool.asInteger(m.group(2), -1);
			
			// does this name and version exist in the new manifest
			if( !isInManifest( name, version, packages ) )
			{
				if( f.delete() )
				{
		        	Log.d( TAG, "Deleted unused or outdated package " + f.getName() );	
				}
				else
				{
					String warning = "Failed to delete unused or outdated package: " + f.getName();
					Log.w( TAG, warning );
					if( logr != null ) logr.w( warning );
				}
			}
		}
		
		// finally... update unpacked packages in citizen911/ao/...
		for( UnzipItem i : unzipList )
		{
			if( !AOCacheManager.makeReady( context, ao, i.name, i.zipfile, logr ) )
			{
				success = false;
			}
		}

        /*
		if( success )
		{
			MyApplication.getSingleton().getZoneManager( true ).reload();
		}
		*/
		
		return success;
	}
	
	private boolean isInManifest( String name, int version, List<Package> packages )
	{
		for( Package p : packages )
		{
			if( name.equals( p.name ) && version == p.version )
			{
				return true;
			}
		}
		
		return false;
	}
	
	private boolean downloadPackage( String url, File file, LocalLogger logr )
	{
    	Log.e( TAG, "Starting download of " + url + " to " + file );
		try
		{
		    HttpGet request = new HttpGet( url );
		    HttpResponse response = HttpTool.getClient().execute( request );
		    HttpEntity entity = response.getEntity();
		    if( entity == null )
		    {
		    	String error = "Problem downloading package " + url + " to " + file + ": no entity from server";
	        	Log.e( TAG, error );
	        	if( logr != null ) logr.e( error );
	        	return false;			    		
		    }
		    
	        InputStream in = entity.getContent();
	        OutputStream out = new FileOutputStream( file );
	        try
	        {
	        	long size = 0;
	            byte[] tmp = new byte[65536];
	            for( int cb = in.read( tmp ); cb > -1; cb = in.read( tmp ) )
	            {
	            	out.write( tmp, 0, cb );
	            	size += cb;
	            }
	            
	        	Log.d( TAG, "Finished downloading package " + url + " to " + file + " of length " + size );
	            return true;
	        }
	        finally
	        {
	            try { in.close(); } catch( Exception ex ) {;}
	            try { out.close(); } catch( Exception ex ) {;}
	        }
		}
	    catch( Exception ex )
	    {
	    	String error = "Problem downloading package " + url + " to " + file;
        	Log.e( TAG, error, ex );
        	if( logr != null ) logr.e( error + ": " + ex.getMessage() );
        	return false;	    	
	    }
	}
}