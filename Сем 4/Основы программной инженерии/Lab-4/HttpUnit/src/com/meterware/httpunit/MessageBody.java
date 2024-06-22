package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: MessageBody.java,v 1.4 2003/02/14 19:04:24 russgold Exp $
*
* Copyright (c) 2000-2001, 2003 Russell Gold
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
import java.io.OutputStream;

/**
 * An abstract class representing the body of a web request.
 **/
abstract
class MessageBody {


    MessageBody( MessageBodyWebRequest request ) {
        _request = request;
    }


    /**
     * Returns the content type of this message body. For text messages, this
     * should include the character set.
     **/
    abstract
    String getContentType();


    /**
     * Transmits the body of this request as a sequence of bytes.
     **/
    abstract
    void writeTo( OutputStream outputStream ) throws IOException;


    protected MessageBodyWebRequest getRequest() {
        return _request;
    }


    private MessageBodyWebRequest _request;

}
