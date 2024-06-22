package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: WebApplet.java,v 1.7 2003/03/12 15:40:44 russgold Exp $
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
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.applet.Applet;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.List;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.meterware.httpunit.scripting.ScriptableDelegate;


/**
 * This class represents the embedding of an applet in a web page.
 *
 * @author <a href="mailto:Oliver.Imbusch.extern@HVBInfo.com">Oliver Imbusch</a>
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/ 
public class WebApplet extends HTMLElementBase {

    private WebResponse _response;
    private String      _baseTarget;

    private URL         _codeBase;
    private String      _className;
    private Applet      _applet;
    private HashMap     _parameters;
    private String[]    _parameterNames;

    final private String CLASS_EXTENSION = ".class";


    public WebApplet( WebResponse response, Node rootNode, String baseTarget ) {
        super( rootNode );
        _response   = response;
        _baseTarget = baseTarget;
    }


    /**
     * Returns the URL of the codebase used to find the applet classes
     */
    public URL getCodeBaseURL() throws MalformedURLException {
        if (_codeBase == null) {
            _codeBase = new URL( _response.getURL(), getCodeBase() );
        }
        return _codeBase;
    }


    private String getCodeBase() {
        final String codeBaseAttribute = getAttribute( "codebase", "/" );
        return codeBaseAttribute.endsWith( "/" ) ? codeBaseAttribute : (codeBaseAttribute + "/");
    }


    /**
     * Returns the name of the applet main class.
     */
    public String getMainClassName() {
        if (_className == null) {
            _className = getAttribute( "code" );
            if (_className.endsWith( CLASS_EXTENSION )) {
                _className = _className.substring( 0, _className.lastIndexOf( CLASS_EXTENSION ));
            }
            _className = _className.replace( '/', '.' ).replace( '\\', '.' );
        }
        return _className;
    }


    /**
     * Returns the width of the panel in which the applet will be drawn.
     */
    public int getWidth() {
        return Integer.parseInt( getAttribute( "width" ) );
    }


    /**
     * Returns the height of the panel in which the applet will be drawn.
     */
    public int getHeight() {
        return Integer.parseInt( getAttribute( "height" ) );
    }


    /**
     * Returns the archive specification.
     */
    public String getArchiveSpecification() {
        String specification = getParameter( "archive" );
        if (specification == null) specification = getAttribute( "archive" );
        return specification;
    }


    List getArchiveList() throws MalformedURLException {
        ArrayList al = new ArrayList();
        StringTokenizer st = new StringTokenizer( getArchiveSpecification(), "," );
        while (st.hasMoreTokens()) al.add( new URL( getCodeBaseURL(), st.nextToken() ) );
        return al;
    }


    /**
     * Returns an array containing the names of the parameters defined for the applet.
     */
    public String[] getParameterNames() {
        if (_parameterNames == null) {
            ArrayList al = new ArrayList( getParameterMap().keySet() );
            _parameterNames = (String[]) al.toArray( new String[ al.size() ] );
        }
        return _parameterNames;
    }


    /**
     * Returns the value of the specified applet parameter, or null if not defined.
     */
    public String getParameter( String name ) {
        return (String) getParameterMap().get( name );
    }


    private Map getParameterMap() {
        if (_parameters == null) {
            _parameters = new HashMap();
            NodeList nl = ((Element) getNode()).getElementsByTagName( "param" );
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                _parameters.put( NodeUtils.getNodeAttribute( n, "name", "" ), NodeUtils.getNodeAttribute( n, "value", "" ) );
            }
        }
        return _parameters;
    }


    public Applet getApplet() throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (_applet == null) {
            ClassLoader cl = new URLClassLoader( getClassPath(), null );
            Object o = cl.loadClass( getMainClassName() ).newInstance();
            if (!(o instanceof Applet)) throw new RuntimeException( getMainClassName() + " is not an Applet" );
            _applet = (Applet) o;
            _applet.setStub( new AppletStubImpl( this ) );
        }
        return _applet;
    }


    private URL[] getClassPath() throws MalformedURLException {
        List classPath = getArchiveList();
        classPath.add( getCodeBaseURL() );
        return (URL[]) classPath.toArray( new URL[ classPath.size() ] );
    }


    String getBaseTarget() {
        return _baseTarget;
    }


    WebApplet[] getAppletsInPage() {
        try {
            return _response.getApplets();
        } catch (SAXException e) {
            e.printStackTrace();  // should never happen.
            return null;
        }
    }


    void sendRequest( URL url, String target ) {
        WebRequest wr = new GetMethodWebRequest( null, url.toExternalForm(), target );
        try {
            _response.getWindow().getResponse( wr );
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            throw new RuntimeException( e.toString() );
        } catch (SAXException e) {
        }
    }


    protected ScriptableDelegate newScriptable() {
        return new HTMLElementScriptable( this );
    }


    protected ScriptableDelegate getParentDelegate() {
        return _response.getScriptableObject().getDocument();
    }

}
