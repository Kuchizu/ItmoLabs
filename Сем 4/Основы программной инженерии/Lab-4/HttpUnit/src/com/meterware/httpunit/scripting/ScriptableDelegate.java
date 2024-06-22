package com.meterware.httpunit.scripting;

import com.meterware.httpunit.HTMLElement;

/********************************************************************************************************************
 * $Id: ScriptableDelegate.java,v 1.10 2004/03/09 21:49:18 russgold Exp $
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

/**
 * An interface for objects which will be accessible via scripting.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
abstract public class ScriptableDelegate {

    private ScriptingEngine _scriptEngine;


    private static final ScriptingEngine NULL_SCRIPT_ENGINE = new ScriptingEngine() {
        public boolean supportsScriptLanguage( String language ) { return false; }
        public String executeScript( String language, String script ) { return ""; }
        public boolean performEvent( String eventScript ) { return true; }
        public String evaluateScriptExpression( String urlString ) { return null; }
        public ScriptingEngine newScriptingEngine( ScriptableDelegate child ) { return this; }
        public void clearCaches() {}
    };


    public boolean supportsScript( String language ) {
        return getScriptEngine().supportsScriptLanguage( language );
    }


    /**
     * Executes the specified scripted event.
     **/
    public boolean doEvent( String eventScript ) {
        if (eventScript.length() == 0) return true;
        return getScriptEngine().performEvent( eventScript );
    }


    /**
     * Executes the specified script, returning any intended replacement text.
     * @return the replacement text, which may be empty.
     **/
    public String runScript( String language, String script ) {
        return (script.length() == 0) ? "" : getScriptEngine().executeScript( language, script );
    }


    /**
     * Evaluates the specified javascript expression, returning its value.
     **/
    public String evaluateExpression( String urlString ) {
        if (urlString.length() == 0) return null;
        return getScriptEngine().evaluateScriptExpression( urlString );
    }


    /**
     * Returns the value of the named property. Will return null if the property does not exist.
     **/
    public Object get( String propertyName ) {
        return null;
    }


    /**
     * Returns the value of the index property. Will return null if the property does not exist.
     **/
    public Object get( int index ) {
        return null;
    }


    /**
     * Sets the value of the named property. Will throw a runtime exception if the property does not exist or
     * cannot accept the specified value.
     **/
    public void set( String propertyName, Object value ) {
        throw new RuntimeException( "No such property: " + propertyName );
    }


    /**
     * Specifies the scripting engine to be used.
     */
    public void setScriptEngine( ScriptingEngine scriptEngine ) {
        _scriptEngine = scriptEngine;
    }


    public ScriptingEngine getScriptEngine() {
        return _scriptEngine != null ? _scriptEngine : NULL_SCRIPT_ENGINE;
    }


    public ScriptingEngine getScriptEngine( ScriptableDelegate child ) {
        return getScriptEngine().newScriptingEngine( child );
    }


    protected ScriptableDelegate[] getDelegates( final HTMLElement[] elements ) {
        ScriptableDelegate[] result = new ScriptableDelegate[ elements.length ];
        for (int i = 0; i < elements.length; i++) {
            result[i] = elements[i].getScriptableDelegate();
        }
        return result;
    }

}
