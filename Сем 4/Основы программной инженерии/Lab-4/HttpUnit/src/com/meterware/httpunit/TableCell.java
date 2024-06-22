package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: TableCell.java,v 1.19 2004/09/29 17:15:24 russgold Exp $
*
* Copyright (c) 2000-2004, Russell Gold
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

import java.net.URL;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A single cell in an HTML table.
 **/
public class TableCell extends BlockElement {


    /**
     * Returns the number of columns spanned by this cell.
     **/
    public int getColSpan() {
        return _colSpan;
    }


    /**
     * Returns the number of rows spanned by this cell.
     **/
    public int getRowSpan() {
        return _rowSpan;
    }


    /**
     * Returns the text value of this cell.
     * @deprecated as of 1.6, use #getText()
     */
    public String asText() {
        return getText();
    }


//---------------------------------------- package methods -----------------------------------------


    TableCell( WebResponse response, FrameSelector frame, Element cellNode, URL url, String parentTarget, String characterSet ) {
        super( response, frame, url, parentTarget, cellNode, characterSet );
        _colSpan = getAttributeValue( cellNode, "colspan", 1 );
        _rowSpan = getAttributeValue( cellNode, "rowspan", 1 );
    }


//----------------------------------- private fields and methods -----------------------------------


    private int     _colSpan;
    private int     _rowSpan;


}

