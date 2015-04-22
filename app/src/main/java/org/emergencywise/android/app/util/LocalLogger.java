package org.emergencywise.android.app.util;

import org.emergencywise.android.app.util.LogMessage.Level;
import java.util.ArrayList;
import java.util.List;

public class LocalLogger
{
	private List<LogMessage> messages = new ArrayList<LogMessage>(); 
	
	public LocalLogger() {}
	public void log( Level level, String text )
	{
		messages.add( new LogMessage( level, text ) );
	}
	public void w( String warning )
	{
		log( Level.WARNING, warning );
	}
	
	public void e( String error )
	{
		log( Level.ERROR, error );
	}
	
	public void e( String error, Throwable t )
	{
		log( Level.ERROR, error + ": " + t.getMessage() );
	}
	
	public void i( String info )
	{
		log( Level.INFO, info );
	}
	
	public String toString()
	{
		StringBuilder out = new StringBuilder();
		for( LogMessage m : messages )
		{
			out.append( m.getLevel() )
			   .append( ": " )
			   .append( m.getMessage() )
			   .append( "\r\n" );
		}
		return out.toString();
	}
}