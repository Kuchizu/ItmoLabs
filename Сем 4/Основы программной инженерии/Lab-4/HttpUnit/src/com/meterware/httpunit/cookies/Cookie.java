package com.meterware.httpunit.cookies;
/********************************************************************************************************************
 * $Id: Cookie.java,v 1.6 2004/09/24 20:13:16 russgold Exp $
 *
 * Copyright (c) 2002, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import java.util.Map;
import java.util.Iterator;
import java.net.URL;


/**
 * An HTTP client-side cookie.
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class Cookie {

    private String _name;

    private String _value;

    private String _path;

    private String _domain;

    private long _expiredTime;


    /**
     * Constructs a cookie w/o any domain or path restrictions.
     */
    Cookie( String name, String value ) {
        _name = name;
        _value = value;
    }


    Cookie( String name, String value, Map attributes ) {
        this( name, value );
        for (Iterator iterator = attributes.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            String attributeValue = (String) attributes.get( key );
            if (key.equalsIgnoreCase( "path" )) {
                _path = attributeValue;
            } else if (key.equalsIgnoreCase( "domain" )) {
                _domain = attributeValue;
            } else if (key.equalsIgnoreCase( "max-age" )) {
                _expiredTime = System.currentTimeMillis() + getAgeInMsec( attributeValue );
            }
        }
    }


    private int getAgeInMsec( String maxAgeValue ) {
        try {
            return 1000 * Integer.parseInt( maxAgeValue );
        } catch (NumberFormatException e) {
            return 0;
        }
    }


    /**
     * Returns the name of this cookie.
     */
    public String getName() {
        return _name;
    }


    /**
     * Returns the value associated with this cookie.
     */
    public String getValue() {
        return _value;
    }


    /**
     * Returns the path to which this cookie is restricted.
     */
    public String getPath() {
        return _path;
    }


    /**
     * Returns the domain to which this cookie may be sent.
     */
    public String getDomain() {
        return _domain;
    }


    void setPath( String path ) {
        _path = path;
    }


    void setDomain( String domain ) {
        _domain = domain;
    }


    public int hashCode() {
        int hashCode = _name.hashCode();
        if (_domain != null) hashCode ^= _domain.hashCode();
        if (_path != null) hashCode ^= _path.hashCode();
        return hashCode;
    }


    public boolean equals( Object obj ) {
        return obj.getClass() == getClass() && equals( (Cookie) obj );
    }


    private boolean equals( Cookie other ) {
        return _name.equalsIgnoreCase( other._name ) &&
                equalProperties( getDomain(), other.getDomain() ) &&
                equalProperties( getPath(), other.getPath() );
    }


    private boolean equalProperties( String first, String second ) {
        return first == second || (first != null && first.equals( second ));
    }


    boolean mayBeSentTo( URL url ) {
        if (getDomain() == null) return true;
        if (_expiredTime != 0 && _expiredTime <= System.currentTimeMillis()) return false;

        return acceptHost( getDomain(), url.getHost() ) && acceptPath( getPath(), url.getPath() );
    }


    private boolean acceptPath( String pathPattern, String hostPath ) {
        return !CookieProperties.isPathMatchingStrict() || hostPath.startsWith( pathPattern );
    }


    private static boolean acceptHost( String hostPattern, String hostName ) {
        return hostPattern.equalsIgnoreCase( hostName ) ||
               (hostPattern.startsWith( "." ) && hostName.endsWith( hostPattern ));
    }
}
