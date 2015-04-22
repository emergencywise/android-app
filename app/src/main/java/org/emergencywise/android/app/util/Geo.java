package org.emergencywise.android.app.util;

public class Geo
{
	private Geo() {}
	
	public static final double LAT_DIVISOR = 1e7;
	public static final double LNG_DIVISOR = 1e7;
	
	private static double deg2rad( double deg )
	{
		double conv_factor = (2.0 * Math.PI) / 360.0;
		return deg * conv_factor;
	}

	public static double longitudeDegreeLength( double latitude ) // Compute length of degrees in meters
	{
		double p1 = 111412.84; // longitude calculation term 1
		double p2 = -93.5; // longitude calculation term 2
		double p3 = 0.118; // longitude calculation term 3
		
		// Convert latitude to radians
		double lat = deg2rad( latitude );

		double longlen = (p1 * Math.cos( lat )) + (p2 * Math.cos( 3 * lat )) + (p3 * Math.cos( 5 * lat ));
		return longlen;
	}
	
	public static int distance( int lat1, int lng1, int lat2, int lng2 )
	{
		return (int) distance( lat1 / 1e6, lng1 / 1e6, lat2 / 1e6, lng2 / 1e6 );
	}
	
	public static float distance( double lng1, double lat1, double lng2, double lat2 )
	{
	    double earthRadius = 3958.75;
	    double dLat = Math.toRadians(lat2-lat1);
	    double dLng = Math.toRadians(lng2-lng1);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;

	    int meterConversion = 1609;

	    return new Float(dist * meterConversion).floatValue();
	}
	
	public static Integer toE6( String s )
	{
		if( s == null ) return null;
		Double d = StringTool.asDouble(s, null);
		if( d == null ) return null;
		return (int) (d * 1e6);
	}
}