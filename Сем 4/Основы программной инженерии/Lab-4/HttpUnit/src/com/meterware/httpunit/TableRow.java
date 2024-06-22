package com.meterware.httpunit;
/********************************************************************************************************************
 * $Id: TableRow.java,v 1.1 2006/03/09 01:52:28 russgold Exp $
 *
 * Copyright (c) 2005, Russell Gold
 *
 *******************************************************************************************************************/
import org.w3c.dom.Element;

import java.util.ArrayList;

import com.meterware.httpunit.scripting.ScriptableDelegate;

/**
 * @author <a href="mailto:russgold@gmail.com">Russell Gold</a>
 */
public class TableRow extends HTMLElementBase {

    private ArrayList _cells = new ArrayList();
    private WebTable _webTable;


    TableRow( WebTable webTable, Element rowNode ) {
        super( rowNode );
        _webTable = webTable;
    }


    TableCell[] getCells() {
        return (TableCell[]) _cells.toArray( new TableCell[ _cells.size() ]);
    }


    TableCell newTableCell( Element element ) {
        return _webTable.newTableCell( element );
    }


    void addTableCell( TableCell cell ) {
        _cells.add( cell );
    }


    protected ScriptableDelegate newScriptable() {
        return new HTMLElementScriptable( this );
    }


    protected ScriptableDelegate getParentDelegate() {
        return _webTable.getParentDelegate();
    }
}
