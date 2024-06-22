package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: WebConversation.java,v 1.38 2004/09/22 02:02:09 russgold Exp $
*
* Copyright (c) 2000-2004, Russell Gold
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
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Properties;


/**
 * The context for a series of HTTP requests. This class manages cookies used to maintain
 * session context, computes relative URLs, and generally emulates the browser behavior
 * needed to build an automated test of a web site.
 *
 * @author Russell Gold
 **/
public class WebConversation extends WebClient {

    private String _proxyHost;
    private int _proxyPort;


    /**
     * Creates a new web conversation.
     **/
    public WebConversation() {
    }


//---------------------------------- protected members --------------------------------


    /**
     * Creates a web response object which represents the response to the specified web request.
     **/
    protected WebResponse newResponse( WebRequest request, FrameSelector targetFrame ) throws MalformedURLException, IOException {
        Properties savedProperties = (Properties) System.getProperties().clone();
        try {
            if (_proxyHost != null) {
                System.setProperty( "proxyHost", _proxyHost );
                System.setProperty( "proxyPort", Integer.toString( _proxyPort ) );
            }
            URLConnection connection = openConnection( getRequestURL( request ) );
            if (HttpUnitOptions.isLoggingHttpHeaders()) {
                String urlString = request.getURLString();
                System.out.println( "\nConnecting to " + request.getURL().getHost() );
                System.out.println( "Sending:: " + request.getMethod() + " " + urlString );
            }
            sendHeaders( connection, getHeaderFields( request.getURL() ) );
            sendHeaders( connection, request.getHeaderDictionary() );
            request.completeRequest( connection );
            return new HttpWebResponse( this, targetFrame, request, connection, getExceptionsThrownOnErrorStatus() );
        } finally {
            System.setProperties( savedProperties );
        }
    }


    public void clearProxyServer() {
        _proxyHost = null;
    }


    public void setProxyServer( String proxyHost, int proxyPort ) {
        _proxyHost = proxyHost;
        _proxyPort = proxyPort;
    }


    private URL getRequestURL( WebRequest request ) throws MalformedURLException {
        DNSListener dnsListener = getClientProperties().getDnsListener();
        if (dnsListener == null) return request.getURL();

        String hostName = request.getURL().getHost();
        String portPortion = request.getURL().getPort() == -1 ? "" : (":" + request.getURL().getPort());
        setHeaderField( "Host", hostName + portPortion );
        String actualHost = dnsListener.getIpAddress( hostName );
        if (HttpUnitOptions.isLoggingHttpHeaders()) System.out.println( "Rerouting request to :: " + actualHost );
        return new URL( request.getURL().getProtocol(), actualHost, request.getURL().getPort(), request.getURL().getFile() );
    }


//---------------------------------- private members --------------------------------


    private URLConnection openConnection( URL url ) throws MalformedURLException, IOException {
        URLConnection connection = url.openConnection();
        if (connection instanceof HttpURLConnection) ((HttpURLConnection) connection).setInstanceFollowRedirects( false );
        connection.setUseCaches( false );
        return connection;
    }


    private void sendHeaders( URLConnection connection, Dictionary headers ) {
        for (Enumeration e = headers.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            connection.setRequestProperty( key, (String) headers.get( key ) );
            if (HttpUnitOptions.isLoggingHttpHeaders()) {
                if (key.equalsIgnoreCase( "authorization" ) || key.equalsIgnoreCase( "proxy-authorization") ) {
                    System.out.println( "Sending:: " + key + ": " + headers.get( key ) );
                } else {
                    System.out.println( "Sending:: " + key + ": " + connection.getRequestProperty( key ) );
                }
            }
        }
    }
}
