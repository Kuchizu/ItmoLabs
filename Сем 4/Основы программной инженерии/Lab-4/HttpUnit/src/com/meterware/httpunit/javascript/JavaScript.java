package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id: JavaScript.java,v 1.63 2004/12/02 03:43:20 russgold Exp $
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
import com.meterware.httpunit.*;

import com.meterware.httpunit.scripting.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.io.IOException;
import java.net.URL;

import org.mozilla.javascript.*;
import org.xml.sax.SAXException;


/**
 * This class is the Rhino-compatible implementation of the JavaScript DOM objects.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class JavaScript {

    private final static Object[] NO_ARGS = new Object[0];

    private static boolean _throwExceptionsOnError = true;

    private static ArrayList _errorMessages = new ArrayList();


    static boolean isThrowExceptionsOnError() {
        return _throwExceptionsOnError;
    }


    static void setThrowExceptionsOnError( boolean throwExceptionsOnError ) {
        _throwExceptionsOnError = throwExceptionsOnError;
    }


    static void clearErrorMessages() {
        _errorMessages.clear();
    }


    static String[] getErrorMessages() {
        return (String[]) _errorMessages.toArray( new String[ _errorMessages.size() ] );
    }


    /**
     * Initiates JavaScript execution for the specified web response.
     */
    static void run( WebResponse response ) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassDefinitionException, NotAFunctionException,
            PropertyException, SAXException, JavaScriptException {
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects( null );
        initHTMLObjects( scope );

        Window w = (Window) context.newObject( scope, "Window" );
        w.initialize( null, response.getScriptableObject() );
    }


    /**
     * Runs the onload event for the specified web response.
     */
    public static void load( WebResponse response ) throws ClassDefinitionException, InstantiationException, IllegalAccessException, InvocationTargetException, PropertyException, JavaScriptException, SAXException, NotAFunctionException {
        if (!(response.getScriptableObject().getScriptEngine() instanceof JavaScriptEngine)) run( response );
        response.getScriptableObject().load();
    }


    private static void initHTMLObjects( Scriptable scope ) throws IllegalAccessException, InstantiationException,
            InvocationTargetException, ClassDefinitionException, PropertyException {
        ScriptableObject.defineClass( scope, Window.class );
        ScriptableObject.defineClass( scope, Document.class );
        ScriptableObject.defineClass( scope, Style.class );
        ScriptableObject.defineClass( scope, Location.class );
        ScriptableObject.defineClass( scope, Navigator.class );
        ScriptableObject.defineClass( scope, Screen.class );
        ScriptableObject.defineClass( scope, Link.class );
        ScriptableObject.defineClass( scope, Form.class );
        ScriptableObject.defineClass( scope, Control.class );
        ScriptableObject.defineClass( scope, Link.class );
        ScriptableObject.defineClass( scope, Image.class );
        ScriptableObject.defineClass( scope, Options.class );
        ScriptableObject.defineClass( scope, Option.class );
        ScriptableObject.defineClass( scope, ElementArray.class );
        ScriptableObject.defineClass( scope, HTMLElement.class );
    }


    abstract static class JavaScriptEngine extends ScriptableObject implements ScriptingEngine {

        protected ScriptableDelegate _scriptable;
        protected JavaScriptEngine   _parent;


        public boolean supportsScriptLanguage( String language ) {
            return language == null || language.toLowerCase().startsWith( "javascript" );
        }


        public String executeScript( String language, String script ) {
            if (!supportsScriptLanguage( language )) return "";
            try {
                script = script.trim();
                if (script.startsWith( "<!--" )) {
                    script = withoutFirstLine( script );
                    if (script.endsWith( "-->" )) script = script.substring( 0, script.lastIndexOf( "-->" ));
                }
                Context.getCurrentContext().evaluateString( this, script, "httpunit", 0, null );
                StringBuffer buffer = getDocumentWriteBuffer();
                return buffer.toString();
            } catch (Exception e) {
                handleScriptException( e, "Script '" + script + "'" );
                return "";
            } finally {
                discardDocumentWriteBuffer();
            }
        }


        protected StringBuffer getDocumentWriteBuffer() {
            throw new IllegalStateException( "may not run executeScript() from " + getClass() );
        }


        protected void discardDocumentWriteBuffer() {
            throw new IllegalStateException( "may not run executeScript() from " + getClass() );
        }


        private String withoutFirstLine( String script ) {
            for (int i=0; i < script.length(); i++) {
                if (isLineTerminator( script.charAt(i) )) return script.substring( i ).trim();
            }
            return "";
        }


        private boolean isLineTerminator( char c ) {
            return c == 0x0A || c == 0x0D;
        }


        public boolean performEvent( String eventScript ) {
            try {
                final Context context = Context.getCurrentContext();
                context.setOptimizationLevel( -1 );
                Function f = context.compileFunction( this, "function x() { " + eventScript + "}", "httpunit", 0, null );
                Object result = f.call( context, this, this, NO_ARGS );
                return (result instanceof Boolean) ? ((Boolean) result).booleanValue() : true;
            } catch (Exception e) {
                handleScriptException( e, "Event '" + eventScript + "'" );
                return false;
            }
        }


        /**
         * Evaluates the specified string as JavaScript. Will return null if the script has no return value.
         */
        public String evaluateScriptExpression( String urlString ) {
            try {
                Object result = Context.getCurrentContext().evaluateString( this, urlString, "httpunit", 0, null );
                return (result == null || result instanceof Undefined) ? null : result.toString();
            } catch (Exception e) {
                handleScriptException( e, "URL '" + urlString + "'" );
                return null;
            }
        }


        private void handleScriptException( Exception e, String badScript ) {
            final String errorMessage = badScript + " failed: " + e;
            if (!(e instanceof EcmaError) && !(e instanceof EvaluatorException)) {
                e.printStackTrace();
                throw new RuntimeException( errorMessage );
            } else if (isThrowExceptionsOnError()) {
                e.printStackTrace();
                throw new ScriptException( errorMessage );
            } else {
                _errorMessages.add( errorMessage );
            }
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws SAXException, PropertyException, JavaScriptException, NotAFunctionException {
            _scriptable = scriptable;
            _scriptable.setScriptEngine( this );
            _parent = parent;
            if (parent != null) setParentScope( parent );
       }


        String getName() {
            return _scriptable instanceof NamedDelegate ? ((NamedDelegate) _scriptable).getName() : "";
        }


        String getID() {
            return _scriptable instanceof IdentifiedDelegate ? ((IdentifiedDelegate) _scriptable).getID() : "";
        }


        public boolean has( String propertyName, Scriptable scriptable ) {
            return super.has( propertyName, scriptable ) ||
                    (_scriptable != null && _scriptable.get( propertyName ) != null);
        }


        public Object get( String propertyName, Scriptable scriptable ) {
            Object result = super.get( propertyName, scriptable );
            if (result != NOT_FOUND) return result;
            if (_scriptable == null) return NOT_FOUND;

            return convertIfNeeded( _scriptable.get( propertyName ) );

        }


        public Object get( int i, Scriptable scriptable ) {
            Object result = super.get( i, scriptable );
            if (result != NOT_FOUND) return result;
            if (_scriptable == null) return NOT_FOUND;

            return convertIfNeeded( _scriptable.get( i ) );
        }


        private Object convertIfNeeded( final Object property ) {
            if (property == null) return NOT_FOUND;

            if (property instanceof ScriptableDelegate[]) return toScriptable( (ScriptableDelegate[]) property );
            if (!(property instanceof ScriptableDelegate)) return property;
            return toScriptable( (ScriptableDelegate) property );
        }


        private Object toScriptable( ScriptableDelegate[] list ) {
            Object[] delegates = new Object[ list.length ];
            for (int i = 0; i < delegates.length; i++) {
                delegates[i] = toScriptable( list[i] );
            }
            return Context.getCurrentContext().newArray( this, delegates );
        }


        public void put( String propertyName, Scriptable scriptable, Object value ) {
            if (_scriptable == null || _scriptable.get( propertyName ) == null) {
                super.put( propertyName, scriptable, value );
            } else {
                _scriptable.set( propertyName, value );
            }
        }


        public String toString() {
            return (_scriptable == null ? "prototype " : "") + getClassName();
        }


        public ScriptingEngine newScriptingEngine( ScriptableDelegate child ) {
            try {
                return (ScriptingEngine) toScriptable( child );
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException( e.toString() );
            }
        }


        public void clearCaches() {
        }


        protected static String toStringIfNotUndefined( Object object ) {
            return (object == null || Undefined.instance.equals( object )) ? null : object.toString();
        }


        /**
         * Converts a scriptable delegate obtained from a subobject into the appropriate Rhino-compatible Scriptable.
         **/
        final Object toScriptable( ScriptableDelegate delegate ) {
            if (delegate == null) {
                return NOT_FOUND;
            } else if (delegate.getScriptEngine() instanceof Scriptable) {
                return (Scriptable) delegate.getScriptEngine();
            } else {
                try {
                    JavaScriptEngine element = (JavaScriptEngine) Context.getCurrentContext().newObject( this, getScriptableClassName( delegate ) );
                    element.initialize( this, delegate );
                    return element;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RhinoException( e );
                }
            }
        }


        private String getScriptableClassName( ScriptableDelegate delegate ) {
            if (delegate instanceof WebResponse.Scriptable) return "Window";
            if (delegate instanceof HTMLPage.Scriptable) return "Document";
            if (delegate instanceof WebForm.Scriptable) return "Form";
            if (delegate instanceof WebLink.Scriptable) return "Link";
            if (delegate instanceof WebImage.Scriptable) return "Image";
            if (delegate instanceof SelectionOptions) return "Options";
            if (delegate instanceof SelectionOption) return "Option";
            if (delegate instanceof Input) return "Control";
            if (delegate instanceof DocumentElement) return "HTMLElement";

            throw new IllegalArgumentException( "Unknown ScriptableDelegate class: " + delegate.getClass() );
        }


        protected ElementArray toElementArray( ScriptableDelegate[] scriptables ) {
            JavaScriptEngine[] elements = new JavaScriptEngine[ scriptables.length ];
            for (int i = 0; i < elements.length; i++) {
                elements[ i ] = (JavaScriptEngine) toScriptable( scriptables[ i ] );
            }
            ElementArray result = ElementArray.newElementArray( this );
            result.initialize( elements );
            return result;
        }

    }


    static public class Window extends JavaScriptEngine {

        private Document     _document;
        private Navigator    _navigator;
        private Location     _location;
        private Screen       _screen;
        private ElementArray _frames;


        public String getClassName() {
            return "Window";
        }


        public Window jsGet_window() {
            return this;
        }


        public Window jsGet_self() {
            return this;
        }


        public Document jsGet_document() {
            if (_document == null) {
                _document = (Document) toScriptable( getDelegate().getDocument() );
            }
            return _document;
        }


        public Scriptable jsGet_frames() throws SAXException, PropertyException, JavaScriptException, NotAFunctionException {
            if (_frames == null) {
                WebResponse.Scriptable scriptables[] = getDelegate().getFrames();
                Window[] frames = new Window[ scriptables.length ];
                for (int i = 0; i < frames.length; i++) {
                    frames[ i ] = (Window) toScriptable( scriptables[ i ] );
                }
                _frames = (ElementArray) Context.getCurrentContext().newObject( this, "ElementArray" );
                _frames.initialize( frames );
            }
            return _frames;
        }


        public Navigator jsGet_navigator() {
            return _navigator;
        }


        public Screen jsGet_screen() {
            return _screen;
        }


        public Location jsGet_location() {
            return _location;
        }


        public void jsSet_location( String relativeURL ) throws IOException, SAXException {
            setLocation( relativeURL );
        }


        void setLocation( String relativeURL ) throws IOException, SAXException {
            getDelegate().setLocation( relativeURL );
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );

            _location = (Location) Context.getCurrentContext().newObject( this, "Location" );
            _location.initialize(this, ((WebResponse.Scriptable) scriptable).getURL() );

            _navigator = (Navigator) Context.getCurrentContext().newObject( this, "Navigator" );
            _navigator.setClientProperties( getDelegate().getClientProperties() );

            _screen = (Screen) Context.getCurrentContext().newObject( this, "Screen" );
            _screen.setClientProperties( getDelegate().getClientProperties() );
        }


        public void jsFunction_alert( String message ) {
            getDelegate().alert( message );
        }


        public boolean jsFunction_confirm( String message ) {
            return getDelegate().getConfirmationResponse( message );
        }


        public String jsFunction_prompt( String message, String defaultResponse ) {
            return getDelegate().getUserResponse( message, defaultResponse );
        }


        public void jsFunction_moveTo( int x, int y ) {
        }


        public void jsFunction_focus() {
        }


        public void jsFunction_setTimeout() {
        }


        public void jsFunction_close() {
            getDelegate().close();
        }


        public Window jsFunction_open( Object url, String name, String features, boolean replace )
                throws PropertyException, JavaScriptException, NotAFunctionException, IOException, SAXException {
            WebResponse.Scriptable delegate = getDelegate().open( toStringIfNotUndefined( url ), name, features, replace );
            return delegate == null ? null : (Window) toScriptable( delegate );
        }


        public void clearCaches() {
            if (_document != null) _document.clearCaches();
        }


        protected StringBuffer getDocumentWriteBuffer() {
            return jsGet_document().getWriteBuffer();
        }


        protected void discardDocumentWriteBuffer() {
            jsGet_document().clearWriteBuffer();
        }


        private WebResponse.Scriptable getDelegate() {
            return (WebResponse.Scriptable) _scriptable;
        }
    }


    static public class Document extends JavaScriptEngine {

        private ElementArray _forms;
        private ElementArray _links;
        private ElementArray _images;
        private StringBuffer _writeBuffer;
        private String _mimeType;


        public String getClassName() {
            return "Document";
        }


        public void clearCaches() {
            _forms = _links = _images = null;
        }


        public String jsGet_title() throws SAXException {
            return getDelegate().getTitle();
        }


        public Scriptable jsGet_images() throws SAXException{
            if (_images == null) _images = toElementArray( getDelegate().getImages() );
            return _images;
        }


        public Scriptable jsGet_links() throws SAXException {
            if (_links == null) _links = toElementArray( getDelegate().getLinks() );
            return _links;
        }


        public Scriptable jsGet_forms() throws SAXException {
            if (_forms == null) _forms = toElementArray( getDelegate().getForms() );
            return _forms;
        }


        public Object jsFunction_getElementById( String id ) {
            ScriptableDelegate elementWithID = getDelegate().getElementWithID( id );
            return elementWithID == null ? null : toScriptable( elementWithID );
        }


        public Object jsFunction_getElementsByName( String name ) {
            return toElementArray( getDelegate().getElementsByName( name ) );
        }


        public Object jsFunction_getElementsByTagName( String name ) {
            return toElementArray( getDelegate().getElementsByTagName( name ) );
        }


        public Object jsGet_location() {
            return _parent == null ? NOT_FOUND : getWindow().jsGet_location();
        }


        public void jsSet_location( String urlString ) throws IOException, SAXException {
            if (urlString.startsWith( "color" )) return;
            getWindow().setLocation( urlString );
        }


        public String jsGet_cookie() {
            return getDelegate().getCookie();
        }


        public void jsSet_cookie( String cookieSpec ) {
            final int equalsIndex = cookieSpec.indexOf( '=' );
            if (equalsIndex <0) return;
            int endIndex = cookieSpec.indexOf( ";", equalsIndex );
            if (endIndex < 0) endIndex = cookieSpec.length();
            String name = cookieSpec.substring( 0, equalsIndex );
            String value = cookieSpec.substring( equalsIndex+1, endIndex );
            getDelegate().setCookie( name, value );
        }


        private Window getWindow() {
            return ((Window) _parent);
        }


        public void jsFunction_open( Object mimeType ) {
            _mimeType = toStringIfNotUndefined( mimeType );
        }


        public void jsFunction_close() {
            if (getDelegate().replaceText( getWriteBuffer().toString(), _mimeType == null ? "text/html" : _mimeType )) {
                getWriteBuffer().setLength(0);
            }
        }


        public void jsFunction_write( String string ) {
            getWriteBuffer().append( string );
        }


        public void jsFunction_writeln( String string ) {
            getWriteBuffer().append( string ).append( (char) 0x0D ).append( (char) 0x0A );
        }


        protected StringBuffer getWriteBuffer() {
            if (_writeBuffer == null) _writeBuffer = new StringBuffer();
            return _writeBuffer;
        }


        protected void clearWriteBuffer() {
            _writeBuffer = null;
        }


        private HTMLPage.Scriptable getDelegate() {
            return (HTMLPage.Scriptable) _scriptable;
        }

    }


    static public class Location extends JavaScriptEngine {

        private URL _url;
        private Window _window;

        public String getClassName() {
            return "Location";
        }


        void initialize( Window window, URL url ) {
            _window = window;
            _url = url;
        }


        public void jsFunction_replace( String urlString ) throws IOException, SAXException {
            _window.setLocation( urlString );
        }


        public String jsGet_href() {
            return toString();
        }


        public void jsSet_href( String urlString ) throws SAXException, IOException {
            _window.setLocation( urlString );
        }


        public String jsGet_protocol() {
            return _url.getProtocol() + ':';
        }


        public String jsGet_host() {
            return _url.getHost() + ':' + _url.getPort();
        }


        public String jsGet_hostname() {
            return _url.getHost();
        }


        public String jsGet_port() {
            return String.valueOf( _url.getPort() );
        }


        public String jsGet_pathname() {
            return _url.getPath();
        }


        public void jsSet_pathname( String newPath ) throws SAXException, IOException {
            if (!newPath.startsWith( "/" )) newPath = '/' + newPath;
            URL newURL = new URL( _url, newPath );
            _window.setLocation( newURL.toExternalForm() );
        }


        public String jsGet_search() {
            return '?' + _url.getQuery();
        }


        public void jsSet_search( String newSearch ) throws SAXException, IOException {
            if (!newSearch.startsWith( "?" )) newSearch = '?' + newSearch;
            _window.setLocation( jsGet_protocol() + "//" + jsGet_host() + jsGet_pathname() + newSearch );
        }


        /**
         * Returns the default value of this scriptable object. In this case, it returns simply the URL as a string.
         * Note that this method is necessary, since Rhino will only call the toString method directly if there are no
         * Rhino methods defined (jsGet_*, jsFunction_*, etc.)
         */
        public Object getDefaultValue( Class typeHint ) {
            return _url.toExternalForm();
        }


        public String toString() {
            return _url.toExternalForm();
        }

    }


    static public class Style extends JavaScriptEngine {

        private String _display    = "inline";
        private String _visibility = "visible";


        public String getClassName() {
            return "Style";
        }


        public String jsGet_display() {
            return _display;
        }


        public void jsSet_display( String display ) {
            _display = display;
        }


        public String jsGet_visibility() {
            return _visibility;
        }


        public void jsSet_visibility( String visibility ) {
            _visibility = visibility;
        }
    }


    static public class Navigator extends JavaScriptEngine {

        private ClientProperties _clientProperties;

        public String getClassName() {
            return "Navigator";
        }


        void setClientProperties( ClientProperties clientProperties ) {
            _clientProperties = clientProperties;
        }


        public String jsGet_appName() {
            return _clientProperties.getApplicationName();
        }


        public String jsGet_appCodeName() {
            return _clientProperties.getApplicationCodeName();
        }


        public String jsGet_appVersion() {
            return _clientProperties.getApplicationVersion();
        }


        public String jsGet_userAgent() {
            return _clientProperties.getUserAgent();
        }


        public String jsGet_platform() {
            return _clientProperties.getPlatform();
        }


        public Object[] jsGet_plugins() {
            return new Object[0];
        }


        public boolean jsFunction_javaEnabled() {
            return false;   // no support is provided for applets at present
        }


    }


    static public class Screen extends JavaScriptEngine {

        private ClientProperties _clientProperties;


        void setClientProperties( ClientProperties clientProperties ) {
            _clientProperties = clientProperties;
        }


        public String getClassName() {
            return "Screen";
        }


        public int jsGet_availWidth() {
            return _clientProperties.getAvailableScreenWidth();
        }


        public int jsGet_availHeight() {
            return _clientProperties.getAvailHeight();
        }


    }


    static public class ElementArray extends ScriptableObject {

        private JavaScriptEngine _contents[] = new HTMLElement[0];


        static ElementArray newElementArray( Scriptable parent ) {
            try {
                return (ElementArray) Context.getCurrentContext().newObject( parent, "ElementArray" );
            } catch (PropertyException e) {
                throw new RhinoException( e );
            } catch (NotAFunctionException e) {
                throw new RhinoException( e );
            } catch (JavaScriptException e) {
                throw new RhinoException( e );
            }
        }


        public ElementArray() {
        }


        void initialize( JavaScriptEngine[] contents ) {
            _contents = contents;
        }


        public int jsGet_length() {
            return _contents.length;
        }


        public String getClassName() {
            return "ElementArray";
        }


        public Object get( int i, Scriptable scriptable ) {
            if (i >= 0 && i < _contents.length) {
                return _contents[i];
            } else {
                return super.get( i, scriptable );
            }
        }


        public Object get( String name, Scriptable scriptable ) {
            for (int i = 0; i < _contents.length; i++) {
                JavaScriptEngine content = _contents[ i ];
                if (name.equalsIgnoreCase( content.getID() )) return content;
            }
            for (int i = 0; i < _contents.length; i++) {
                JavaScriptEngine content = _contents[ i ];
                if (name.equalsIgnoreCase( content.getName() )) return content;
            }
            return super.get( name, scriptable );
        }


        protected JavaScriptEngine[] getContents() {
            return _contents;
        }
    }


    static public class HTMLElement extends JavaScriptEngine {

        private Style _style;
        private Document _document;


        public String getClassName() {
            return "HTMLElement";
        }


        public Document jsGet_document() {
            return _document;
        }


        public Style jsGet_style() {
            return _style;
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            _document = (Document) parent;
            _style = (Style) Context.getCurrentContext().newObject( this, "Style" );
        }

    }


    static public class Image extends HTMLElement {

        public String getClassName() {
            return "Image";
        }
    }


    static public class Link extends HTMLElement {

        public Document jsGet_document() {
            return super.jsGet_document();
        }


        public String getClassName() {
            return "Link";
        }
    }


    static public class Form extends HTMLElement {

        private ElementArray _controls;

        public String getClassName() {
            return "Form";
        }


        public String jsGet_action() {
            return getDelegate().getAction();
        }


        public void jsSet_action( String action ) {
            getDelegate().setAction( action );
        }


        public Scriptable jsGet_elements() throws PropertyException, NotAFunctionException, JavaScriptException {
            if (_controls == null) {
                initializeControls();
            }
            return _controls;
        }


        public Object jsFunction_getElementsByTagName( String name ) throws SAXException {
            return toElementArray( getDelegate().getElementsByTagName( name ) );
        }


        public void jsFunction_submit() throws IOException, SAXException {
            getDelegate().submit();
        }


        public void jsFunction_reset() throws IOException, SAXException {
            getDelegate().reset();
        }


        private void initializeControls() throws PropertyException, NotAFunctionException, JavaScriptException {
            ScriptableDelegate scriptables[] = getDelegate().getElementDelegates();
            Control[] controls = new Control[ scriptables.length ];
            for (int i = 0; i < controls.length; i++) {
                controls[ i ] = (Control) toScriptable( scriptables[ i ] );
            }
            _controls = (ElementArray) Context.getCurrentContext().newObject( this, "ElementArray" );
            _controls.initialize( controls );
        }


        private WebForm.Scriptable getDelegate() {
            return (WebForm.Scriptable) _scriptable;
        }

    }


    static public class Control extends JavaScriptEngine {

        private Form _form;

        public String getClassName() {
            return "Control";
        }

        public Form jsGet_form() {
            return _form;
        }

        public void jsFunction_focus() {}


        public void jsFunction_select() {}


        public void jsFunction_click() throws IOException, SAXException {
            getDelegate().click();
        }


        private Input getDelegate() {
            return (Input) _scriptable;
        }


        void initialize( JavaScriptEngine parent, ScriptableDelegate scriptable )
                throws JavaScriptException, NotAFunctionException, PropertyException, SAXException {
            super.initialize( parent, scriptable );
            if (parent instanceof Form) _form = (Form) parent;
        }


    }


    static public class Options extends JavaScriptEngine {

        public String getClassName() {
            return "Options";
        }


        public int jsGet_length() {
            return getDelegate().getLength();
        }


        public void jsSet_length( int length ) {
            getDelegate().setLength( length );
        }


        public void put( int i, Scriptable scriptable, Object object ) {
            if (object == null) {
                getDelegate().put( i, null );
            } else {
                if (!(object instanceof Option)) throw new IllegalArgumentException( "May only add an Option to this array" );
                Option option = (Option) object;
                getDelegate().put( i, option.getDelegate() );
            }
        }


        private SelectionOptions getDelegate() {
            return (SelectionOptions) _scriptable;
        }


    }


    static public class Option extends JavaScriptEngine {

        public String getClassName() {
            return "Option";
        }


        public void jsConstructor( String text, String value, boolean defaultSelected, boolean selected ) {
            _scriptable = WebResponse.newDelegate( "Option" );
            getDelegate().initialize( text, value, defaultSelected, selected );
        }


        public int jsGet_index() {
            return getDelegate().getIndex();
        }


        public String jsGet_text() {
            return getDelegate().getText();
        }


        public void jsSet_text( String text ) {
            getDelegate().setText( text );
        }


        public String jsGet_value() {
            return getDelegate().getValue();
        }


        public void jsSet_value( String value ) {
            getDelegate().setValue( value );
        }


        public boolean jsGet_selected() {
            return getDelegate().isSelected();
        }


        public void jsSet_selected( boolean selected ) {
            getDelegate().setSelected( selected );
        }


        public boolean jsGet_defaultSelected() {
            return getDelegate().isDefaultSelected();
        }


        SelectionOption getDelegate() {
            return (SelectionOption) _scriptable;
        }
    }

}


class RhinoException extends RuntimeException {

    private Exception _cause;


    public RhinoException( Exception cause ) {
        _cause = cause;
    }


    public String getMessage() {
        return "Rhino exception: " + _cause;
    }
}
