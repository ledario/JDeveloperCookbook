package com.packt.jdeveloper.cookbook.shared.bc.exceptions;

import com.packt.jdeveloper.cookbook.shared.bc.exceptions.messages.BundleUtils;

import java.util.Locale;
import java.util.ResourceBundle;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.JboException;

public class ExtJboException extends JboException {
    
    private static final String ERRORS_BUNDLE =
        "com.packt.jdeveloper.cookbook.shared.bc.exceptions.messages. ErrorMessages";
    private static final String PARAMETERS_BUNDLE = 
        "com.packt.jdeveloper.cookbook.shared.bc.exceptions.messages.ErrorParams";
    private static final String MESSAGE_PREFIX = "message."; 
    private static final String PARAMETER_PREFIX = "parameter.";
    private static final ADFLogger LOGGER = 
        ADFLogger.createADFLogger(ExtJboException.class);
    
    /**
    * Constructor to create an exception using a standard error code and 
    * error message parameters
    * @param errorCode, the error message code
    * @param errorParameters, the error message parameters
    */
    public ExtJboException(final String errorCode, 
                           final Object[] errorParameters) {
        super(ResourceBundle.class, errorCode, errorParameters); 
    }
    
    public ExtJboException(final String errorCode) { 
        super(ResourceBundle.class, errorCode, null);
    }

    @Override   
    public String getMessage() {
        return BundleUtils.loadMessage(this.getErrorCode(), this.getErrorParameters());
    }

    
    // for testing purposes; remove or comment if not needed
    public static void main(String[] args) { 
        // throw a custom exception with error code "00001" and two parameters 
        throw new ExtJboException("00001", 
                                  new String[] { "FirstParameter", "SecondParameter" });
    }
}
