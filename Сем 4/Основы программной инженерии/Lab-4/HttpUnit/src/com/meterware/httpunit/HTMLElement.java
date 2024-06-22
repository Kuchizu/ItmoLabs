package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: HTMLElement.java,v 1.7 2004/10/29 00:41:24 russgold Exp $
 *
 * Copyright (c) 2002-2004, Russell Gold
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


/**
 * An interface which defines the common properties for an HTML element, which can correspond to any HTML tag.
 *
 * @since 1.5.2
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public interface HTMLElement {

    /**
     * Returns the ID associated with this element. IDs are unique throughout the HTML document.
     **/
     String getID();


    /**
     * Returns the class associated with this element.
     **/
    String getClassName();


    /**
     * Returns the name associated with this element.
     **/
    String getName();


    /**
     * Returns the title associated with this element.
     **/
    String getTitle();


    /**
     * Returns the value of the attribute of this element with the specified name.
     * Returns the empty string if no such attribute exists.
     *
     * @since 1.6
     */
    String getAttribute( String name );


    /**
     * Returns true if this element may have an attribute with the specified name.
     *
     * @since 1.6
     */
    boolean isSupportedAttribute( String name );


    /**
     * Returns the delegate which supports scripting this element.
     */
    ScriptableDelegate getScriptableDelegate();


    /**
     * Returns the contents of this element, converted to a string.
     *
     * @since 1.6
     */
    String getText();


    /**
     * Returns the tag name of this node.
     *
     * @since 1.6.1
     */
    String getTagName();
}
