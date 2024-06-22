package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: Button.java,v 1.15 2004/06/29 23:24:12 russgold Exp $
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

import java.io.IOException;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;


/**
 * A button in a form.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class Button extends FormControl {

    static final public HTMLElementPredicate WITH_ID;
    static final public HTMLElementPredicate WITH_LABEL;

    private String _onClickEvent = "";
    private WebResponse _baseResponse;


    public String getType() {
        return BUTTON_TYPE;
    }


    Button( WebForm form ) {
        super( form );
    }


    Button( WebForm form, Node node ) {
        super( form, node );
        _onClickEvent = NodeUtils.getNodeAttribute( node, "onclick" );
    }


    Button( WebResponse response, Node node ) {
        super( null, node );
        _onClickEvent = NodeUtils.getNodeAttribute( node, "onclick" );
        _baseResponse = response;
    }


    /**
     * Returns the value associated with this button.
     **/
    public String getValue() {
        return getValueAttribute();
    }


    /**
     * Performs the action associated with clicking this button after running any 'onClick' script.
     * For a submit button this typically submits the form.
     */
    public void click() throws IOException, SAXException {
        verifyButtonEnabled();
        if (doOnClickEvent()) doButtonAction();
    }


    protected void verifyButtonEnabled() {
        if (isDisabled()) throw new IllegalStateException( "Button" + (getName().length() == 0 ? "" : " '" + getName() + "'") + " is disabled and may not be clicked." );
    }


    /**
     * Returns true if this button is disabled, meaning that it cannot be clicked.
     **/
    public boolean isDisabled() {
        return super.isDisabled();
    }


    /**
     * Does the 'onClick' event defined for this button.
     * @return true if subsequent actions should be performed.
     */
    final protected boolean doOnClickEvent() {
        return _onClickEvent.length() == 0 || getScriptableDelegate().doEvent( _onClickEvent );
    }


    /**
     * Perform the normal action of this button.
     */
    protected void doButtonAction() throws IOException, SAXException {}


//-------------------------------------------------- FormControl methods -----------------------------------------------


    String[] getValues() {
        return new String[ 0 ];
    }


    void addValues( ParameterProcessor processor, String characterSet ) throws IOException {
    }


    protected ScriptableDelegate newScriptable() {
        return new Scriptable();
    }


    protected ScriptableDelegate getParentDelegate() {
        if (getForm() != null) return super.getParentDelegate();
        return _baseResponse.getScriptableObject().getDocument();
    }


    class Scriptable extends FormControl.Scriptable {

        public void click() throws IOException, SAXException {
            doButtonAction();
        }
    }


    static {
        WITH_ID = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object button, Object id ) {
                return ((Button) button).getID().equals( id );
            };
        };

        WITH_LABEL = new HTMLElementPredicate() {
            public boolean matchesCriteria( Object button, Object label ) {
                return ((Button) button).getValue().equals( label );
            };
        };

    }
}
