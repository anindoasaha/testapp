package com.anindoasaha.testapp;

import com.anindoasaha.testapp.tasks.AggregateResultsTask;
import com.anindoasaha.testapp.tasks.CreateLabTask;
import com.anindoasaha.testapp.tasks.PublishLabTask;
import com.anindoasaha.testapp.tasks.StudentSubmissionTask;
import com.anindoasaha.workflowengine.prianza.api.SimpleWorkflowBuilder;
import com.anindoasaha.workflowengine.prianza.bo.Task;
import com.anindoasaha.workflowengine.prianza.bo.Workflow;

import static com.anindoasaha.workflowengine.prianza.cli.Main.*;

public class Main {

    public static void main(String[] args) {
        SimpleWorkflowBuilder workflowBuilder = new SimpleWorkflowBuilder("TestAppWorkflow");
        Task createLabTask = new CreateLabTask();
        Task publishLabTask = new PublishLabTask();
        Task studentSubmissionTask = new StudentSubmissionTask();
        Task aggregateResultsTask = new AggregateResultsTask();

        Workflow workflow = workflowBuilder.addTasks(createLabTask, publishLabTask, studentSubmissionTask, aggregateResultsTask)
                .addPipe(createLabTask, publishLabTask)
                .addPipe(publishLabTask, studentSubmissionTask)
                .addPipe(studentSubmissionTask, aggregateResultsTask)
                .build();
        defaultParser("testapp", args, workflow);
    }

}
