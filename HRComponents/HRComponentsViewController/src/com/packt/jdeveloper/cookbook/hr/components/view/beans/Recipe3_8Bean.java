package com.packt.jdeveloper.cookbook.hr.components.view.beans;

import com.packt.jdeveloper.cookbook.shared.bc.extensions.ExtViewObjectImpl;
import com.packt.jdeveloper.cookbook.shared.view.util.ADFUtils;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.share.logging.ADFLogger;

import oracle.binding.BindingContainer;
import oracle.binding.OperationBinding;

public class Recipe3_8Bean {

    // create an ADFLogger
    private static final ADFLogger LOGGER =
        ADFLogger.createADFLogger(Thread.currentThread().getStackTrace()[1].getClassName());
    
    public Recipe3_8Bean() {
    }
    
    public String rollback() {
                LOGGER.log(ADFLogger.TRACE,
                            "Enter rollback()");
           // get the binding container
           DCBindingContainer bindings =
                             ADFUtils.getDCBindingContainer();
           // prevent View objects from executing after rollback
           bindings.setExecuteOnRollback(false);
           // execute rollback operation
           ADFUtils.findOperation("Rollback").execute();
           return null;
    }

    public BindingContainer getBindings() {
        return BindingContext.getCurrent().getCurrentBindingsEntry();
    }
}
