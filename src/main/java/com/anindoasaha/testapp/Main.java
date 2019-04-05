package com.anindoasaha.testapp;

import com.anindoasaha.testapp.tasks.AggregateResultsTask;
import com.anindoasaha.testapp.tasks.CreateLabTask;
import com.anindoasaha.testapp.tasks.PublishLabTask;
import com.anindoasaha.testapp.tasks.StudentSubmissionTask;
import com.anindoasaha.workflowengine.prianza.bo.Task;
import com.anindoasaha.workflowengine.prianza.bo.Workflow;
import com.anindoasaha.workflowengine.prianza.bo.impl.simple.SimpleWorkflow;

import java.util.Map;

import static com.anindoasaha.workflowengine.prianza.cli.cmd.Parser.defaultParser;

public class Main {

    public static void main(String[] args) {
        SimpleWorkflow.Builder workflowBuilder = new SimpleWorkflow.Builder("TestAppWorkflow");

        Task createLabTask = new CreateLabTask("create_lab");

        Task publishLabTask = new PublishLabTask("publish_lab");

        Task studentSubmissionTask = new StudentSubmissionTask("student_submission");

        Task aggregateResultsTask = new AggregateResultsTask("aggregate_results");

        Workflow workflow = workflowBuilder.addTasks(createLabTask, publishLabTask, studentSubmissionTask, aggregateResultsTask)
                .addPipe(createLabTask, publishLabTask)
                .addPipe(publishLabTask, studentSubmissionTask)
                .addPipe(studentSubmissionTask, aggregateResultsTask)
                .initVariables(Map.of(
                                    "TRYRC_FILE_ABSOLUTE_PATH", "~/.tryrc.json"
                        )
                )
                .build();
        defaultParser("testapp", args, workflow);
    }

}
