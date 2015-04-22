package org.emergencywise.android.app.util;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class Filer
{
	private static final String TAG = Filer.class.getName();
	public static final String APP_ROOT = "emergencywise/";
	private File root;
	public enum Type { UPLOAD, AOCACHE, AO, CACHE, INTERNAL, ROOT };
	
	private Filer() {}
	
	public static Filer create( Context context, boolean writable, Type type )
	{
		return create( context, writable, type, null );
	}
	
	public static Filer create( Context context, boolean writable, Type type, String name )
	{
		Filer filer = new Filer();
		
		// is the sdcard available?  if not fallback to internal storage
		File storage;
		boolean useSD = isExternalStorageWritable();
		if( useSD )
		{
			storage = Environment.getExternalStorageDirectory();
		}
		else
		{
			storage = context.getFilesDir();
		}
		
		switch( type )
		{
			case ROOT:
				filer.root = new File( storage, APP_ROOT );
				if( name != null ) filer.root = new File( filer.root, name );
				break;
				
			case UPLOAD:
				filer.root = new File( storage, APP_ROOT + "upload/" );
				if( name != null ) filer.root = new File( filer.root, name );
				break;
				
			case AOCACHE:
				filer.root = new File( storage, APP_ROOT + "aocache/" );
				if( name != null ) filer.root = new File( filer.root, name );
				break;

			case AO:
				filer.root = new File( storage, APP_ROOT + "ao/" );
				if( name != null ) filer.root = new File( filer.root, name );
				break;

            /*
			case MAPTILE:
				filer.root = new File( storage, APP_ROOT + "maptile/" );
				if( name != null ) filer.root = new File( filer.root, name );
				break;
            */

			case CACHE:
				filer.root = useSD ? context.getExternalCacheDir() : context.getCacheDir();
				break;
				
			case INTERNAL:
				filer.root = context.getFilesDir();
				break;

			default:
				return null;
		}
		
		// make sure full path exists
		ensurePath( filer.root );
		
		return filer;
	}
	
	private static void ensurePath( File path )
	{
		File parent = path.getParentFile();
		if( parent != null ) ensurePath( parent );
		
		if( path.exists() != true )
		{
			if( !path.mkdir() )
			{
			    Log.e(TAG, "Failed to create last directory in path " + path);
			}
		}
	}
	
	public File getDirectory()
	{
		return root;
	}
	
	public File save( String filename, byte[] raw )
		throws IOException
	{
		File f = new File( root, filename );
		save( f, raw );
		return f;
	}
	
	public File save( String subdir, String filename, byte[] raw )
		throws IOException
	{
		File dir = mkdir( subdir );
		File out = new File( dir, filename );
		return save( out, raw );
	}
	
	public static File save( File f, byte[] raw )
		throws IOException
	{	
		OutputStream out = null;
		try
		{
			out = new BufferedOutputStream( new FileOutputStream( f ) );
			out.write( raw );
			
			return f;
		}
		finally
		{
			if( out != null ) try { out.close(); } catch( Exception ex ) {;}
		}
	}
	
	public File mkdir( String dir )
	{
		File f = new File( root, dir );
		if( f.exists() != true )
		{
			f.mkdir();
		}
		
		return f; 
	}
	
	public byte[] load( String filename )
		throws IOException
	{
		File f = new File( root, filename );
		return load( f );
	}
		
	public byte[] load( File f )
		throws IOException
	{
		FileInputStream in = null;
		try
		{
			in = new FileInputStream( f );
			
			ByteArrayOutputStream buf = new ByteArrayOutputStream( (int) f.length() );
			byte[] tmp = new byte[16384];
			for( int cb = in.read( tmp ); cb > -1; cb = in.read( tmp ) )
			{
				buf.write( tmp, 0, cb );
			}
			
			return buf.toByteArray();
		}
		finally
		{
			if( in != null ) try { in.close(); } catch( Exception ex ) {;}
		}
	}

    /*
	public MediaFile createMediaFile( RecordId recordId, String extension )
	{
		// make sure directory <uid> exists
		File dir = new File( root, recordId.getUserId() );
		if( dir.exists() != true )
		{
			dir.mkdir();
		}
		
		// assuming a small number of media files for each record, walk from zero up to
		// find an unused index
		for( int index = 0; index < 20; index++ )
		{
			String filename = recordId.toString() + "-media(" + index + ')' + extension;
			File f = new File( dir, filename );
			if( f.exists() ) continue;
			
			return new MediaFile( recordId, f.getAbsolutePath(), null );
		}
		
		// wierd, ok just use a big number
		String filename = recordId.toString() + "-media(" + System.currentTimeMillis() + ')' + extension;
		return new MediaFile( recordId, new File( dir, filename ).getAbsolutePath(), null );
	}
	*/
	
	public static boolean isExternalStorageWritable()
	{
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED.equals(state);
	}

	public static boolean isExternalStorageReadOnly()
	{
		String state = Environment.getExternalStorageState();
		return Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
	}	
	
    // for any "ao" schemed uris, convert to file://...
    public static String toUrl( Uri uri, Context ctx )
    {
    	String url;
        if( uri.getScheme().equals( "ao" ) )
        {
        	String ao = uri.getAuthority();
        	File aodir = Filer.create( ctx, false, Type.AO, ao ).getDirectory();
        	url = new File( aodir, uri.getPath() ).toURI().toString();
        }
        else
        {
            url = uri.toString();	
        } 
        
        return url;
    }
    
	public static String fromFile( File src )
		throws IOException
	{
		FileInputStream stream = new FileInputStream(src);
		try
		{
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map( FileChannel.MapMode.READ_ONLY, 0, fc.size() );
			
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} 
		finally
		{
			stream.close();
		}
	}
	
	public static boolean deleteDir( File dir )
	{
	    if( dir.isDirectory() )
	    {
	        for( String child : dir.list() )
	        {
	            if( !deleteDir( new File(dir, child ) ) )
	            {
	            	return false;
	            }
	        }
	    }

	    // The directory is now empty so delete it
	    if( dir.delete() ) return true;
	    
	    Log.e( TAG, "Failed to delete " + dir );
	    return false;
	}
	
	public static void setupNoMedia( File dir )
	{
        File nomedia = new File( dir, ".nomedia" );
        if( !nomedia.exists() )
        {
        	try
        	{
	        	if( !nomedia.createNewFile() )
	        	{
	                Log.e( TAG, "Failed to block media indexing with .nomedia file" );
	        	}
        	}
        	catch( IOException ex )
        	{
                Log.e( TAG, "Failed to block media indexing with .nomedia file", ex );
        	}
        }	
	}
}