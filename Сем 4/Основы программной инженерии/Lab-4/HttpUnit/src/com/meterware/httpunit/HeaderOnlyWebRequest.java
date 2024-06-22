package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: HeaderOnlyWebRequest.java,v 1.4 2004/06/30 23:54:48 russgold Exp $
 *
 * Copyright (c) 2002,2004, Russell Gold
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
import java.io.IOException;


/**
 * A web request which has no information in its message body.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
public abstract class HeaderOnlyWebRequest extends WebRequest {


    /**
     * Returns the query string defined for this request.
     **/
    public String getQueryString() {
        try {
            URLEncodedString encoder = new URLEncodedString();
            getParameterHolder().recordPredefinedParameters( encoder );
            getParameterHolder().recordParameters( encoder );
            return encoder.getString();
        } catch (IOException e) {
            throw new RuntimeException( "Programming error: " + e );   // should never happen
        }
    }


//-------------------------------- protected members ---------------------------


    protected HeaderOnlyWebRequest( URL urlBase, String urlString, FrameSelector frame, String target ) {
        super( urlBase, urlString, frame, target );
    }


    protected HeaderOnlyWebRequest( URL urlBase, String urlString, String target ) {
        super( urlBase, urlString, target );
    }


    protected HeaderOnlyWebRequest( URL urlBase, String urlString ) {
        super( urlBase, urlString );
    }


    protected HeaderOnlyWebRequest( String urlString ) {
        super( urlString );
    }


//------------------------------------ package members --------------------------


    HeaderOnlyWebRequest( WebRequestSource requestSource ) {
        super( requestSource, WebRequest.newParameterHolder( requestSource ) );
    }


    HeaderOnlyWebRequest( WebForm sourceForm, ParameterHolder parameterHolder, SubmitButton button, int x, int y ) {
        super( sourceForm, parameterHolder, button, x, y );
    }


    HeaderOnlyWebRequest( URL urlBase, String urlString, FrameSelector frame ) {
        super( urlBase, urlString, frame, frame.getName() );
    }

}
