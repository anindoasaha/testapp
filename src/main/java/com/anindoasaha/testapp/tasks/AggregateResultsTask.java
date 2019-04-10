package com.anindoasaha.testapp.tasks;

import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;

import java.util.Map;

public class AggregateResultsTask extends AbstractTask {
    public AggregateResultsTask(String name) {
        super(name);
    }

    @Override
    public Object beforeAction(WorkflowInstance workflowInstance) {
        return null;
    }

    @Override
    public Map<String, String> onAction(WorkflowInstance workflowInstance) {
        System.out.println(this.getClass().getCanonicalName());

        Map<String, String> instanceVariables = workflowInstance.getInstanceVariables();

        String pathname = instanceVariables.get("working_dir");


        return null;
    }

    @Override
    public Object onSuccess(WorkflowInstance workflowInstance) {
        return null;
    }

    @Override
    public Object onError(WorkflowInstance workflowInstance) {
        return null;
    }
}
