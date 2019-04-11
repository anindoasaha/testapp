package com.anindoasaha.testapp.tasks;

import com.anindoasaha.testapp.tasks.util.Utils;
import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;

import java.io.File;
import java.util.List;
import java.util.Map;

public class CreateLabTask extends AbstractTask {

    private static final String COURSE_NAME = "course_name";
    private static final String PROJECT_NAME = "project_name";
    private static final String TRY_DIR_PATH = "try_dir_path";

    private static final String GIVEN_PROJECT_FILES = "given_project_files";
    private static final String ANSWER_PROJECT_FILES = "answer_project_files";
    private static final String TEST_PROJECT_FILES = "test_project_files";
    private static final String TEST_DATA_FILES = "test_data_files";


    public CreateLabTask(String name) {
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
        final Map<String, String> taskVariables = getTaskVariables();

        String pathname = Utils.createFolders(instanceVariables, taskVariables);
        instanceVariables.put("working_dir", pathname);
        instanceVariables.put(TEST_PROJECT_FILES, taskVariables.get(TEST_PROJECT_FILES));
        instanceVariables.put(TEST_DATA_FILES, taskVariables.get(TEST_DATA_FILES));



        // Copy over files to given_project_files, answer_project_files and test_project_files directory
        Utils.copyFiles(instanceVariables, taskVariables, pathname, GIVEN_PROJECT_FILES, GIVEN_PROJECT_FILES);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, ANSWER_PROJECT_FILES, ANSWER_PROJECT_FILES);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, TEST_PROJECT_FILES, TEST_PROJECT_FILES);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, TEST_DATA_FILES, TEST_DATA_FILES);


        // Create scratch directory for grader user and copy over everything
        String currentUser = Utils.getCurrentUser();
        System.out.println(currentUser);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, GIVEN_PROJECT_FILES, currentUser);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, TEST_PROJECT_FILES, currentUser);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, TEST_DATA_FILES, currentUser);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, ANSWER_PROJECT_FILES, currentUser);

        File[] files = new File(pathname + File.pathSeparator + currentUser).listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                System.out.println(file.getName());
            }
        }

        List<String> fileList = Utils.getFileList(taskVariables, List.of(GIVEN_PROJECT_FILES, TEST_PROJECT_FILES, ANSWER_PROJECT_FILES));

        Utils.execJavac(pathname + File.separatorChar + currentUser, fileList);

        String[] testDataFiles = taskVariables.get(TEST_DATA_FILES).split(";");

        for (String testDataFile : testDataFiles) {
            Utils.execJava(pathname + File.separatorChar + currentUser,
                    taskVariables.get(TEST_PROJECT_FILES).split("\\.")[0],
                    testDataFile);
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
