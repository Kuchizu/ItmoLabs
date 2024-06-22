package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: HttpsProtocolSupport.java,v 1.1 2003/02/04 19:17:26 russgold Exp $
*
* Copyright (c) 2003, Russell Gold
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
import java.security.Provider;
import java.security.Security;


/**
 * Encapsulates support for the HTTPS protocol.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
abstract public class HttpsProtocolSupport {

    /** The name of the system parameter used by java.net to locate protocol handlers. **/
    private final static String PROTOCOL_HANDLER_PKGS  = "java.protocol.handler.pkgs";

    /** The name of the JSSE class which provides support for SSL. **/
    private final static String SunJSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";

    /** The name of the JSSE class which supports the https protocol. **/
    private final static String SSL_PROTOCOL_HANDLER   = "com.sun.net.ssl.internal.www.protocol";

    private static Class _httpsProviderClass;

    private static boolean _httpsSupportVerified;

    private static boolean _httpsProtocolSupportEnabled;


    /**
     * Returns true if the JSSE extension is installed.
     */
    static boolean hasHttpsSupport() {
        if (!_httpsSupportVerified) {
            try {
                getHttpsProviderClass();
            } catch (ClassNotFoundException e) {
            }
            _httpsSupportVerified = true;
        }
        return _httpsProviderClass != null;
    }


    /**
     * Attempts to register the JSSE extension if it is not already registered. Will throw an exception if unable to
     * register the extension.
     */
    static void verifyProtocolSupport( String protocol ) {
        if (protocol.equalsIgnoreCase( "http" )) {
            return;
        } else if (protocol.equalsIgnoreCase( "https" )) {
            validateHttpsProtocolSupport();
        }
    }


    private static void validateHttpsProtocolSupport() {
        if (!_httpsProtocolSupportEnabled) {
            verifyHttpsSupport();
            _httpsProtocolSupportEnabled = true;
        }
    }

    private static void verifyHttpsSupport() {
        try {
            Class providerClass = getHttpsProviderClass();
            if (!hasProvider( providerClass )) Security.addProvider( (Provider) providerClass.newInstance() );
            registerSSLProtocolHandler();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException( "https support requires the Java Secure Sockets Extension. See http://java.sun.com/products/jsse" );
        } catch (Throwable e) {
            throw new RuntimeException( "Unable to enable https support. Make sure that you have installed JSSE " +
                                        "as described in http://java.sun.com/products/jsse/install.html: " + e );
        }
    }


    private static Class getHttpsProviderClass() throws ClassNotFoundException {
        if (_httpsProviderClass == null) {
            _httpsProviderClass = Class.forName( SunJSSE_PROVIDER_CLASS );
        }
        return _httpsProviderClass;
    }


    private static boolean hasProvider( Class providerClass ) {
        Provider[] list = Security.getProviders();
        for (int i = 0; i < list.length; i++) {
            if (list[i].getClass().equals( providerClass )) return true;
        }
        return false;
    }


    private static void registerSSLProtocolHandler() {
        String list = System.getProperty( PROTOCOL_HANDLER_PKGS );
        if (list == null || list.length() == 0) {
            System.setProperty( PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER );
        } else if (list.indexOf( SSL_PROTOCOL_HANDLER ) < 0) {
            System.setProperty( PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER + " | " + list );
        }
    }
}
