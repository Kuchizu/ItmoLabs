package com.meterware.httpunit.parsing;
/********************************************************************************************************************
 * $Id: HTMLParserFactory.java,v 1.3 2002/12/26 04:59:35 russgold Exp $
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
import java.util.Vector;


/**
 * Factory for creating HTML parsers. Parser customization properties can be specified but do not necessarily work
 * for every parser type.
 *
 * @since 1.5.2
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bw@xmlizer.biz">Bernhard Wagner</a>
 **/
abstract public class HTMLParserFactory {

    private static Vector     _listeners = new Vector();
    private static HTMLParser _jtidyParser;
    private static HTMLParser _nekoParser;

    private static HTMLParser _htmlParser;
    private static boolean    _preserveTagCase;
    private static boolean    _returnHTMLDocument;
    private static boolean    _parserWarningsEnabled;


    /**
     * Resets all settings to their default values. This includes the parser selection.
     */
    public static void reset() {
        _preserveTagCase = false;
        _returnHTMLDocument = true;
        _parserWarningsEnabled = false;
        _htmlParser = null;
    }


    /**
     * Selects the JTidy parser, if present.
     */
    public static void useJTidyParser() {
        if (_jtidyParser == null) throw new RuntimeException( "JTidy parser not available" );
        _htmlParser = _jtidyParser;
    }


    /**
     * Selects the NekoHTML parser, if present.
     */
    public static void useNekoHTMLParser() {
        if (_nekoParser == null) throw new RuntimeException( "NekoHTML parser not available" );
        _htmlParser = _nekoParser;
    }


    /**
     * Specifies the parser to use.
     */
    public static void setHTMLParser( HTMLParser htmlParser ) {
        _htmlParser = htmlParser;
    }


    /**
     * Returns the current selected parser.
     */
    public static HTMLParser getHTMLParser() {
        if (_htmlParser == null) {
            if (_nekoParser != null) {
                _htmlParser = _nekoParser;
            } else if (_jtidyParser != null) {
                _htmlParser = _jtidyParser;
            } else {
                throw new RuntimeException( "No HTML parser found. Make sure that either nekoHTML.jar or Tidy.jar is in the in classpath" );
            }
        }
        return _htmlParser;
    }


    /**
     * Returns true if the current parser will preserve the case of HTML tags and attributes.
     */
    public static boolean isPreserveTagCase() {
        return _preserveTagCase && getHTMLParser().supportsPreserveTagCase();
    }


    /**
     * Specifies whether the parser should preserve the case of HTML tags and attributes. Not every parser can
     * support this capability.  Note that enabling this will disable support for the HTMLDocument class.
     * @see #setReturnHTMLDocument
     */
    public static void setPreserveTagCase( boolean preserveTagCase ) {
        _preserveTagCase = preserveTagCase;
        if (preserveTagCase) _returnHTMLDocument = false;
    }


    /**
     * Returns true if the current parser will return an HTMLDocument object rather than a Document object.
     */
    public static boolean isReturnHTMLDocument() {
        return _returnHTMLDocument && getHTMLParser().supportsReturnHTMLDocument();
    }


    /**
     * Specifies whether the parser should return an HTMLDocument object rather than a Document object.
     * Not every parser can support this capability.  Note that enabling this will disable preservation of tag case.
     * @see #setPreserveTagCase
     */
    public static void setReturnHTMLDocument( boolean returnHTMLDocument ) {
        _returnHTMLDocument = returnHTMLDocument;
        if (returnHTMLDocument) _preserveTagCase = false;
    }


    /**
     * Returns true if parser warnings are enabled.
     **/
    public static boolean isParserWarningsEnabled() {
        return _parserWarningsEnabled && getHTMLParser().supportsParserWarnings();
    }


    /**
     * If true, tells the parser to display warning messages. The default is false (warnings are not shown).
     **/
    public static void setParserWarningsEnabled( boolean enabled ) {
        _parserWarningsEnabled = enabled;
    }


    /**
     * Remove an HTML Parser listener.
     **/
    public static void removeHTMLParserListener( HTMLParserListener el ) {
        _listeners.removeElement( el );
    }


    /**
     * Add an HTML Parser listener.
     **/
    public static void addHTMLParserListener( HTMLParserListener el ) {
        _listeners.addElement( el );
    }


//------------------------------------- package protected members ------------------------------------------------------


    /**
     * Get the list of Html Error Listeners
     **/
    static Vector getHTMLParserListeners() {
        return _listeners;
    }


    private static HTMLParser loadParserIfSupported( final String testClassName, final String parserClassName ) {
        try {
            Class.forName( testClassName );
            return (HTMLParser) Class.forName( parserClassName ).newInstance();
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (ClassNotFoundException e) {
        }
        return null;
    }


    static {
        _jtidyParser = loadParserIfSupported( "org.w3c.tidy.Parser", "com.meterware.httpunit.parsing.JTidyHTMLParser" );
        _nekoParser  = loadParserIfSupported( "org.cyberneko.html.HTMLConfiguration", "com.meterware.httpunit.parsing.NekoHTMLParser" );
        reset();
    }


}
