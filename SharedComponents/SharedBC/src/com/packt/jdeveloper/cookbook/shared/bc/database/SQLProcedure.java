package com.packt.jdeveloper.cookbook.shared.bc.database;

import java.sql.CallableStatement;

import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

import oracle.jbo.JboException;
import oracle.jbo.server.DBTransaction;

public class SQLProcedure {
    
    // the transaction associated with procedure call
    private DBTransaction transaction;
    // a list of procedure parameters
    private List<SQLParameter> parameters = new ArrayList<SQLParameter>(); 
    // the callable statement
    private CallableStatement statement = null; 
    // the statement string
    private String statementString = null;
    // the procedure parameter count
    private int parameterCount = 0;
    
    public SQLProcedure(final String procedure, final DBTransaction transaction) {
        this.statementString = procedure;
        this.transaction = transaction; 
    }

    /**
     * @param data
     */
    public void setIN(final Object data) { 
        ++parameterCount;
        parameters.add(new SQLParameter(parameterCount, SQLParameter.TYPE.IN, 
                                        data, 0)); 
    }

    /**
     * @param returnType
     */
    public void setOUT(final int returnType) {
        ++parameterCount;
        parameters.add(new SQLParameter(parameterCount, SQLParameter.TYPE.OUT, 
                                        null, returnType));
    }

    /**
     * @param data
     * @param returnType
     */
    public void setINOUT(final Object data, final int returnType) {
        ++parameterCount;
        parameters.add(new SQLParameter(parameterCount, SQLParameter.TYPE.INOUT, 
                                        data, returnType));
    }

    /**
     * @param returnType
     */
    public void setRETURN(final int returnType) {
        // do not allow multiple RETURNs 
        SQLParameter returnParameter = hasRETURN(); 
        if (returnParameter != null) {
            // If a return parameter exists, update RETURN parameter type
            returnParameter.setReturnType(returnType); 
        } else {
            // If no RETURN parameter exists, create a new one
            ++parameterCount;
            // check RETURN parameter count; should always be the 1st
            if (parameterCount != 1) {
                // move any existing parameters after this new RETURN parameter
                shiftParameters(); 
            }
            // add RETURN parameter
            parameters.add(new SQLParameter(1, SQLParameter.TYPE.RETURN, null, returnType));
        } 
    }
    
    /**
     * @param parameterNumber the number position for the OUT parameter
     * @return value of the OUT parameter
     */
    public Object getOUT(final int parameterNumber) {
        Object returnedData = null;
        for (SQLParameter parameter : parameters) {
            if (parameter.getParameterNumber() == parameterNumber &&
                        (parameter.getType() == SQLParameter.TYPE.OUT ||
                         parameter.getType() == SQLParameter.TYPE.INOUT)) {
                returnedData = parameter.getOutputData();
            }
        }
        return returnedData;
    }
    
    public Object getRETURN() {
        Object returnedData = null;
        for (SQLParameter parameter : parameters) {
            if (parameter.getType() == SQLParameter.TYPE.RETURN) { 
                returnedData = parameter.getOutputData();
            } 
        }
        return returnedData; 
    }

    /**
     * @return the return parameter if there is one, otherwise null
     */
    private SQLParameter hasRETURN() {
        SQLParameter returnParameter = null;
        for (SQLParameter parameter : parameters) {
            if (parameter.getType() == SQLParameter.TYPE.RETURN) {
                returnParameter = parameter;
                break;
            }
        }
        return returnParameter;
    }
    
    private void shiftParameters() {
        for (SQLParameter parameter : parameters) {
            parameter.setParameterNumber(parameter.getParameterNumber() + 1);
        }
    }
    
    public void execute() { 
        try {
            // create statement for parameters 
            createStatementFromParameters(); 
            // add parameters to statement 
            addParameterValues();
            // execute callable statement 
            statement.execute();
            // retrieve returned data 
            retrieveData();
            // close callable statement 
            statement.close();
        } catch (SQLException e) { 
            throw new JboException(e);
        } 
    }

    private void createStatementFromParameters() { 
        String paramString = "";
        for (SQLParameter parameter : parameters) {
            switch (parameter.getType()) {
                case SQLParameter.TYPE.IN:
                case SQLParameter.TYPE.OUT:
                case SQLParameter.TYPE.INOUT:
                    // add parameter placeholder in statement
                    paramString += (paramString.length() > 0) ? ",?" : "?";
                    break;
                case SQLParameter.TYPE.RETURN:
                    // add return placeholder in statement
                    statementString = "?:=" + statementString;
                    break;
            } 
        }
        // add complete parameter string to statement
       if (paramString.length() > 0) {
           statementString += "(" + paramString + ")";
       }
       // wrap statement with BEGIN/END
       statementString = "BEGIN " + statementString + "; END;";
       // create CallableStatement from statement
        statement = transaction.createCallableStatement(statementString, 0);
    }
    
    private void addParameterValues() throws SQLException { 
        for (SQLParameter parameter : parameters) {
            switch (parameter.getType()) {
                case SQLParameter.TYPE.IN:
                    statement.setObject(parameter.getParameterNumber(), 
                                        parameter.getInputData());
                    break;
                case SQLParameter.TYPE.OUT:
                    statement.registerOutParameter(parameter. getParameterNumber(), 
                                                   parameter.getReturnType());
                    break;
                case SQLParameter.TYPE.INOUT:
                    statement.setObject(parameter.getParameterNumber(), 
                                        parameter.getInputData());
                    statement.registerOutParameter(parameter. getParameterNumber(), 
                                                   parameter.getReturnType());
                    break;
                case SQLParameter.TYPE.RETURN:
                    statement.registerOutParameter(parameter.getParameterNumber(), 
                                                   parameter.getReturnType());
                    break; 
            }
        } 
    }
    
    private void retrieveData() throws SQLException { 
        for (SQLParameter parameter : parameters) {
            if (    SQLParameter.TYPE.INOUT == parameter.getType() || 
                    SQLParameter.TYPE.OUT == parameter.getType() || 
                    SQLParameter.TYPE.RETURN == parameter.getType()) { 
                parameter.setOutputData(statement.getObject(parameter.getParameterNumber()));
            }
        } 
    }
}
