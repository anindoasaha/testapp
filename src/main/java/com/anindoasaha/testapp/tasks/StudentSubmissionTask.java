package com.anindoasaha.testapp.tasks;

import com.anindoasaha.testapp.tasks.util.Utils;
import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StudentSubmissionTask extends AbstractTask {

    private static final String COURSE_NAME = "course_name";
    private static final String PROJECT_NAME = "project_name";
    private static final String TRY_DIR_PATH = "try_dir_path";

    private static final String GIVEN_PROJECT_FILES = "given_project_files";
    private static final String ANSWER_PROJECT_FILES = "answer_project_files";
    private static final String TEST_PROJECT_FILES = "test_project_files";
    private static final String TEST_DATA_FILES = "test_data_files";

    private static final String SUBMISSIONS_DIR = "submissions";

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
        final Map<String, String> taskVariables = getTaskVariables();

        String pathname = instanceVariables.get("working_dir");
        // Create scratch directory for user and copy over everything
        String currentUser = Utils.getCurrentUser();
        currentUser = "studentAccount_" + UUID.randomUUID().toString();
        System.out.println(currentUser);
        Utils.copyFiles(instanceVariables, taskVariables, pathname, ANSWER_PROJECT_FILES,
                SUBMISSIONS_DIR + File.separatorChar + currentUser);

        Utils.copyFilesFromFolder(pathname, GIVEN_PROJECT_FILES, currentUser);
        Utils.copyFilesFromFolder(pathname, TEST_PROJECT_FILES, currentUser);
        Utils.copyFilesFromFolder(pathname, TEST_DATA_FILES, currentUser);

        File[] files = new File(pathname + File.pathSeparator + currentUser).listFiles(File::isFile);
        if (files != null) {
            for (File file : files) {
                System.out.println(file.getName());
            }
        }

        List<String> fileList = Utils.getFileList(pathname, SUBMISSIONS_DIR + File.separatorChar + currentUser);

        Utils.execJavac(pathname + File.separatorChar + SUBMISSIONS_DIR + File.separatorChar + currentUser, fileList);

        String[] testDataFiles = instanceVariables.get(TEST_DATA_FILES).split(File.pathSeparator);

        for (String testDataFile : testDataFiles) {

            String mainClassName = instanceVariables.get(TEST_PROJECT_FILES).split("\\.")[0];
            Utils.execJava(pathname + File.separatorChar + SUBMISSIONS_DIR + File.separatorChar + currentUser,
                    mainClassName, testDataFile);
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
