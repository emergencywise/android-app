package org.emergencywise.android.app;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.citizen911.android.MyPreferences;
import com.citizen911.android.aocache.service.AOCacheUpdateWorker;
import com.citizen911.android.json.JsonClient;
import com.citizen911.android.log.LocalLogger;
import com.citizen911.android.log.Logr;
import com.citizen911.android.util.UrlTool;
import com.citizen911.android.web.API;
import com.citizen911.android.R;
import com.citizen911.common.model.AOManifest;

public class ListNewNearbyAOActivity
	extends ListActivity
{
	private static final String TAG = ListNewNearbyAOActivity.class.getName();
	private ProgressDialog progressBar;
	private List<JSONObject> manifestList;
	private AOManifestAdapter adapter;
	private FetchNearbyAOTask fetchTask;
	private RefreshAOTask refreshTask;
	private String nearbyUrl;
	private String manifestUrl;
	private boolean scanned = false;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.ao_scan );
	    
	    manifestList = new ArrayList<JSONObject>();
	    adapter = new AOManifestAdapter( this, R.layout.ao_row, manifestList );
	    setListAdapter( adapter );
	    
		Button button = (Button) findViewById( R.id.button_scan );
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// request nearby list from server
				showDialog( PROGRESS_DIALOG );
			}
		});
		
		button = (Button) findViewById( R.id.button_wifi );
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
			}
		});
		
		getListView().setOnItemClickListener(new OnItemClickListener()
		{
		    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
		    {
		    	JSONObject manifest = manifestList.get( position );
		    	final int index = position;
		    	final String ao = manifest.optString( "id" );
		    	new AlertDialog.Builder( ListNewNearbyAOActivity.this )
                			   //.setTitle( R.string.select_dialog )
                			   .setItems( R.array.new_ao_options, new DialogInterface.OnClickListener() {
                				   	public void onClick(DialogInterface dialog, int which)
                				   	{
                				   		if( which == 0 )
                				   		{
                				   			AOCacheManager.addAO( ListNewNearbyAOActivity.this, ao );
                				   			manifestList.remove( index );
                							adapter.notifyDataSetChanged();
                							
                				   			showDialog( REFRESHING_AO_DIALOG );
                				   			refreshTask = new RefreshAOTask();
                				   			refreshTask.execute( ao );
                				   			//Toast.makeText( ListNewNearbyAOActivity.this, R.string.ao_added, Toast.LENGTH_LONG ).show();
                				   		}
                				   		else if( which == 1 )
                				   		{
                				   			// TODO add to ignore list		
                				   		}
                				   	}
                			   }).show();
		    }
		});
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		
	    // use the syncUrl as our base, or override
		nearbyUrl = getIntent().getDataString();
	    if( nearbyUrl == null )
	    {
			MyPreferences preferences = new MyPreferences( this );
			nearbyUrl = preferences.getAoNearbyUrl();	    	
	    }
	    
	    TextView serverName = (TextView) findViewById( R.id.server_name );
	    serverName.setText( nearbyUrl );
	    
	    // also make sure to create manifest url for downloads
	    try
	    {
	    	URL url = new URL( nearbyUrl );

	    	String base = url.getProtocol() + "://" + url.getHost();
	    	int port = url.getPort();
	    	if( port != -1 && port != 80 )
	    	{
	    		base = base + ":" + Integer.toString( port );
	    	}
	    	manifestUrl = UrlTool.fixupServerUrl( base, API.AO_MANIFEST_PATH );
	    }
	    catch( Exception ex )
	    {
	    	Logr.e( TAG, "Failed to create manifest URL", ex );
	    }
	    
	    if( !scanned )
	    {
	    	scanned = true;
			showDialog( PROGRESS_DIALOG );	
	    }
	}
	    
	private class AOManifestAdapter
		extends ArrayAdapter<JSONObject>
	{
	    private List<JSONObject> items;
	
	    public AOManifestAdapter(Context context, int textViewResourceId, List<JSONObject> items)
	    {
            super(context, textViewResourceId, items);
            this.items = items;
	    }
	
	    @Override
	    public View getView( int position, View convertView, ViewGroup parent ) 
	    {
	        View v = convertView;
	        if (v == null)
	        {
	            LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.ao_row, null);
	        }
	        JSONObject manifest = items.get(position);
	        if( manifest != null ) 
	        {
	            TextView tv = (TextView) v.findViewById(R.id.ao_id );
	            tv.setText( manifest.optString( "id" ) );
	            tv = (TextView) v.findViewById(R.id.ao_title);
	            tv.setText( manifest.optString( "title" )  );
	        }
	        return v;
	    }
	}
	
	private static final int PROGRESS_DIALOG = 0;
	private static final int REFRESHING_AO_DIALOG = 1;
	@Override
	protected Dialog onCreateDialog( int id )
	{
	    switch (id)
	    {
	        case PROGRESS_DIALOG:
	        {
	        	progressBar = new ProgressDialog(this);
	        	progressBar.setProgressStyle( ProgressDialog.STYLE_SPINNER );
	        	progressBar.setMessage( this.getString( R.string.scanning_for_nearby_aos ) );
	        	progressBar.setCancelable( true );
	        	progressBar.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						if( fetchTask != null )
						{
							fetchTask.cancel( false );
						}
					}} );
	            return progressBar;
	        }
	        
	        case REFRESHING_AO_DIALOG:
	        	progressBar = new ProgressDialog(this);
	        	progressBar.setProgressStyle( ProgressDialog.STYLE_SPINNER );
	        	progressBar.setMessage( this.getString( R.string.refreshing_ao ) );
	        	progressBar.setCancelable( true );
	        	progressBar.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog)
					{
						if( refreshTask != null )
						{
							refreshTask.cancel( false );
						}
					}} );
	            return progressBar;
	    }
	    
	    return null;
	}
	
	@Override
	protected void onPrepareDialog( int id, Dialog dialog )
	{
		if( id == PROGRESS_DIALOG )
		{
			// clear existing list
			manifestList.clear();
			adapter.notifyDataSetChanged();
			
			fetchTask = new FetchNearbyAOTask();
			fetchTask.execute( nearbyUrl );
		}
	}
	
	private class FetchNearbyAOTask
		extends AsyncTask<String, Void, JSONArray>
	{
		List<AOManifest> current;
		
	    protected JSONArray doInBackground( String... urls )
	    {
	         current = AOCacheManager.getManifestList( ListNewNearbyAOActivity.this );
	         return getNearbyAOs( urls[0] );
	    }
	    protected void onPostExecute( JSONArray result )
	    {
			manifestList.clear();
			if (result == null)
			{
				Toast.makeText(ListNewNearbyAOActivity.this, R.string.failed_to_fetch_nearby_ao, Toast.LENGTH_LONG ).show();
			}
			else 
			{
				// only add aos that aren't currently installed
				for (int i = 0; i < result.length(); i++)
				{
					JSONObject ao = result.optJSONObject(i);
					if (ao != null)
					{
						String aoid = ao.optString("id");
						if( !exists( current, aoid ) )
						{
							manifestList.add(ao);
						}
					}
				}
			}

			adapter.notifyDataSetChanged();
			dismissDialog(PROGRESS_DIALOG);
		}

		protected void onCancelled()
		{
			dismissDialog(PROGRESS_DIALOG);
		}
	}
	
	private boolean exists( List<AOManifest> list, String aoid )
	{
		if( list == null || aoid == null ) return false;
		for( AOManifest ao : list )
		{
			if( aoid.equals( ao.getId() ) )
			{
				return true;
			}
		}
		
		return false;
	}
	
	// run in thread or asynctask
	private JSONArray getNearbyAOs( String nearbyUrl )
	{
		// sync with server to see what needs to be uploaded
		JSONObject json = JsonClient.getInstance().get( nearbyUrl, null, null );
		JSONObject failure = json.optJSONObject( "failure" );
		if( failure != null ) 
		{
			Logr.e( TAG, "Failed to find nearby aos with " + nearbyUrl + ": " + failure );
			return null;
		}
		JSONArray result = json.optJSONArray( "result" );
		if( result == null || result.length() == 0 )
		{
			Logr.e( TAG, "Nearby ao list had empty result from " + nearbyUrl );
			return null;			
		}
		
		return json.optJSONArray( "result" );
	}
	
	private class RefreshAOTask
		extends AsyncTask<String, Void, Boolean>
	{
		private String ao;
		private LocalLogger logr = new LocalLogger();
		
		protected Boolean doInBackground( String... ao )
		{
			this.ao = ao[0];
			return new AOCacheUpdateWorker( ListNewNearbyAOActivity.this ).syncAO( this.ao, manifestUrl, false, logr );
		}
		
		protected void onPostExecute(Boolean success)
		{
			if( success )
			{
				Toast.makeText( ListNewNearbyAOActivity.this, R.string.ao_refreshed, Toast.LENGTH_LONG ).show();
			}
			else
			{
				ProblemViewer.alertToProblems( ListNewNearbyAOActivity.this, R.string.failed_to_refresh_ao, logr );
				//Toast.makeText( ListNewNearbyAOActivity.this, R.string.failed_to_refresh_ao, Toast.LENGTH_LONG ).show();
			}
		
			dismissDialog( REFRESHING_AO_DIALOG );
		}
		
		protected void onCancelled()
		{
			dismissDialog( REFRESHING_AO_DIALOG );
		}
	}
}