package org.emergencywise.android.app.util;

import java.util.Arrays;

// A north,east,south,west box
public class Box
{
	private static final double latDegMeters = 40075000f / 360;
	public static int[] fromRadius( int lngE6, int latE6, int radius )
	{
		double lngDegMeters = Geo.longitudeDegreeLength(latE6 / 1e6f);
		int lngRadius = (int) (radius * (latDegMeters / lngDegMeters));
		return new int[] { latE6 + radius, lngE6 + lngRadius, latE6 - radius, lngE6 - lngRadius };
	}
	
	public static String toString( int[] box )
	{
		if( box == null ) return null;
		
		StringBuilder out = new StringBuilder( "[" );
		for( int i = 0; i < box.length; i++ )
		{
			if( out.length() > 1 ) out.append( ',' );
			out.append( box[i] );
		}
		out.append( ']' );
		return out.toString();
	}
	
	// combine all the boxes into one big one
	public static int[] combine( int[][] box )
	{
		if( box == null ) return null;
		
		int[] result = Arrays.copyOf( box[0], box[0].length );
		for( int i = 1; i < box.length; i++ )
		{
			int[] b = box[i];
			stretch( result, b[1], b[0] );
			stretch( result, b[3], b[2] );
		}
		
		return result;
	}
	
	// stretch the first box to cover the second box
	public static void stretch( int[] result, int[] b )
	{
		stretch( result, b[1], b[0] );
		stretch( result, b[3], b[2] );	
	}
	
	// stretch only the first box
	public static void stretch( int[][] box, int x, int y )
	{
		stretch( box[0], x, y );
	}
	
	// box is n,e,s,w
	public static void stretch( int[] box, int x, int y )
	{
		if( box[0] < y ) box[0] = y;  // n
		if( box[1] < x ) box[1] = x;  // e
		if( box[2] > y ) box[2] = y;  // s
		if( box[3] > x ) box[3] = x;  // w
	}
	
	// poly is [noncontiguous][[outer][inner1][inner2]]
	public static int[][] fromPoly( int[][][] poly )
	{
		int[][] boxes = new int[ poly.length ][];
		for( int i = 0; i < poly.length; i++ )
		{
			boxes[i] = fromPoly( poly[i][0] );
		}
		
		return boxes;
	}
	
	// convert polygon into box
	public static int[] fromPoly( int[] poly )
	{
		if( poly == null || poly.length < 2 ) return null;
		
		int n = poly[1];
		int e = poly[0];
		int s = n;
		int w = e;
		
		for( int i = 0; i < poly.length; )
		{
			int lngE6 = poly[i++];
			if( lngE6 > e ) e = lngE6;
			if( lngE6 < w ) w = lngE6;
			
			int latE6 = poly[i++];
			if( latE6 > n ) n = latE6;
			if( latE6 < s ) s = latE6;
		}
		
		return new int[] { n, e, s, w };
	}
	
	public static boolean overlaps( int[][] box1, int[] box2 )
	{
		// sanity
		if( box1 == null || box1[0] == null )
		{
			return false;
		}
		
		for( int[] box : box1 )
		{
			if( overlaps( box, box2 ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	// derived from http://rbrundritt.wordpress.com/2009/10/03/determining-if-two-bounding-boxes-overlap/
	public static boolean overlaps( int[] box1, int[] box2 )
	{
		// sanity
		if( box1 == null || box2 == null || box1.length < 4 || box2.length < 4 )
		{
			return false;
		}
		
        // First bounding box, top left corner, bottom right corner
        int aTLx = box1[3]; // w bb1.TopLeftLatLong.Longitude;
        int aTLy = box1[0]; // n bb1.TopLeftLatLong.Latitude;
        int aBRx = box1[1]; // e bb1.BottomRightLatLong.Longitude;
        int aBRy = box1[2]; // s bb1.BottomRightLatLong.Latitude;

        // Second bounding box, top left corner, bottom right corner
        int bTLx = box2[3]; // w bb2.TopLeftLatLong.Longitude;
        int bTLy = box2[0]; // n bb2.TopLeftLatLong.Latitude;
        int bBRx = box2[1]; // e bb2.BottomRightLatLong.Longitude;
        int bBRy = box2[2]; // s bb2.BottomRightLatLong.Latitude;
        
        int rabx = Math.abs( aTLx + aBRx - bTLx - bBRx );
        int raby = Math.abs( aTLy + aBRy - bTLy - bBRy );

        //rAx + rBx
        int raxPrbx = aBRx - aTLx + bBRx - bTLx;

        //rAy + rBy
        int rayPrby = aTLy - aBRy + bTLy - bBRy;

        if(rabx <= raxPrbx && raby <= rayPrby)
        {
        	return true;
        }
        return false;
	}
	
	public static int containsXY( int[][] boxes, int x, int y )
	{
		if( boxes == null ) return -1;
		
		for( int i = 0; i < boxes.length; i++ )
		{
			int[] box = boxes[i];
			
			if( containsXY( box, x, y ) ) return i;
		}
		
		return -1;
	}
	
	public static boolean containsXY( int[] box, int x, int y )
	{
		int n = box[0], e = box[1], s = box[2], w = box[3];
		
		if( y > n || y < s ) return false;
		if( x > e || x < w ) return false;
		
		return true;
	}
	
	private static double LAT_MILLIS_TO_METERS = 111120f / 1e6;
	
	// n,e,s,w
	public static float size( int[] box )
	{
		// E6 height and width
		int height = box[0] - box[2];
		int width = box[1] - box[3];
		
		// how many meters/deg at this latitude?
		double midLat = (box[2] + height / 2) / 1e6;
		double lngDegreeMeters = Geo.longitudeDegreeLength( midLat );
		double lngMillisToMeters = lngDegreeMeters / 1e6;
		
		int yMeters = (int) (height * LAT_MILLIS_TO_METERS );
		int xMeters = (int) (width * lngMillisToMeters );
		
		float size = xMeters * yMeters;
		return size;	
	}
	
	// n,e,s,w -> x,y
	public static int[] center( int[] box )
	{
		// E6 height and width
		int height = box[0] - box[2];
		int width = box[1] - box[3];
		
		int[] center = new int[2];
		center[0] = box[3] + width / 2;		// x
		center[1] = box[2] + height / 2;	// y
		return center;
	}
}