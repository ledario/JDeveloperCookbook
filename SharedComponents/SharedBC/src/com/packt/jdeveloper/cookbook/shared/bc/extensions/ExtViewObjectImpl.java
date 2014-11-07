package com.packt.jdeveloper.cookbook.shared.bc.extensions;

import oracle.jbo.AttributeDef;
import oracle.jbo.server.ViewAttributeDefImpl;
import oracle.jbo.server.ViewObjectImpl;

public class ExtViewObjectImpl extends ViewObjectImpl {
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
}
