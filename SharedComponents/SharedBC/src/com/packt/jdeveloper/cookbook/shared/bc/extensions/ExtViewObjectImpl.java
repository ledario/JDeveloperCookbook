package com.packt.jdeveloper.cookbook.shared.bc.extensions;

import oracle.jbo.AttributeDef;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.server.ViewAttributeDefImpl;
import oracle.jbo.server.ViewObjectImpl;

public class ExtViewObjectImpl extends ViewObjectImpl {
    private static final String NEW_ROW_AT_END = "NewRowAtEnd";
    
    protected void setQueriable(int attribute, boolean condition) { 
        // get the attribute definition
        AttributeDef def = getAttributeDef(attribute);
        // set/unset only if needed
        if (def != null && def.isQueriable() != condition) { 
            // set/unset queriable
            ViewAttributeDefImpl attributeDef = (ViewAttributeDefImpl)def; 
            attributeDef.setQueriable(condition);
        } 
    }

    @Override
    public void insertRow(Row row) {
        // check for overriden behavior based on custom property 
        if ("true".equalsIgnoreCase((String)this.getProperty(NEW_ROW_AT_END))) { 
            // get the last row in the rowset
            Row lastRow = this.last(); 
            if (lastRow != null) {
                // get index of last row
                int lastRowIdx = this.getRangeIndexOf(lastRow); 
                // insert row after the last row (added +1 to fix bug from this example)
                this.insertRowAtRangeIndex(lastRowIdx + 1, row); 
                // set inserted row as the current row 
                this.setCurrentRow(row);
            } else { 
                super.insertRow(row);
            }
        } else {
            // default behavior: insert at current rowset slot
            super.insertRow(row); 
        }
    }

    @Override
    protected void create() {
        super.create();
        // allow read-only View objects to use findByKey() methods 
        this.setManageRowsByKey(true);
    }
    
    public void refreshView() {
        Key curRowKey = null;
        int rangePosOfCurRow = -1;
        int rangeStart = -1;
        // get and save the current row
        Row currentRow = getCurrentRow();
        // do this only if we have a current row
        if (currentRow != null) {
            // get the row information
            curRowKey = currentRow.getKey();
            rangePosOfCurRow = getRangeIndexOf(currentRow);
            rangeStart = getRangeStart();
        }
        // execute the View object query
        executeQuery();
        // if we have a current row, restore it
        if (currentRow != null) {
            setRangeStart(rangeStart);
            findAndSetCurrentRowByKey(curRowKey, rangePosOfCurRow);
        }
    }
}
