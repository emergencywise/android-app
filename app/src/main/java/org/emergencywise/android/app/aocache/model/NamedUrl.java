package org.emergencywise.android.app.aocache.model;

import java.io.Serializable;

public class NamedUrl
	implements Serializable
{
	private static final long serialVersionUID = -6267008845396278340L;
	private String name;
	private String url;
	
	public NamedUrl() {}
	public NamedUrl( String name, String url )
	{
		this.name = name;
		this.url = url;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
