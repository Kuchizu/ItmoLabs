package com.meterware.httpunit.parsing;

import com.meterware.httpunit.scripting.ScriptableDelegate;

import java.net.URL;
import java.io.IOException;
import java.util.Enumeration;

import org.cyberneko.html.HTMLConfiguration;
import org.apache.xerces.xni.parser.XMLDocumentFilter;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.XNIException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

/**
 * 
 *
 * @author <a href="russgold@acm.org">Russell Gold</a>
 * @author <a href="mailto:Artashes.Aghajanyan@lycos-europe.com">Artashes Aghajanyan</a>
 **/
class NekoDOMParser extends org.apache.xerces.parsers.DOMParser {

    private static final String HTML_DOCUMENT_CLASS_NAME = "org.apache.html.dom.HTMLDocumentImpl";

    /** Error reporting feature identifier. */
    private static final String REPORT_ERRORS = "http://cyberneko.org/html/features/report-errors";

    /** Augmentations feature identifier. */
    private static final String AUGMENTATIONS = "http://cyberneko.org/html/features/augmentations";

    /** Filters property identifier. */
    private static final String FILTERS = "http://cyberneko.org/html/properties/filters";

    /** Element case settings. possible values: "upper", "lower", "match" */
    private static final String TAG_NAME_CASE = "http://cyberneko.org/html/properties/names/elems";

    /** Attribute case settings. possible values: "upper", "lower", "no-change" */
    private static final String ATTRIBUTE_NAME_CASE = "http://cyberneko.org/html/properties/names/attrs";

    private DocumentAdapter _documentAdapter;

    /** The node representing the document. **/
    private Node _documentNode;


    static NekoDOMParser newParser( DocumentAdapter adapter, URL url ) {
        final HTMLConfiguration configuration = new HTMLConfiguration();
        if (!HTMLParserFactory.getHTMLParserListeners().isEmpty() || HTMLParserFactory.isParserWarningsEnabled()) {
            configuration.setErrorHandler( new ErrorHandler( url ) );
            configuration.setFeature( REPORT_ERRORS, true);
        }
        configuration.setFeature( AUGMENTATIONS, true );
        final ScriptFilter javaScriptFilter = new ScriptFilter( configuration );
        configuration.setProperty( FILTERS, new XMLDocumentFilter[] { javaScriptFilter } );
        if (HTMLParserFactory.isPreserveTagCase()) {
            configuration.setProperty( TAG_NAME_CASE, "match" );
            configuration.setProperty( ATTRIBUTE_NAME_CASE, "no-change" );
        }

        try {
            final NekoDOMParser domParser = new NekoDOMParser( configuration, adapter );
            domParser.setFeature( DEFER_NODE_EXPANSION, false );
            if (HTMLParserFactory.isReturnHTMLDocument()) domParser.setProperty( DOCUMENT_CLASS_NAME, HTML_DOCUMENT_CLASS_NAME );
            javaScriptFilter.setParser( domParser );
            return domParser;
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException( e.toString() );
        } catch (SAXNotSupportedException e) {
            throw new RuntimeException( e.toString() );
        }

    }


    ScriptableDelegate getScriptableDelegate() {
        if (_documentNode == null) {
            Node node = getCurrentElementNode();
            while (!(node instanceof Document)) node = node.getParentNode();
            _documentNode = node;
        }
        _documentAdapter.setRootNode( _documentNode );
        return _documentAdapter.getScriptableObject();
    }


    private Node getCurrentElementNode() {
        try {
            final Node node = (Node) getProperty( CURRENT_ELEMENT_NODE );
            return node;
        } catch (SAXNotRecognizedException e) {
            throw new RuntimeException( CURRENT_ELEMENT_NODE + " property not recognized" );
        } catch (SAXNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException( CURRENT_ELEMENT_NODE + " property not supported" );
        }
    }


    String getIncludedScript( String srcAttribute ) {
        try {
            return _documentAdapter.getIncludedScript( srcAttribute );
        } catch (IOException e) {
            throw new ScriptException( e );
        }
    }


    NekoDOMParser( HTMLConfiguration configuration, DocumentAdapter adapter ) {
        super( configuration );
        _documentAdapter = adapter;
    }


    static class ScriptException extends RuntimeException {
        private IOException _cause;

        public ScriptException( IOException cause ) {
            _cause = cause;
        }

        public IOException getException() {
            return _cause;
        }
    }
}


class ErrorHandler implements XMLErrorHandler {

    private URL _url = null;

    ErrorHandler( URL url ) {
        _url = url;
    }

    public void warning( String domain, String key, XMLParseException warningException ) throws XNIException {
        if (HTMLParserFactory.isParserWarningsEnabled()) {
            System.out.println( "At line " + warningException.getLineNumber() + ", column " + warningException.getColumnNumber() + ": " + warningException.getMessage() );
        }

        Enumeration listeners = HTMLParserFactory.getHTMLParserListeners().elements();
        while (listeners.hasMoreElements()) {
            ((HTMLParserListener) listeners.nextElement()).warning( _url, warningException.getMessage(), warningException.getLineNumber(), warningException.getColumnNumber() );
        }
    }


    public void error( String domain, String key, XMLParseException errorException ) throws XNIException {
        Enumeration listeners = HTMLParserFactory.getHTMLParserListeners().elements();
        while (listeners.hasMoreElements()) {
            ((HTMLParserListener) listeners.nextElement()).error( _url, errorException.getMessage(), errorException.getLineNumber(), errorException.getColumnNumber() );
        }
    }


    public void fatalError( String domain, String key, XMLParseException fatalError ) throws XNIException {
        error( domain, key, fatalError );
        throw fatalError;
    }
}