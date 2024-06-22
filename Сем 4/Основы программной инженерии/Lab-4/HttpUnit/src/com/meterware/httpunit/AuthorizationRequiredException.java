package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: AuthorizationRequiredException.java,v 1.6 2002/11/08 01:59:37 russgold Exp $
*
* Copyright (c) 2000-2001, Russell Gold
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
import java.util.Properties;
import java.io.*;


/**
 * This exception is thrown when an unauthorized request is made for a page that requires authentication.
 **/
public class AuthorizationRequiredException extends RuntimeException {


    AuthorizationRequiredException( String wwwAuthenticateHeader ) {
        final int index = wwwAuthenticateHeader.indexOf( ' ' );
        if (index < 0) {  // non-conforming header
            _scheme = "Basic";
            _params = wwwAuthenticateHeader;
        } else {
            _scheme = wwwAuthenticateHeader.substring( 0, index );
            _params = wwwAuthenticateHeader.substring( index+1 );
        }
        loadProperties();
    }


    private void loadProperties() {
        try {
            _properties = new Properties();
            _properties.load( new ByteArrayInputStream( _params.getBytes() ) );
        } catch (IOException e) {
        }
    }


    protected AuthorizationRequiredException( String scheme, String params ) {
        _scheme = scheme;
        _params = params;
        loadProperties();
    }


    public String getMessage() {
        return _scheme + " authentication required: " + _params;
    }


    /**
     * Returns the name of the <a href="http://www.freesoft.org/CIE/RFC/Orig/rfc2617.txt">authentication scheme</a>.
     **/
    public String getAuthenticationScheme() {
        return _scheme;
    }


    /**
     * Returns the named authentication parameter. For Basic authentication, the only parameter is "realm".
     **/
    public String getAuthenticationParameter( String parameterName ) {
        return _properties.getProperty( parameterName );
    }


//------------------------------------- private members ------------------------------------------


    private String     _params;
    private String     _scheme;
    private Properties _properties;
}
