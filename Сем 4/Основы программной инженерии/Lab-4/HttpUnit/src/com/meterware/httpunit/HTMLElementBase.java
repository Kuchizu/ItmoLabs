package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: HTMLElementBase.java,v 1.8 2004/10/29 00:41:24 russgold Exp $
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
import org.w3c.dom.Node;
import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.util.List;
import java.util.ArrayList;


/**
 *
 * @since 1.5.2
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
abstract
class HTMLElementBase implements HTMLElement {

    private Node        _node;
    private ScriptableDelegate _scriptable;
    private List _supportedAttributes = new ArrayList();


    public String getID() {
        return getAttribute( "id" );
    }


    public String getClassName() {
        return getAttribute( "class" );
    }


    public String getTitle() {
        return getAttribute( "title" );
    }


    public String getName() {
        return getAttribute( "name" );
    }


    /**
     * Returns a scriptable object which can act as a proxy for this control.
     */
    public ScriptableDelegate getScriptableDelegate() {
        if (_scriptable == null) {
            _scriptable = newScriptable();
            _scriptable.setScriptEngine( getParentDelegate().getScriptEngine( _scriptable ) );
        }
        return _scriptable;
    }


    /**
     * Returns the text value of this block.
     **/
    public String getText() {
        if (_node.getNodeType() == Node.TEXT_NODE) {
            return _node.getNodeValue().trim();
        } else if (_node == null || !_node.hasChildNodes()) {
            return "";
        } else {
            return NodeUtils.asText( _node.getChildNodes() ).trim();
        }
    }


    public String getTagName() {
        return _node.getNodeName();
    }


    protected HTMLElementBase( Node node ) {
        _node = node;
        supportAttribute( "id" );
        supportAttribute( "class" );
        supportAttribute( "title" );
        supportAttribute( "name" );
    }


    public String getAttribute( final String name ) {
        return NodeUtils.getNodeAttribute( getNode(), name );
    }


    public boolean isSupportedAttribute( String name ) {
        return _supportedAttributes.contains( name );
    }


    protected String getAttribute( final String name, String defaultValue ) {
        return NodeUtils.getNodeAttribute( getNode(), name, defaultValue );
    }


    protected Node getNode() {
        return _node;
    }


    protected void supportAttribute( String name ) {
        _supportedAttributes.add( name );
    }


    /**
     * Creates and returns a scriptable object for this control. Subclasses should override this if they use a different
     * implementation of Scriptable.
     */
    protected ScriptableDelegate newScriptable() {
        return new HTMLElementScriptable( this );
    }


    /**
     * Returns the scriptable delegate which can provide the scriptable delegate for this element.
     */
    abstract protected ScriptableDelegate getParentDelegate();


}
