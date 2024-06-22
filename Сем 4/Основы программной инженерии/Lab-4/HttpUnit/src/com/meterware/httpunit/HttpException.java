package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: HttpException.java,v 1.6 2001/11/27 19:51:27 russgold Exp $
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
import java.net.URL;


/**
 * This exception is thrown when an Http error (response code 4xx or 5xx) is detected.
 * @author Seth Ladd
 * @author Russell Gold
 **/
public class HttpException extends RuntimeException {


    protected HttpException( int responseCode ) {
        _responseCode = responseCode;
    }


    protected HttpException( int responseCode, String responseMessage, URL baseURL ) {
        _responseMessage = responseMessage;
        _responseCode = responseCode;
        _url = baseURL;
    }


    public String getMessage() {
        StringBuffer sb = new StringBuffer(HttpUnitUtils.DEFAULT_TEXT_BUFFER_SIZE).append( "Error on HTTP request: " );
        sb.append( _responseCode );
        if (_responseMessage != null) {
            sb.append( " " );
            sb.append( _responseMessage );
            sb.append( "" );
        }
        if (_url != null) {
            sb.append( " [" );
            sb.append( _url.toExternalForm() );
            sb.append( "]" );
        }
        return sb.toString();
    }


    public int getResponseCode() {
        return _responseCode;
    }


    public String getResponseMessage() {
        return _responseMessage;
    }


    private int _responseCode;
    private URL _url;

    private String _responseMessage;

}