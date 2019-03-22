package com.anindoasaha.testapp;

import com.anindoasaha.testapp.tasks.AggregateResultsTask;
import com.anindoasaha.testapp.tasks.CreateLabTask;
import com.anindoasaha.testapp.tasks.PublishLabTask;
import com.anindoasaha.testapp.tasks.StudentSubmissionTask;
import com.anindoasaha.workflowengine.prianza.api.WorkflowService;
import com.anindoasaha.workflowengine.prianza.api.impl.WorkflowServiceImpl;
import com.anindoasaha.workflowengine.prianza.bo.Task;
import com.anindoasaha.workflowengine.prianza.bo.Workflow;
import com.anindoasaha.workflowengine.prianza.bo.impl.SimpleWorkflowBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestAppWorkflowTest {

    SimpleWorkflowBuilder workflowBuilder = null;
    Workflow workflow = null;


    @Test
    public void testWorkflowLoad() {
        Main.main(new String[] {"load"});
    }

    @Test
    @Ignore
    public void testWorkflowInstanceStart() {
        WorkflowService workflowService = new WorkflowServiceImpl();
        workflowService.addWorkflow(workflow);

        Main.main(new String[] {"create", "-w", workflow.getId()});

        Main.main(new String[] {"start", "-i", workflow.getId()});
    }
}
