package com.anindoasaha.testapp.tasks;

import com.anindoasaha.testapp.tasks.util.FileUtils;
import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;

import java.io.File;
import java.util.Map;

public class CreateLabTask extends AbstractTask {

    private static final String COURSE_NAME = "course_name";
    private static final String PROJECT_NAME = "project_name";
    private static final String TRY_DIR_PATH = "try_dir_path";

    private static final String GIVEN_PROJECT_FILES = "given_project_files";
    private static final String ANSWER_PROJECT_FILES = "answer_project_files";
    private static final String TEST_PROJECT_FILES = "test_project_files";


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

        String pathname = FileUtils.createFolders(instanceVariables);
        new File(pathname).mkdirs();

        // Get given_project_files from instanceVariables and copy to given_project_files directory
        FileUtils.copyFiles(instanceVariables, pathname, GIVEN_PROJECT_FILES);
        // Get answer_project_files from instanceVariables and copy to answer_project_files directory
        FileUtils.copyFiles(instanceVariables, pathname, ANSWER_PROJECT_FILES);
        // Get test_project_files from instanceVariables and copy to test_project_files directory
        FileUtils.copyFiles(instanceVariables, pathname, TEST_PROJECT_FILES);

        // TODO Execute tests on the sample solution
        DefaultDockerClientConfig.Builder config
                = DefaultDockerClientConfig.createDefaultConfigBuilder();
        DockerClient dockerClient = DockerClientBuilder
                .getInstance(config)
                .build();

        String imageId = dockerClient.buildImageCmd()
                .withDockerfile(new File(instanceVariables.get("dockerfile_path")))
                .withPull(true)
                .exec(new BuildImageResultCallback())
                .awaitImageId();
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
