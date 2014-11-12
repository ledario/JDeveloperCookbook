package com.packt.jdeveloper.cookbook.shared.bc.extensions;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.AttributeDef;
import oracle.jbo.Key;
import oracle.jbo.Row;
import oracle.jbo.server.TransactionEvent;
import oracle.jbo.server.ViewAttributeDefImpl;
import oracle.jbo.server.ViewObjectImpl;
import oracle.jbo.server.ViewRowImpl;

public class ExtViewObjectImpl extends ViewObjectImpl {
    
    // create an ADFLogger
    private static final ADFLogger LOGGER =
        ADFLogger.createADFLogger(ExtViewObjectImpl.class);

    private static final String NEW_ROW_AT_END = "NewRowAtEnd";
    
    private static Key currentRowKeyBeforeRollback = null;
    private static int rangeStartBeforeRollback = -1;
    private static int rangePosOfCurrentRowBeforeRollback = -1;
    
    public ExtViewObjectImpl() {
        super();
        // log a trace 
        String className = this.getClass().getCanonicalName();
//        LOGGER.log(ADFLogger.TRACE,
//                    "ExtViewObjectImpl was constructed");
        LOGGER.log(ADFLogger.TRACE, className);
    }
    
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

    @Override
    public void beforeRollback(TransactionEvent transactionEvent) {
        
        LOGGER.log(ADFLogger.TRACE,
                    "Entered beforeRollback(TransactionEvent)");
        
        // check for query execution
        if (isExecuted()) {
            // get the current row
            ViewRowImpl currentRow = (ViewRowImpl)getCurrentRow(); 
            if (currentRow != null) {
                // save the current row's key 
                currentRowKeyBeforeRollback = currentRow.getKey(); 
                // save range start
                rangeStartBeforeRollback = getRangeStart();
                LOGGER.log(ADFLogger.TRACE,
                            "rangeStartBeforeRollback = [" + rangeStartBeforeRollback + "]");

                // get index of current row in range 
                rangePosOfCurrentRowBeforeRollback = getRangeIndexOf(currentRow);
                LOGGER.log(ADFLogger.TRACE,
                            "rangePosOfCurrentRowBeforeRollback = [" + rangePosOfCurrentRowBeforeRollback + "]");
            }
        }
        super.beforeRollback(transactionEvent);
    }

    @Override
    public void afterRollback(TransactionEvent transactionEvent) {
        LOGGER.log(ADFLogger.TRACE,
                    "Entered afterRollback(TransactionEvent)");

       super.afterRollback(transactionEvent);
        
        // check for current row key to restore
        if (currentRowKeyBeforeRollback != null) {
            // execute View object's query 
            executeQuery();
            
            // set start range 
            LOGGER.log(ADFLogger.TRACE,
                        "rangeStartBeforeRollback = [" + rangeStartBeforeRollback + "]");
            setRangeStart(rangeStartBeforeRollback); 
            
            // set current row in range 
            LOGGER.log(ADFLogger.TRACE,
                        "rangePosOfCurrentRowBeforeRollback = [" + rangePosOfCurrentRowBeforeRollback + "]");
            findAndSetCurrentRowByKey(currentRowKeyBeforeRollback, 
                                      rangePosOfCurrentRowBeforeRollback);
        }
        // reset 
        currentRowKeyBeforeRollback = null;
    }

    @Override
    public void executeQuery() {
        LOGGER.log(ADFLogger.TRACE,
                    "Entered executeQuery()");
        super.executeQuery();
    }

    @Override
    public void findAndSetCurrentRowByKey(Key key, int i) {
        LOGGER.log(ADFLogger.TRACE,
                    "Entered findAndSetCurrentRowByKey(Key,int)");
        super.findAndSetCurrentRowByKey(key, i);
    }

    @Override
    protected boolean buildWhereClause(StringBuffer sqlBuffer, int noUserParams) {
        final String ROW_COUNT_LIMIT = "RowCountLimit";
        //return super.buildWhereClause(stringBuffer, i);
        // framework processing
        boolean appended = super.buildWhereClause(sqlBuffer, noUserParams);
        // check for a row count limit 
        String rowCountLimit = (String)this.getProperty(ROW_COUNT_LIMIT);
        // if a row count limit exists, limit the query
        if (rowCountLimit != null) {
            // check to see if a WHERE clause was appended; 
            // if not, we will append it
            if (!appended) {
                // append WHERE clause 
                sqlBuffer.append(" WHERE ");
                // indicate that a where clause was added 
                appended = true;
            }
            // a WHERE clause was appended by the framework; 
            // just amend it
            else {
                sqlBuffer.append(" AND ");
            }
            // add ROWNUM limit based on the pre-defined
            // custom property
            sqlBuffer.append("(ROWNUM <= " + rowCountLimit + ")");
        }
        // a true/false indicator whether a WHERE clause was appended 
        // is returned to the framework
        return appended;
    }
}
