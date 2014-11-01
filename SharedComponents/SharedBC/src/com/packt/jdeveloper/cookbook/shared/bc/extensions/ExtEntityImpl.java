package com.packt.jdeveloper.cookbook.shared.bc.extensions;

import com.packt.jdeveloper.cookbook.shared.bc.exceptions.messages.BundleUtils;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.AttributeDef;
import oracle.jbo.AttributeList;
import oracle.jbo.server.EntityImpl;
import oracle.jbo.server.SequenceImpl;
import oracle.jbo.server.TransactionEvent;

public class ExtEntityImpl extends EntityImpl {
    static final String CREATESEQ_PROPERTY = "CreateSequence";
    static final String COMMITSEQ_PROPERTY = "CommitSequence";
    
    static final ADFLogger LOGGER = ADFLogger.createADFLogger(ExtEntityImpl.class);
    
    @Override
    protected void create(AttributeList attributeList) {
        super.create(attributeList);

        // iterate all entity attributes
        for (AttributeDef atrbDef : this.getEntityDef().getAttributeDefs()) {
            // construct the custom property name from the entity name and attribute 
            String propertyName = CREATESEQ_PROPERTY + 
                                  getEntityDef().getName() + atrbDef.getName();
            // check for a custom property called CREATESEQ_PROPERTY 
            String sequenceName = (String)atrbDef.getProperty(propertyName); 
            if (sequenceName != null) {
                // create the sequence based on the custom property sequence name 
                SequenceImpl sequence = new SequenceImpl(sequenceName, this.getDBTransaction()); 
                // populate the attribute with the next sequence number 
                this.populateAttributeAsChanged(atrbDef.getIndex(), sequence.getSequenceNumber());
                LOGGER.severe("Next create sequence number: " + sequence.getSequenceNumber());
                //this.populateAttributeAsChanged(atrbDef.getIndex(), new oracle.jbo.domain.Number(400));
            } 
        }
    }

    @Override
    protected void doDML(int operation, TransactionEvent transactionEvent) {
        // check for insert operation
        LOGGER.severe("Inside doDML()");
        if (DML_INSERT == operation) { 
            LOGGER.severe("Inside DML_INSERT");
            // iterate all entity attributes 
            for (AttributeDef atrbDef : this.getEntityDef().getAttributeDefs()) {
                // construct the custom property name from the entity name and attribute
                String propertyName = COMMITSEQ_PROPERTY + 
                                      getEntityDef().getName() + atrbDef.getName();
                // check for a custom property called COMMITSEQ_PROPERTY 
                String sequenceName = (String)atrbDef.getProperty(propertyName); 
                if (sequenceName != null) {
                    // create the sequence based on the custom property sequence name 
                    SequenceImpl sequence = new SequenceImpl(sequenceName, this.getDBTransaction()); 
                    // populate the attribute with the next sequence number 
                    this.populateAttributeAsChanged(atrbDef.getIndex(), sequence.getSequenceNumber());
                    LOGGER.severe("Next insert sequence number: " + sequence.getSequenceNumber());
                } 
            }
        }
        super.doDML(operation, transactionEvent);
        LOGGER.severe("End of doDML()");
    }
    
    /**
    * Check if attribute’s value differs from its posted value * @param attrIdx the attribute index
    * @return
    */
    public boolean isAttrValueChanged(int attrIdx) {
        // get the attribute’s posted value
        Object postedValue = getPostedAttribute(attrIdx);
        // get the attribute’s current value
        Object newValue = getAttributeInternal(attrIdx);
        // return true is attribute value differs from its posted value 
        return isAttributeChanged(attrIdx) && 
               ((postedValue == null && newValue != null) || 
                (postedValue != null && newValue == null) || 
                (postedValue != null && newValue != null && 
                 !newValue.equals(postedValue))); 
    }
    
    public String getBundleParameter(String parameterKey) { 
        // use BundleUtils to load the parameter
        return BundleUtils.loadParameter(parameterKey);
    }
}
