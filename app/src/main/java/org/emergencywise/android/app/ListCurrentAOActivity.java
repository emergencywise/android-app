package org.emergencywise.android.app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.emergencywise.android.app.aocache.AOCacheManager;
import org.emergencywise.android.app.aocache.AOCacheUpdateWorker;
import org.emergencywise.android.app.aocache.model.AOManifest;
import org.emergencywise.android.app.util.LocalLogger;
import org.emergencywise.android.app.util.UrlTool;

import java.util.ArrayList;
import java.util.List;

public class ListCurrentAOActivity
	extends ListActivity
{
	private List<AOManifest> manifestList;
	private AOManifestAdapter adapter;
	private FetchCurrentAOTask fetchTask;
	private DeleteAOTask deleteTask;
	private RefreshAOTask refreshTask;
	private VerifyAOTask verifyTask;
	private String manifestUrl;
	
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.current_ao );
	    
	    manifestList = new ArrayList<AOManifest>();
	    adapter = new AOManifestAdapter( this, R.layout.ao_row, manifestList );
	    setListAdapter( adapter );
	    
		getListView().setOnItemClickListener(new OnItemClickListener()
		{
		    public void onItemClick( AdapterView<?> parent, View view, int position, long id )
		    {
		    	final AOManifest manifest = manifestList.get( position );
		    	new AlertDialog.Builder( ListCurrentAOActivity.this )
                			   //.setTitle( R.string.select_dialog )
                			   .setItems( R.array.current_ao_options, new DialogInterface.OnClickListener()
                			   {
                				   	public void onClick(DialogInterface dialog, int which)
                				   	{
                				   		if( which == 0 )
                				   		{
                				   			showDialog( REFRESHING_AO_DIALOG );
                				   			refreshTask = new RefreshAOTask();
                				   			refreshTask.execute( manifest );	              				   			
                				   		}
                				   		else if( which == 1 || which == 2 )
                				   		{
                				   			showDialog( VERIFY_AO_DIALOG );
                				   			verifyTask = new VerifyAOTask();
                				   			if( which == 2 ) verifyTask.local = true;
                				   			verifyTask.execute( manifest );		
                				   		}
                				   		else if( which == 3 )
                				   		{
                				   			showDialog( DELETING_AO_DIALOG );
                				   			deleteTask = new DeleteAOTask();
                				   			deleteTask.execute( manifest );		
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
		
	    // use the provided syncUrl, or the default if not provided
		manifestUrl = getIntent().getDataString();
	    if( manifestUrl == null )
	    {
			MyPreferences preferences = new MyPreferences( this );
			manifestUrl = preferences.getAoManifestUrl();	    	
	    }
	    else
	    {
	    	manifestUrl = UrlTool.fixupServerUrl(manifestUrl, API.AO_MANIFEST_PATH);
	    }
	    
	    TextView serverName = (TextView) findViewById( R.id.server_name );
	    serverName.setText( manifestUrl );
	    
		showDialog( SCANNING_DIALOG );
	}
	
	private class AOManifestAdapter
		extends ArrayAdapter<AOManifest>
	{
	    private List<AOManifest> items;
	
	    public AOManifestAdapter(Context context, int textViewResourceId, List<AOManifest> items)
	    {
	        super( context, textViewResourceId, items );
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
	        AOManifest manifest = items.get(position);
	        if( manifest != null ) 
	        {
	            TextView tv = (TextView) v.findViewById(R.id.ao_id );
	            tv.setText( manifest.getId() );
	            tv = (TextView) v.findViewById(R.id.ao_title);
	            tv.setText( manifest.getTitle()  );
	        }
	        return v;
	    }
	}
	
	private static final int SCANNING_DIALOG = 0;
	private static final int DELETING_AO_DIALOG = 1;
	private static final int VERIFY_AO_DIALOG = 2;
	private static final int REFRESHING_AO_DIALOG = 3;

	@Override
	protected Dialog onCreateDialog( int id )
	{
	    switch (id)
	    {
	        case SCANNING_DIALOG:
	        	ProgressDialog progressBar = new ProgressDialog(this);
	        	progressBar.setProgressStyle( ProgressDialog.STYLE_SPINNER );
	        	progressBar.setMessage( this.getString( R.string.scanning_current_aos ) );
	        	progressBar.setCancelable( true );
	        	progressBar.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog)
					{
						if( fetchTask != null )
						{
							fetchTask.cancel( false );
						}
					}} );
	            return progressBar;

	        case VERIFY_AO_DIALOG:
	        	progressBar = new ProgressDialog(this);
	        	progressBar.setProgressStyle( ProgressDialog.STYLE_SPINNER );
	        	progressBar.setMessage( this.getString( R.string.verifying_ao ) );
	        	progressBar.setCancelable( true );
	        	progressBar.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog)
					{
						if( verifyTask != null )
						{
							verifyTask.cancel( false );
						}
					}} );
	            return progressBar;
	            
	        case DELETING_AO_DIALOG:
	        	progressBar = new ProgressDialog(this);
	        	progressBar.setProgressStyle( ProgressDialog.STYLE_SPINNER );
	        	progressBar.setMessage( this.getString( R.string.deleting_ao ) );
	        	progressBar.setCancelable( true );
	        	progressBar.setOnCancelListener( new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog)
					{
						if( deleteTask != null )
						{
							deleteTask.cancel( false );
						}
					}} );
	            return progressBar;
	            
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
	    switch( id )
	    {
	        case SCANNING_DIALOG:
				// clear existing list
				manifestList.clear();
				adapter.notifyDataSetChanged();
				
				// scan directory
				fetchTask = new FetchCurrentAOTask();
				fetchTask.execute();
				break;
	    }
	}
	
	private class FetchCurrentAOTask
		extends AsyncTask<Void, Void, List<AOManifest>>
	{
	     protected List<AOManifest> doInBackground( Void... nada )
	     {
	         return AOCacheManager.getManifestList(ListCurrentAOActivity.this);
	     }
	
	     protected void onPostExecute( List<AOManifest> result )
	     {
	    	 manifestList.clear();
	    	 if( result == null )
	    	 {
	    		Toast.makeText( ListCurrentAOActivity.this, R.string.failed_to_fetch_nearby_ao, Toast.LENGTH_LONG ).show(); 
	    	 }
	    	 else
	    	 {
	    		 for( AOManifest ao : result )
	    		 {
	    			 manifestList.add( ao );
	    		 }
	    	 }
	    	 
	    	 adapter.notifyDataSetChanged();
	         dismissDialog( SCANNING_DIALOG );
	     }
	     
	     protected void onCancelled()
	     {
	         dismissDialog( SCANNING_DIALOG ); 
	     }
	 }
	
	private class DeleteAOTask
		extends AsyncTask<AOManifest, Void, Boolean>
	{
		private AOManifest manifest;

		protected Boolean doInBackground( AOManifest... manifest )
		{
			this.manifest = manifest[0];
			return AOCacheManager.deleteAO(ListCurrentAOActivity.this, this.manifest.getId() );
		}

		protected void onPostExecute(Boolean success)
		{
			if (success)
			{
				// remove ao from list
				manifestList.remove( manifest );
				adapter.notifyDataSetChanged();
				Toast.makeText( ListCurrentAOActivity.this, R.string.ao_deleted, Toast.LENGTH_LONG ).show();
			}
			else
			{
				Toast.makeText( ListCurrentAOActivity.this, R.string.failed_to_delete_ao, Toast.LENGTH_LONG ).show();
			}

			dismissDialog( DELETING_AO_DIALOG );
		}

		protected void onCancelled()
		{
			dismissDialog( DELETING_AO_DIALOG );
		}
	}
	
	private class RefreshAOTask
		extends AsyncTask<AOManifest, Void, Boolean>
	{
		private AOManifest manifest;
		private LocalLogger logr = new LocalLogger();

		protected Boolean doInBackground( AOManifest... manifest )
		{
			this.manifest = manifest[0];
			return new AOCacheUpdateWorker( ListCurrentAOActivity.this ).syncAO( this.manifest.getId(), manifestUrl, false, logr );
		}

		protected void onPostExecute(Boolean success)
		{
			dismissDialog( REFRESHING_AO_DIALOG );
			
			if( success )
			{
				// reload manifest
				int p = manifestList.indexOf( manifest );
				if( p > -1 )
				{
					manifest = AOCacheManager.getManifest( ListCurrentAOActivity.this, manifest.getId() );
					if( manifest != null )
					{
						manifestList.remove( p );
						manifestList.add( p, manifest );
						adapter.notifyDataSetChanged();
					}
				}
				
				ProblemViewer.noProblem(ListCurrentAOActivity.this, R.string.ao_refreshed);
			}
			else
			{
				ProblemViewer.alertToProblems(ListCurrentAOActivity.this, R.string.failed_to_refresh_ao, logr);
			}
		}

		protected void onCancelled()
		{
			dismissDialog( REFRESHING_AO_DIALOG );
		}
	}
	
	private class VerifyAOTask
		extends AsyncTask<AOManifest, Void, Boolean>
	{
		private AOManifest manifest;
		private boolean local;
		private LocalLogger logr = new LocalLogger();
	
		protected Boolean doInBackground( AOManifest... manifest )
		{
			this.manifest = manifest[0];
			if( local )
			{
				return new AOCacheUpdateWorker( ListCurrentAOActivity.this ).verify( this.manifest, logr );
			}
			else
			{
				return new AOCacheUpdateWorker( ListCurrentAOActivity.this ).syncAO( this.manifest.getId(), manifestUrl, true, logr );
			}
		}
	
		protected void onPostExecute(Boolean success)
		{
			dismissDialog( VERIFY_AO_DIALOG );
			
			if( success )
			{
				ProblemViewer.noProblem(ListCurrentAOActivity.this, R.string.ao_verified);
			}
			else
			{
				ProblemViewer.alertToProblems(ListCurrentAOActivity.this, R.string.failed_to_verify_ao, logr);
			}
		}
	
		protected void onCancelled()
		{
			dismissDialog( VERIFY_AO_DIALOG );
		}
	}
}