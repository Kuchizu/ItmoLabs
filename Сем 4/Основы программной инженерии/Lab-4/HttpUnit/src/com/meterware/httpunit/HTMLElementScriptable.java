package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: HTMLElementScriptable.java,v 1.3 2004/10/29 00:41:24 russgold Exp $
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
import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.DocumentElement;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class HTMLElementScriptable extends ScriptableDelegate implements DocumentElement {

    private HTMLElement _element;

    public Object get( String propertyName ) {
        if (propertyName.equals( "nodeName" )) {
            return _element.getTagName();
        } else if (propertyName.equals( "tagName" )) {
            return _element.getTagName();
        } else if (propertyName.equalsIgnoreCase( "title" )) {
            return _element.getTitle();
        } else if (_element.isSupportedAttribute( propertyName )) {
            return _element.getAttribute( propertyName );
        } else {
            return super.get( propertyName );
        }
    }


    public HTMLElementScriptable( HTMLElement element ) {
        _element = element;
    }
}
