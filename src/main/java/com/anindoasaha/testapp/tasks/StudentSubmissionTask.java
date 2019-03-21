package com.anindoasaha.testapp.tasks;

import com.anindoasaha.testapp.tasks.util.FileUtils;
import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class StudentSubmissionTask extends AbstractTask {

    private static final String COURSE_NAME = "course_name";
    private static final String PROJECT_NAME = "project_name";
    private static final String TRY_DIR_PATH = "try_dir_path";

    private static final String GIVEN_PROJECT_FILES = "given_project_files";
    private static final String ANSWER_PROJECT_FILES = "answer_project_files";
    private static final String TEST_PROJECT_FILES = "test_project_files";

    public StudentSubmissionTask(String name) {
        super(name);
    }

    @Override
    public Object beforeAction(WorkflowInstance workflowInstance) {
        return null;
    }

    @Override
    public Map<String, String> onAction(WorkflowInstance workflowInstance) {
        System.out.println(this.getClass().getCanonicalName());
        final Map<String, String> instanceVariables = workflowInstance.getInstanceVariables();
        String pathname = FileUtils.createFolders(instanceVariables);
        new File(pathname + "student_name").mkdirs();
        FileUtils.copyFiles(instanceVariables, pathname, ANSWER_PROJECT_FILES);
        // TODO Execute tests on the sample solution
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
