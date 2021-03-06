package org.emergencywise.android.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.emergencywise.android.app.R;
import org.emergencywise.android.app.util.LocalLogger;

public class ProblemViewer
{
	public static void noProblem( final Activity activity, int title )
	{
		new AlertDialog.Builder( activity )
		   .setTitle( title )
		   //.setMessage( resId )
		   .setCancelable( true )
		   .setPositiveButton( R.string.ok_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which )
				{
					dialog.dismiss();
				}		   
		   })
		   .show();
	}
	
	public static void alertToProblems( final Activity activity, int resId, final LocalLogger logr )
	{
		new AlertDialog.Builder( activity )
		   .setTitle( R.string.problem_with_area )
		   .setMessage( resId )
		   .setPositiveButton( R.string.ok_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which )
				{
					dialog.dismiss();
				}		   
		   })
		   //.setCancelable( true )
		   .setNeutralButton( R.string.detail_button, new DialogInterface.OnClickListener() {
				@Override
				public void onClick( DialogInterface dialog, int which )
				{
					dialog.dismiss();
					showProblems( activity, logr.toString() );
				}		   
		   })
		   .show();
	}
	
	private static void showProblems( final Activity activity, final String problems )
	{
		new AlertDialog.Builder( activity )
		   .setTitle( R.string.problem_with_area )
		   .setMessage( problems )
		   .show();
	}
}