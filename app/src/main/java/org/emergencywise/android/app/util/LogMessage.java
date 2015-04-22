package org.emergencywise.android.app.util;

public class LogMessage
{
	public enum Level { INFO, WARNING, ERROR };
	private Level level;
	private String msg;
	private long time;
	
	public LogMessage( Level level, String msg )
	{
		this.level = level;
		this.msg = msg;
		this.time = System.currentTimeMillis();
	}
	
	public Level getLevel()
	{
		return level;
	}
	
	public String getMessage()
	{
		return msg;
	}
	
	public long getTime()
	{
		return time;
	}
}