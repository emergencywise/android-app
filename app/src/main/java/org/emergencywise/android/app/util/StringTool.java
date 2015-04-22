package org.emergencywise.android.app.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StringTool
{
	private static final Logger logger = Logger.getLogger( StringTool.class.getName() );

	public static String encode( String s )
	{
		if( s == null ) return null;
		
		try
		{
			return URLEncoder.encode( s, "UTF-8" );
		}
		catch( Exception ex )
		{
			logger.log( Level.WARNING, "Failed to encode " + s, ex );
			return null;
		}
	}
	
	public static String decode( String s )
	{
		if( s == null ) return null;
		
		logger.log( Level.FINE, "Decoding " + s );
		
		try
		{
			return URLDecoder.decode( s, "UTF-8" );
		}
		catch( Exception ex )
		{
			logger.log( Level.WARNING, "Failed to decode " + s, ex );
			return null;
		}
	}
		
	public static String[] bifurcate( String source, String cut )
	{
		if( source == null || cut == null ) return new String[]{ source, null };
		
		int p = source.indexOf( cut );
		if( p == -1 ) return new String[]{ source, null };
		
		String head = source.substring( 0, p );
		String tail = source.substring( p + cut.length() );
		
		return new String[]{ head, tail };
	}
	
	/** Remove any null strings */
	public static String[] compact( String[] in )
	{
		// fast check
		int count = 0;
		for( int i = 0; i < in.length; i++ )
		{
			if( in[i] != null ) count++;
		}
		
		// none were zero?
		if( count == in.length ) return in;	// easy case
		
		// otherwise create a new array and fill
		String[] out = new String[ count ];
		int p = 0;
		for( int i = 0; i < in.length; i++ )
		{
			if( in[i] != null ) out[p++] = in[i];
		}
		
		return out;
	}
	
	public static String intern( String s )
	{
		return s == null ? null : s.intern();
	}
	
	public static String asString( InputStream in, boolean close )
	{
		byte[] buf = new byte[ 1024000 ];
		try
		{
			while( true )
			{
				int cb = in.read( buf );
				if( cb == -1 ) return null;
			}
		}
		catch( IOException ex )
		{
			return null; // ignore
		}
		finally
		{
			if( close ) try { in.close(); } catch( Exception ex ) {;}
		}
	}

	public static boolean asBoolean( String s )
	{
		return asBoolean( s, Boolean.FALSE );
	}
	
	public static Boolean asBoolean( String s, Boolean def )
	{
		if( s == null ) return def;
		s = s.trim();
		
		if( "1".equals( s ) ) return true;
		if( "on".equalsIgnoreCase( s ) ) return true;
		if( "true".equalsIgnoreCase( s ) ) return true;
		if( "yes".equalsIgnoreCase( s ) ) return true;
		
		if( "0".equals( s ) ) return false;
		if( "off".equalsIgnoreCase( s ) ) return false;
		if( "false".equalsIgnoreCase( s ) ) return false;
		if( "no".equalsIgnoreCase( s ) ) return false;
		
		return def;
	}
	
	public static boolean hasOnly( String s, String required )
	{
		if( s == null ) return true;	// we didn't fail test, so true?
		
		for( int p = 0; p < s.length(); p++ )
		{
			char c = s.charAt( p );
			if( required.indexOf( c ) == -1 )
			{
				// we found a character NOT in our required list
				return false;
			}
		}
		
		return true;
	}
	
	public static String first( String s, String delim )
	{
		if( s == null || delim == null ) return s;
		
		int p = s.indexOf( delim );
		if( p == -1 ) return s;
		
		String first = s.substring( 0, p );
		return first;
	}
	
	public static String last( String s, String delim )
	{
		if( s == null || delim == null ) return s;
		
		int p = s.lastIndexOf( delim );
		if( p == -1 ) return s;
		
		String last = s.substring( p + delim.length() );
		return last;
	}
	
	public static String[] push( String head, String[] tail )
	{
		String[] result = new String[ tail.length + 1 ];
		
		result[0] = head;
		for( int p = 0; p < tail.length; p++ ) result[p+1] = tail[p];
		
		return result;
	}
	
	public static String capitalize( String s )
	{
		if( s == null || s.length() == 0 ) return s;
		
		String out = ""
					+ Character.toUpperCase( s.charAt( 0 ) )
					+ s.substring( 1 );
		
		return out;
	}
	
	public static String[] merge( String[] a, String[] b )
	{
		if( b == null || b.length == 0 ) return a;
		if( a == null || a.length == 0 ) return b;
		
		Set<String> tmp = new HashSet<String>();
		for( String s : a ) tmp.add( s );
		for( String s : b ) tmp.add( s );
		
		return tmp.toArray( new String[ tmp.size() ] );
	}
	
	public static String[] splitAndClean( String s, String delimiter )
	{
		if( s == null ) return null;
		
		String[] split = s.split( delimiter );
		for( int i = 0; i < split.length; i++ ) split[i] = split[i].trim();
		
		return split;
	}
	
	public static String truncate( double f, int nDigits )
	{
		String s = Double.toString( f );
		int pDot = s.indexOf( '.' );
		s = s.substring( 0, pDot + nDigits );
		return s;
	}	
	
    public static String unquote( String s )
    {
        if( s == null ) return null;
        s = s.trim();
        if( s.length() == 0 ) return s;
        
        int pStart = 0, pEnd = s.length();
        if( s.charAt( 0 ) == '"' ) pStart++;
        if( pEnd > 0 && s.charAt( pEnd - 1 ) == '"' ) pEnd--;
        
        return s.substring( pStart, pEnd );
    }
    
    public static String getToken( String s, int index )
    {
        if( s == null || s.length() == 0 ) return null;
        
        StringTokenizer tok = new StringTokenizer( s );
        while( tok.hasMoreTokens() )
        {
            String sToken = tok.nextToken();
            if( index == 0 ) return sToken;
            --index;
        }
        
        return null;
    }
    
    public static String toString( Object obj )
    {
        return obj == null ? null : obj.toString();
    }
    
    public static int length( String s )
    {
        return s == null ? 0 : s.length();
    }
    
    public static String first( String s, char delimiter )
    {
        if( s == null ) return null;
        
        int p = s.indexOf( delimiter );
        if( p == -1 )
        {
            // no delimiter found, return entire string
            return s;
        }
        else
        {
            return s.substring( 0, p );
        }
    }
    
    public static String left( String s, int maxLength, String sSuffix )
    {
        if( s == null || s.length() <= maxLength )
        {
            return s;
        }
        else
        {
            s = s.substring( 0, maxLength );
            if( sSuffix != null ) s = s + sSuffix;
            return s;
        }
    }
    
	public static boolean isEmpty( String[] s )
	{
		return s == null || s.length == 0 || isEveryEmpty( s );
	}
	
	public static boolean isEveryEmpty( String[] s )
	{
	    for( int i = 0; i < s.length; i++ )
	    {
	        if( isEmpty( s[i] ) != true ) return false;
	    }
	    
	    return true;
	}
	
	public static boolean isEmpty( String s )
	{
		return s == null || s.trim().length() == 0;
	}	
	
	public static boolean compare( String s1, String s2 )
	{
		if( s1 == null )
			return s2 == null;
		else
			return s1.equals( s2 );
	}
	
	public static boolean compare( String s1[], String s2[] )
	{
		if( s1 == null )
			return s2 == null;

		if( s2 == null ) return false;
		if( s1.length != s2.length ) return false;
		for( int i = 0; i < s1.length; i++ )
		{
			if( compare( s1[i], s2[i] ) != true ) return false;						
		}
		
		// passed the gauntlet
		return true;	// success!
	}	
	
	public static String[] zeroDupes( String[] set )
	{
		for( int i = 0; i < set.length; i++ )
		{
			String s = set[i];
			if( s == null ) continue;
			
			for( int n = 0; n < set.length; n++ )
			{
				if( i == n ) continue; // ignore self :)
				if( s.equals( set[n] ) ) set[n] = null;
			}
		}
		
		return set;
	}	
	
	/**
	 * <p>Remove any zero length or null strings from an array.</p>
	 * @param set The array of strings to cull.
	 * @return A set of strings without nulls or zero length strings.
	 */
	public static String[] cull( String[] set )
	{
		if( set == null ) return null;
		if( set.length == 0 ) return set;
		
		// clear the duplicates
		zeroDupes( set );

		// count legitimate entries
		int cValid = 0;
		for( int i = 0; i < set.length; i++ )
		{
			if( set[i] != null && set[i].length() > 0 ) cValid++;
		}
		
		// all OK?
		if( cValid == set.length ) return set;
		
		// go to work...
		
		// easy case?
		if( cValid == 0 ) return null;
		
		// craft new set with valid entries
		String[] result = new String[ cValid ];
		int p = 0;
		for( int i = 0; i < set.length; i++ )
		{
			if( set[i] != null && set[i].length() > 0 )
				result[p++] = set[i]; 
		}

		return result;
	}
	
	public static String asTitle( String s )
	{
		if( s == null || s.length() == 0 ) return "";
	
		char first = Character.toUpperCase( s.charAt( 0 ) );
		
		if( s.length() == 1 )
			return Character.toString( first );
		else
			return "" + first + s.substring( 1 );
	}

	public static Double asDouble( String s )
	{
	    return asDouble( s, 0d );
	}
	
	public static Double asDouble( String s, Double fDefault )
	{
		if( s == null || s.length() == 0 ) return fDefault;
		try
		{
			return Double.parseDouble( s );
		}
		catch( NumberFormatException ex )
		{
			return fDefault;
		}
	}	
	
	public static Float asFloat( String s )
	{
	    return asFloat( s, 0f );
	}
	
	public static Float asFloat( String s, Float fDefault )
	{
		if( s == null || s.length() == 0 ) return fDefault;
		try
		{
			return Float.parseFloat( s );
		}
		catch( NumberFormatException ex )
		{
			return fDefault;
		}
	}		
	
	public static Integer asInteger( String s )
	{
		return asInteger( s, 0 );
	}
	
	public static Integer asInteger( String s, Integer nDefault )
	{
		if( s == null || s.length() == 0 ) return nDefault;
		try
		{
			return Integer.parseInt( s );
		}
		catch( NumberFormatException ex )
		{
			return nDefault;
		}
	}		

	public static Long asLong( String s )
	{
		return asLong( s, 0L );
	}
	
	public static Long asLong( String s, Long nDefault )
	{
		if( s == null || s.length() == 0 ) return nDefault;
		try
		{
			return Long.parseLong( s );
		}
		catch( NumberFormatException ex )
		{
			return nDefault;
		}
	}
	
	// validate entire string is digits
	public static boolean checkDigits( String sNumber )
	{
		if( sNumber == null || sNumber.length() == 0 )
			return false;

		for( int i = 0; i < sNumber.length(); i++ )
		{
			if( Character.isDigit( sNumber.charAt( i ) ) != true ) return false;
		}

		return true;
	}
	
	public static String[] asArray( String s, char delimiter )
	{
		if( s == null ) return null;
		
		String[] as = s.split( "" + delimiter + "" );
		return clean( as );
	}
	
	public static String[] clean( String[] s )
	{
		if( s == null || s.length == 0 ) return null;
		
		// how many have values?
		int c = 0;
		for( int i = 0; i < s.length; i++ )
		{
			if( isEmpty( s[i] ) != true ) c++;
		}
		
		if( c == 0 ) return null;
		
		String[] result = new String[c];
		
		c = 0;
		for( int i = 0; i < s.length; i++ )
		{
			if( isEmpty( s[i] ) != true ) result[c++] = s[i].trim();
		}
		
		
		return result;
	}
	
	public static String clean( String s )
	{
		if( s == null ) return null;
		if( s.length() == 0 ) return null;
		
		s = s.trim();
		if( s.length() == 0 ) return null;

		return s;
	}
	
	public static String asDelimitedString( Collection<?> items, char delimiter )
	{
		return asDelimitedString( items, delimiter, 0 );
	}

	public static String asDelimitedString( Collection<?> items, char delimiter, int index )
	{
		if( items == null || items.size() == 0 ) return "";
		
		StringBuffer buf = new StringBuffer();
		int i = 0;
		for( Object obj : items )
		{
			if (obj==null) continue;
			String sId = obj.toString();
			
			if( i < index ) continue;
			if( i > index ) buf.append( delimiter );
			buf.append( sId );
			
			i++;
		}

		return buf.toString();
	}	

	public static void asDelimitedString( StringBuffer buf, List<?> items, char delimiter )
	{
	    asDelimitedString( buf, items, delimiter, 0 );
	}	
	
	public static void asDelimitedString( StringBuffer buf, List<?> items, char delimiter, int index )
	{
		for( int i = index; i < items.size(); i++ )
		{
			if( i > index ) buf.append( delimiter );
			buf.append( items.get( i ).toString() );
		}
	}	
	
	public static String asDelimitedString( String[] items
	        							, int nOffset
	        							, String sDelimiter )
	{
	    if( items == null || items.length <= nOffset ) return "";
	    
	    StringBuffer buf = new StringBuffer();
	    for( int i = nOffset; i < items.length; i++ )
	    {
	        if( i > nOffset ) buf.append( sDelimiter );
	        buf.append( items[i] );
	    }
	    
	    return buf.toString();
	}

	public static StringBuffer append( StringBuffer buf, String sValue )
	{
		return append( buf, sValue, "," );	
	}
	
	public static StringBuffer append( StringBuffer buf, String sValue, String sDelimiter )
	{
		sValue = clean( sValue );
		if( sValue == null ) return buf;
		
		if( buf == null ) buf = new StringBuffer();
		
		if( buf.length() > 0 ) buf.append( sDelimiter );
		buf.append( sValue );
		
		return buf;
	}
	
	public static boolean contains( String[] set, String sKey )
	{
		if( set == null ) return false;
		for( int i = 0; i < set.length; i++ )
		{
			if( compare( sKey, set[i] ) == true ) return true;						
		}
		
		return false;
	}
	
	public static boolean contains( String sSet, String sValue )
	{
		if( sSet == null ) return false;
		if( sValue == null ) return false;
		int setLength = sSet.length();
		if( setLength == 0 ) return false;
		int valueLength = sValue.length();
		if( valueLength == 0 ) return false;
		
		for( int p = 0;; )
		{
			p = sSet.indexOf( sValue, p );
			if( p == -1 ) return false;
			if( validHead( p, sSet ) && validTail( p + valueLength, sSet ) ) return true;
			
			p += valueLength;
		}
	}
	
	protected static boolean validHead( int p, String sSet )
	{
		if( p == 0 ) return true;
		return sSet.charAt( p - 1 ) == ',';
	}
	
	protected static boolean validTail( int p, String sSet )
	{
		if( p == sSet.length() ) return true;	// at end
		return sSet.charAt( p) == ',';
	}	
	
	public static boolean contains( List<?> set, String sKey )
	{
		if( set == null ) return false;
		for( int i = 0; i < set.size(); i++ )
		{
		    Object entry = set.get( i );
		    if( entry instanceof String )
		    {
		        if( compare( sKey, (String) entry ) == true ) return true;
		    }
		}
		
		return false;
	}	
	
	public static String quote( String sText )
	{
	    if( sText == null )
	        return "\"\"";
	    else
	        return "\"" + sText + "\"";
	}
	
	public static String uriFilter( String sSource )
	{
		return filter( sSource, URI_FILTER );
	}
	
	public static String filter( String sSource, String sFilter )
	{
	    if( sSource == null ) return null;
	    
	    StringBuffer buf = null;
	    for( int i = 0; i < sSource.length(); i++ )
	    {
	        char c = sSource.charAt( i );
	        if( sFilter.indexOf( c ) == -1 )
	        {
	            // we need a modified string
	            if( buf == null )
	            {
	                // fab the new buffer
	                buf = new StringBuffer();
	                if( i > 0 )
	                {
	                    // add the previous characters
	                    for( int n = 0; n < i; n++ )
	                    {
	                        buf.append( sSource.charAt( n ) );
	                    }
	                }
	            }
	            
	            // ignore the invalid character
	        }
	        else
	        {
	            // legitimate character, if we're building a buffer, add it
	            if( buf != null ) buf.append( c );
	        }
	    }
	    
	    if( buf == null )
	    {
	        // means no changes were found
	        return sSource;
	    }
	    else
	    {
	        // return the new string we've built
	        return buf.toString();
	    }
	}
	
	public static String trim( String s )
	{
	    if( s == null ) return null;
	    return s.trim();
	}
	public static String limit(String str, int maxLength){
		if (str==null) return null;
		if (str.length()<=maxLength) return str;
		if (maxLength>str.length()) maxLength = str.length();	
		return str.substring(0,maxLength)+"...";
	}
	
	public static final String ALPHANUM_FILTER = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String URI_FILTER = ALPHANUM_FILTER + "-_.!~*'()";
}