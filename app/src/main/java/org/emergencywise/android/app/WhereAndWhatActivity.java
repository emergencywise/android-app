package org.emergencywise.android.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.emergencywise.android.app.aocache.AOCacheManager;
import org.emergencywise.android.app.aocache.model.AOManifest;
import org.emergencywise.android.app.aocache.model.NamedUrl;
import org.emergencywise.android.app.util.Box;
import org.emergencywise.android.app.util.Filer;
import org.emergencywise.android.app.util.StringTool;
import org.xml.sax.Locator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class WhereAndWhatActivity
	extends BaseActivity
{
	private static final String TAG = WhereAndWhatActivity.class.getName();
	private List<AOManifest> aoList = new ArrayList<AOManifest>();
	private ArrayAdapter<AOManifest> aoAdapter;
	private List<NamedUrl> menuItems = new ArrayList<NamedUrl>();
	private ArrayAdapter<NamedUrl> menuAdapter;
	
	private Spinner aoSpinner;
	private Spinner menuSpinner;
	
	private AOManifest manifest;
	private String path;
	private boolean debouncePath = false;
	
	public void onCreate( Bundle savedInstanceState )
	{
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.where_and_what );
	    
	    // setup available AOs 
	    aoSpinner = (Spinner) findViewById(R.id.ao_select );
	    aoSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
			{
				manifest = aoList.get( position );

				populateMenu( manifest, path );
				switchLogo( manifest );
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});

	    aoSpinner.setAdapter( aoAdapter = new ArrayAdapter<AOManifest> ( this, android.R.layout.simple_spinner_item, aoList )
		{
	        @Override
	        public View getView( int position, View convertView, ViewGroup parent ) 
	        {
	            View v = convertView;
	            if (v == null)
	            {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate( android.R.layout.simple_spinner_item, null);
	            }
	            
	            AOManifest item = getItem( position );
	            if( item != null ) 
	            {
	                TextView tv = (TextView) v.findViewById( android.R.id.text1 );
	                tv.setText( item.getTitle() );
	            }
	            return v;
	        }	
	        
	        @Override
	        public View	getDropDownView( int position, View convertView, ViewGroup parent)
	        {
	            View v = convertView;
	            if (v == null)
	            {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate( android.R.layout.simple_spinner_dropdown_item, null);
	            }
	            
	            AOManifest item = getItem( position );
	            if( item != null ) 
	            {
	                TextView tv = (TextView) v.findViewById( android.R.id.text1 );
	                tv.setText( item.getTitle() );
	            }
	            return v;	        	
	        }
		} );
		
		// setup menu spinner and adapter 
	    menuSpinner = (Spinner) findViewById(R.id.ao_top_menu );
	    menuSpinner.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id )
			{
	            NamedUrl item = menuItems.get( position );
	            
	            path = item.getUrl();
	            if( debouncePath )
	            {
	            	debouncePath = false;
	            }
	            else
	            {
	            	nextActivity();
	            }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0)
			{
			}
		});
	    
	    menuSpinner.setAdapter( menuAdapter = new ArrayAdapter<NamedUrl>( this, android.R.layout.simple_spinner_item, menuItems )
	    {
	        @Override
	        public View getView(int position, View convertView, android.view.ViewGroup parent) 
	        {
	            View v = convertView;
	            if (v == null)
	            {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate( android.R.layout.simple_spinner_item, null);
	            }
	            
	            NamedUrl item = getItem( position );
	            if( item != null ) 
	            {
	                TextView tv = (TextView) v.findViewById( android.R.id.text1 );
	                tv.setText( item.getName() );
	            }
	            return v;
	        }   
	        
	        @Override
	        public View	getDropDownView( int position, View convertView, ViewGroup parent)
	        {
	            View v = convertView;
	            if (v == null)
	            {
	                LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	                v = vi.inflate( android.R.layout.simple_spinner_dropdown_item, null);
	            }
	            
	            NamedUrl item = getItem( position );
	            if( item != null ) 
	            {
	                TextView tv = (TextView) v.findViewById( android.R.id.text1 );
	                tv.setText( item.getName() );
	            }
	            return v;	        	
	        }
	    });
	    
		Button button = (Button) findViewById( R.id.button_continue );
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				nextActivity();
			}
		});
	}
	
	private void nextActivity()
	{
		// do we have a path?
		if( path == null )
		{
			Toast.makeText( this, R.string.no_top_menu_selection, Toast.LENGTH_LONG ).show();
			return;
		}
		
		// set app context to this ao
		MyApplication.getInstance( WhereAndWhatActivity.this ).setAOManifest( manifest );
		
		// render next page or start an activity
        Intent intent = new Intent( WhereAndWhatActivity.this, com.citizen911.android.util.WebViewActivity.class );
        Uri uri = AOCacheManager.asUri(manifest.getId(), path);
        intent.setData( uri );
		startActivity( intent );		
	}
	
	public void onResume()
	{
	    super.onResume();
	    
	    // reload manifest list in case new aos were added
	    aoList.clear();
	    List<AOManifest> list = AOCacheManager.getManifestList( this );
	    if( list != null )
	    {
	    	aoList.addAll( list );
	    }
	    if( aoList.isEmpty() )
	    {
	    	// if there are no AOs, then ask them to get some
	    	startActivityForResult( new Intent( this, com.citizen911.android.aocache.SelectAOIntroductionActivity.class ), REQUEST_AREA_LOAD );
	    	return;
	    }
	    
	    sortAreas( aoList );

	    // find current ao in list
	    Integer index = getPosition( manifest );
	    if( index == null )
	    {
	    	// defaults
	    	manifest = aoList.get( 0 );
	    	index = 0;
	    	path = null;
	    }
	    
	    switchLogo( manifest );
	    aoSpinner.setSelection( index );
	    aoAdapter.notifyDataSetChanged();
	}
	
	private static final int REQUEST_AREA_LOAD = 0;
	
	@Override
	protected void onActivityResult( int requestCode, int resultCode, Intent data )
	{
		if( requestCode != REQUEST_AREA_LOAD ) return; // huh?
		
		if( resultCode == Activity.RESULT_CANCELED )
		{
			// they gave up, so finish this task too
			finish();
			return;
		}
	}
	
	private static class SortableManifest
	{
		AOManifest manifest;
		int score;
		
		public String toString() { return "" + score + ": " + manifest.getTitle(); }
	}
	
	private void sortAreas( List<AOManifest> aolist )
	{
		Location fix = Locator.getLocation(this, 120000);
		if( fix == null )
		{
			Logr.d( TAG, "No location is available so areas are not sorted" );
			return;
		}
		
		List<SortableManifest> sortedList = new ArrayList<SortableManifest>( aolist.size() );
		for( AOManifest ao : aolist )
		{
			SortableManifest sm = new SortableManifest();
			sm.score = scoreGeoMatch( fix, ao );
			sm.manifest = ao;
			sortedList.add( sm );
		}
		Collections.sort( sortedList, new Comparator<SortableManifest>() {
			@Override
			public int compare( SortableManifest lhs, SortableManifest rhs )
			{
				return lhs.score - rhs.score;
			}
		} );
		Logr.d( TAG, "Sorted areas: " + StringTool.asDelimitedString(sortedList, ',') );
		
		// repopulate aolist in scored order
		aolist.clear();
		for( SortableManifest sm : sortedList )
		{
			aolist.add( sm.manifest );
		}
	}
	
	private int scoreGeoMatch( Location fix, AOManifest ao )
	{
		int y = (int) (fix.getLatitude() * 1e6);
		int x = (int) (fix.getLongitude() * 1e6);
		
		// is fix inside ao?
		int[][] boxes = ao.getBox();
		int match = Box.containsXY(boxes, x, y);
		if( match == -1 ) return Integer.MAX_VALUE;
		
		// how big is this area?
		float size = Box.size( boxes[ match ] );
		
		// scale down
		return (int) Math.sqrt( size );
	}
	
	private Integer getPosition( AOManifest m )
	{
		if( manifest == null ) return null;
		
	    for( int i = 0; i < aoList.size(); i++ )
	    {
	    	if( aoList.get( i ).getId().equals( m.getId() ) ) return i;
	    } 		
	    
	    return null;
	}
	
	private void populateMenu( AOManifest manifest, String path )
	{
		int oldSelection = menuSpinner.getSelectedItemPosition();
		boolean oldEmpty = menuItems.isEmpty();
		menuItems.clear();
		
		boolean selected = false;
		if( manifest != null && manifest.getMenu() != null )
		{
			menuItems.addAll( manifest.getMenu() );
			for( int i = 0; i < menuItems.size(); i++ )
			{
				String url = menuItems.get( i ).getUrl();
				if( url.equals( path ) )
				{
					menuSpinner.setSelection( i );
					selected = true;
					break;
				}
			}
		}
		
		if( !selected && !menuItems.isEmpty() )
		{
			this.path = menuItems.get( 0 ).getUrl();
		}
		
		if( menuItems.isEmpty() || oldEmpty != menuItems.isEmpty() || oldSelection != menuSpinner.getSelectedItemPosition() )
		{
			debouncePath = true;
		}
		
		menuAdapter.notifyDataSetChanged();
	}
	
	private void switchLogo( AOManifest manifest )
	{
		ImageView view = (ImageView) findViewById(R.id.imageView1 );

	    File aodir = Filer.create(this, false, Type.AO, manifest.getId()).getDirectory();
	    File logo = new File( aodir, "start/logo.png" );
	    if( logo.exists() )
	    {
	        view.setImageURI( Uri.fromFile( logo ) );
	    }
	    else
	    {
	    	// use the default view
	    	view.setImageResource( R.drawable.cert320x184 );
	    }
	}
}