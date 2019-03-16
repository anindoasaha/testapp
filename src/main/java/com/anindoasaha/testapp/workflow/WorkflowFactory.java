package com.anindoasaha.testapp.workflow;


import com.anindoasaha.testapp.tasks.AggregateResultsTask;
import com.anindoasaha.testapp.tasks.CreateLabTask;
import com.anindoasaha.testapp.tasks.PublishLabTask;
import com.anindoasaha.testapp.tasks.StudentSubmissionTask;
import com.anindoasaha.workflowengine.prianza.api.WorkflowService;
import com.anindoasaha.workflowengine.prianza.api.impl.WorkflowServiceImpl;
import com.anindoasaha.workflowengine.prianza.bo.Task;
import com.anindoasaha.workflowengine.prianza.bo.Workflow;
import com.anindoasaha.workflowengine.prianza.bo.impl.SimpleWorkflowBuilder;

public class WorkflowFactory {

    SimpleWorkflowBuilder workflowBuilder = null;
    Workflow workflow = null;

    public void init() {
        createWorkflow();
    }

    public void createWorkflow() {
        workflowBuilder = new SimpleWorkflowBuilder("TestAppWorkflow");
        Task createLabTask = new CreateLabTask("create_lab");
        Task publishLabTask = new PublishLabTask("publish_lab");
        Task studentSubmissionTask = new StudentSubmissionTask("student_submission");
        Task aggregateResultsTask = new AggregateResultsTask("aggregate_results");

        workflowBuilder.addTasks(createLabTask, publishLabTask, studentSubmissionTask, aggregateResultsTask)
                .addPipe(createLabTask, publishLabTask)
                .addPipe(publishLabTask, studentSubmissionTask)
                .addPipe(studentSubmissionTask, aggregateResultsTask);
        workflow = workflowBuilder.build();

        WorkflowService workflowService = new WorkflowServiceImpl();
        workflowService.addWorkflow(workflow);
    }
}
