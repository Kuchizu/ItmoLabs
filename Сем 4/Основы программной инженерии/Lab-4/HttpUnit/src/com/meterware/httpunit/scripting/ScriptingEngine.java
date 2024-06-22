package com.meterware.httpunit.scripting;
/********************************************************************************************************************
 * $Id: ScriptingEngine.java,v 1.7 2004/03/09 21:49:18 russgold Exp $
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
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public interface ScriptingEngine {


    /**
     * Returns true if this engine supports the specified script.
     **/
    public boolean supportsScriptLanguage( String language );


    /**
     * Interprets the specified script, which may include global function definitions.
     */
    public String executeScript( String language, String script );


    /**
     * Interprets the specified script and returns a boolean result.
     */
    public boolean performEvent( String eventScript );


    /**
     * Evaluates the specified string as JavaScript. Will return null if the script has no return value.
     */
    public String evaluateScriptExpression( String urlString );


    /**
     * Returns a new scripting engine for the specified delegate.
     */
    public ScriptingEngine newScriptingEngine( ScriptableDelegate child );


    /**
     * Clears any cached values, permitting them to be recomputed as needed.
     */
    public void clearCaches();

}
