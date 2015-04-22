package org.emergencywise.android.app.aocache.model;

import org.emergencywise.android.app.util.Box;
import org.emergencywise.android.app.util.StringTool;

import java.io.Serializable;
import java.util.List;

public class AOManifest
	implements Serializable
{
	private static final long serialVersionUID = -1328481775729609929L;
	private String id;
	private String title;
	
	// list of lists of lng,lat... boundary paths
	// matches multipolygon to allow non-contiguous areas and donuts
	private int[][][] poly;
	
	// lists of boxes as n,e,s,w; outer list is to support non-contiguous areas
	private int[][] box;
	
	private List<NamedUrl> menu;
	private List<Package> packages;
	
	public AOManifest() {}
	
	// TODO as equals()? equivalent()?
	public boolean isIdentical( AOManifest peer )
	{
		if( peer == null ) return false;
		
		if( !StringTool.compare(id, peer.id) ) return false;
		if( !StringTool.compare( title, peer.title ) ) return false;
		
		if( packages == null || peer.packages == null ) return false;
		if( packages.size() != peer.packages.size() ) return false;
		for( int i = 0; i < packages.size(); i++ )
		{
			if( !packages.get( i ).isIdentical( peer.packages.get( i ) ) ) return false;
		}
		
		// ignore menu and geocircle for now
		return true;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/*
	public List<GeoCircle> getArea() {
		return area;
	}

	public void setArea(List<GeoCircle> area) {
		this.area = area;
	}
	*/

	public List<NamedUrl> getMenu() {
		return menu;
	}

	public void setMenu(List<NamedUrl> menu) {
		this.menu = menu;
	}

	public List<Package> getPackages() {
		return packages;
	}

	public void setPackages(List<Package> packages) {
		this.packages = packages;
	}

	public int[][][] getPoly()
	{
		return poly;
	}

	public void setPoly( int[][][] poly )
	{
		this.poly = poly;
	}

	public int[][] getBox()
	{
		if( box == null && poly != null )
		{
			box = Box.fromPoly(poly);
		}
		
		return box;
	}

	public void setBox( int[][] box )
	{
		this.box = box;
	}
}