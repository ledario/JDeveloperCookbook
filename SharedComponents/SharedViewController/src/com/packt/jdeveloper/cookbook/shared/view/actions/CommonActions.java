package com.packt.jdeveloper.cookbook.shared.view.actions;

import com.packt.jdeveloper.cookbook.shared.bc.exceptions.messages.BundleUtils;
import com.packt.jdeveloper.cookbook.shared.view.util.ADFUtils;

import com.packt.jdeveloper.cookbook.shared.view.util.JSFUtils;

import javax.faces.event.ActionEvent;

public class CommonActions {

    public static final class Operations {
        public static final String COMMIT = "Commit";
        public static final String ROLLBACK = "RollBack";
        public static final String INSERT = "CreateInsert";
        public static final String DELETE = "Remove";
    }

    public void commit(ActionEvent actionEvent) {
        if (ADFUtils.hasChanges()) {
            // allow derived beans to handle before commit actions
            onBeforeCommit(actionEvent);
            // allow derived beans to handle commit actions
            onCommit(actionEvent);
            // allow derived beans to handle after commit actions
            onAfterCommit(actionEvent);
        } else {
            // display "No changes to commit" message
            JSFUtils.addFacesInformationMessage(BundleUtils.loadMessage("00002"));
        }
    }

    protected void onBeforeCommit(ActionEvent actionEvent) {
    }


    protected void onCommit(ActionEvent actionEvent) {
        // execute commit
        ADFUtils.execOperation(Operations.COMMIT);
    }

    protected void onAfterCommit(ActionEvent actionEvent) {
        // display "Changes were committed successfully" message
        JSFUtils.addFacesInformationMessage(BundleUtils.loadMessage("00003"));
    }

    public void create(ActionEvent actionEvent) {
        if (ADFUtils.hasChanges()) {
            onCreatePendingChanges(actionEvent);
        } else {
            onContinueCreate(actionEvent);
        }
    }

    protected void onBeforeCreate(ActionEvent actionEvent) {
        // commit before creating a new record
        ADFUtils.execOperation(Operations.COMMIT);
    }

    public void onCreate(ActionEvent actionEvent) {
        ADFUtils.execOperation(Operations.INSERT);
    }

    protected void onAfterCreate(ActionEvent actionEvent) {
    }

    public void onCreatePendingChanges(ActionEvent actionEvent) {
        ADFUtils.showPopup("CreatePendingChanges");
    }

    public void onContinueCreate(ActionEvent actionEvent) {
        onBeforeCreate(actionEvent);
        onCreate(actionEvent);
        onAfterCreate(actionEvent);
    }
}
