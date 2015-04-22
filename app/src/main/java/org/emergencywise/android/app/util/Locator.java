package org.emergencywise.android.app.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

public class Locator
{
	private static final String TAG = Locator.class.getName();
	private static final int SECOND_MILLIS = 1000;
	private static final int METERS_100 = 100;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private boolean running = false;
	private int locationCheckInterval;
	//private static LinkedList<Waypoint> track = new LinkedList<Waypoint>();
	private Context context;
	
	public Locator( Context context )
	{
		this.context = context;
		locationManager = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );		
	}
	
	public void start()
	{
		start( 120, METERS_100, null );
	}
	
	public void start( int intervalSeconds, int distance, LocationListener listener )
	{
		locationListener = new LocationListenerProxy( listener );
		locationCheckInterval = intervalSeconds * SECOND_MILLIS;
		//locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, locationCheckInterval, distance, locationListener);
		locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, locationCheckInterval, distance, locationListener );
		
		Toast.makeText( context, "GPS tracking every " + intervalSeconds + " seconds or " + distance + " meters", Toast.LENGTH_SHORT ).show();
		
		running = true;
	}
	
	public void stop()
	{
		if( locationListener == null ) return;
		locationManager.removeUpdates( locationListener );
		
		Toast.makeText( context, "GPS tracking stopped", Toast.LENGTH_SHORT ).show();
		
		running = false;
	}	
	
	public boolean isRunning()
	{
		return running;
	}

    /*
	public GeoPoint getGeoPoint()
	{
		Location location = getLocation();
		if( location == null ) return null;
		
	    Double latitude = location.getLatitude()*1E6;
	    Double longitude = location.getLongitude()*1E6;
	    return new GeoPoint( latitude.intValue(), longitude.intValue() );
	}*/
	
	// provide last known location
	public Location getLocation()
	{
		return getLocation( locationManager, locationCheckInterval );
	}
	
	//private static final String[] LOCATION_PROVIDERS = { LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER }; 
	
	// provide last known location
	public static Location getLocation( Context context, int gpsWaitTime )
	{
		LocationManager lm = (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
		return getLocation( lm, gpsWaitTime );
	}
	
	private static Location getLocation( LocationManager lm, int gpsWaitTime )
	{	
		/*
		for( String provider : LOCATION_PROVIDERS )
		{
			Location fix = lm.getLastKnownLocation( provider );
			if( fix != null ) return fix;
		}
		*/
		
		if( lm == null )
		{
			return null;
		}
		final Location gpsLocation = lm.getLastKnownLocation( LocationManager.GPS_PROVIDER );
		final Location networkLocation = lm.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
		if( gpsLocation == null )
		{
			return networkLocation;
		}
		else if( networkLocation == null )
		{
			return gpsLocation;
		}
		else
		{
			// both are non-null - use the most recent
			if( networkLocation.getTime() > gpsLocation.getTime() + gpsWaitTime )
			{
				return networkLocation;
			}
			else
			{
				return gpsLocation;
			}
		}
	}

    /*
	public static Waypoint asWaypoint( Location location )
	{
		Waypoint wp = new Waypoint( location.getLongitude(), location.getLatitude() );
		if( location.hasAccuracy() ) wp.setAccuracy( location.getAccuracy() );
		if( location.hasAltitude() ) wp.setAltitude( location.getAltitude() );
		if( location.hasBearing() ) wp.setBearing( location.getBearing() );
		if( location.hasSpeed() ) wp.setSpeed( location.getSpeed() );
		wp.setTime( new Date( location.getTime() ) );
		return wp;
	}
    */

	private class LocationListenerProxy
		implements LocationListener
	{
		private LocationListener listener;
		public LocationListenerProxy( LocationListener listener )
		{
			this.listener = listener;
		}
		
	    public void onLocationChanged(Location location) {
	    	Log.i(TAG, "Found location " + location);
	    	//addTrackPoint( location );
	    	if( listener != null ) listener.onLocationChanged( location );
	    }

	    public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}
	    public void onProviderEnabled(String provider) {}
	    public void onProviderDisabled(String provider) {}
	};
	
	//
	// track management
	/*
	private static final int MAX_TRACK_SIZE = 20;
	
	public static Waypoint[] getTrack()
	{
		synchronized( track )
		{
			return track.toArray( new Waypoint[ track.size() ] );
		}
	}
	
	private void addTrackPoint( Location location )
	{
		synchronized( track )
		{
			while( track.size() > MAX_TRACK_SIZE )
			{
				track.poll();
			}
			
			track.add( asWaypoint( location ) );
		}
	}*/
}