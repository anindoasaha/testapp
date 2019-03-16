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
import org.junit.Test;

public class TestAppWorkflowTest {

    SimpleWorkflowBuilder workflowBuilder = null;
    Workflow workflow = null;

    @Before
    public void createWorkflow() {
        workflowBuilder = new SimpleWorkflowBuilder("TestAppWorkflow");
        Task createLabTask = new CreateLabTask();
        Task publishLabTask = new PublishLabTask();
        Task studentSubmissionTask = new StudentSubmissionTask();
        Task aggregateResultsTask = new AggregateResultsTask();

        workflowBuilder.addTasks(createLabTask, publishLabTask, studentSubmissionTask, aggregateResultsTask)
                .addPipe(createLabTask, publishLabTask)
                .addPipe(publishLabTask, studentSubmissionTask)
                .addPipe(studentSubmissionTask, aggregateResultsTask);
        workflow = workflowBuilder.build();

        WorkflowService workflowService = new WorkflowServiceImpl();
        workflowService.addWorkflow(workflow);
    }

    @Test
    public void testWorkflowInstanceCreation() {
        Main.main(new String[] {"create", "-i", workflow.getId()});
    }

    @Test
    public void testWorkflowInstanceStart() {
        WorkflowService workflowService = new WorkflowServiceImpl();
        workflowService.addWorkflow(workflow);

        Main.main(new String[] {"create", "-w", workflow.getId()});

        Main.main(new String[] {"start", "-i", workflow.getId()});
    }
}
