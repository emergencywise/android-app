package org.emergencywise.android.app.aocache.model;

import org.emergencywise.android.app.util.StringTool;

import java.io.Serializable;

public class Package
    implements Serializable {

    private static final long serialVersionUID = 4177940226298931842L;
    private int version;
    private String name;
    private String url;
    private Long size;

    public Package() {}

    public boolean isIdentical( Package peer )
    {
        if( version != peer.version ) return false;
        if( !StringTool.compare(name, peer.name) ) return false;
        if( !StringTool.compare( url, peer.url ) ) return false;
        if( size == null && peer.size == null ) return true;
        if( size != null && peer.size == null ) return false;
        if( size == null && peer.size != null ) return false;
        return (long) size == (long) peer.size;
    }

    public int getVersion() {
        return version;
    }
    public void setVersion(int version) {
        this.version = version;
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
    public Long getSize() {
        return size;
    }
    public void setSize(Long size) {
        this.size = size;
    }
}
