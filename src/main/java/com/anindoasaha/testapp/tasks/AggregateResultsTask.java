package com.anindoasaha.testapp.tasks;

import com.anindoasaha.testapp.tasks.util.Utils;
import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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

        File srcFilePathDir = new File(pathname + File.separatorChar + "submissions");
        List<String> testResults = new ArrayList<>();
        for (File listOfFile : srcFilePathDir.listFiles()) {
            if (listOfFile.isDirectory()) {
                File[] files = listOfFile.listFiles((dir, name) -> name.endsWith(".OUT"));
                if (files.length > 0) {
                    testResults.add(listOfFile.getName());
                }
            }
        }
        try (FileWriter fileWriter = new FileWriter(pathname + File.separatorChar + "submitted_" + workflowInstance.getId() + ".json")) {
            new GsonBuilder().setPrettyPrinting().serializeNulls()
                    .create()
                    .toJson(testResults, fileWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
