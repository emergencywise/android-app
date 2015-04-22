package org.emergencywise.android.app;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.citizen911.android.R;
import com.citizen911.common.model.AOManifest;

public class SelectAOIntroductionActivity
	extends Activity
{
	@Override
	public void onCreate( Bundle savedInstanceState )
	{
	    super.onCreate( savedInstanceState );
	    setContentView( R.layout.select_ao_intro );
	    
		Button button = (Button) findViewById( R.id.button1 );
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult( new Intent( getBaseContext(), SelectAOActivity.class ), SELECT_AO_REQUEST_CODE );
			}
		});
	}
	
	private static int SELECT_AO_REQUEST_CODE = 1;
	
	@Override
	public void onActivityResult( int requestCode, int resultCode, Intent result )
	{
		// any areas now?
	    List<AOManifest> list = AOCacheManager.getManifestList( this );
	    if( list == null || list.isEmpty() )
	    {
	    	// make the wording stronger
	    	TextView tv = (TextView) findViewById( R.id.textView1 );
	    	tv.setText( R.string.select_ao_intro_strong );
	    }
	    else
	    {
	    	this.setResult( RESULT_OK );
	    	finish();
	    }
	}
	
	@Override
	public void onBackPressed()
	{
		// same as selecting "cancel" button
    	setResult( RESULT_CANCELED );
    	finish();
	}
}