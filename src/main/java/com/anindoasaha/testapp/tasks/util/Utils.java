package com.anindoasaha.testapp.tasks.util;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.BuildImageResultCallback;
import com.google.common.io.CharStreams;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Utils {

    private static final String COURSE_NAME = "course_name";
    private static final String PROJECT_NAME = "project_name";
    private static final String TRY_DIR_PATH = "try_dir_path";

    private static final String GIVEN_PROJECT_FILES = "given_project_files";
    private static final String ANSWER_PROJECT_FILES = "answer_project_files";
    private static final String TEST_PROJECT_FILES = "test_project_files";

    public static String createFolders(Map<String, String> instanceVariables, Map<String, String> taskVariables) {
        final String courseName = instanceVariables.get(COURSE_NAME);
        final String projectName = instanceVariables.get(PROJECT_NAME);

        // Go into TRYRC_FILE_ABSOLUTE_PATH and read try_dir_path config
        final String tryDirPath = instanceVariables.get(TRY_DIR_PATH); // TODO Read from initVariables

        // Create directory course_name/project_name inside try_dir_path
        String pathname = tryDirPath + File.separatorChar + courseName + File.separatorChar + projectName;
        new File(pathname).mkdirs();
        return pathname;
    }

    public static void copyFiles(Map<String, String> instanceVariables, Map<String, String> taskVariables, String pathname, String taskVariableKey, String folderNameKey) {
        final String projectFiles = taskVariables.get(taskVariableKey);
        File filePathDir = new File(pathname + File.separatorChar + folderNameKey);
        filePathDir.mkdirs();

        final String[] projectFilesList = projectFiles.split(";");
        for (String projectFile : projectFilesList) {
            Path src = new File(projectFile).toPath();
            Path destPath = filePathDir.toPath();
            try {
                Files.copy(src, destPath.resolve(src.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getFileList(Map<String, String> taskVariables, List<String> keys) {
        List<String> fileList = new ArrayList<>();
        for(String key : keys) {
            final String projectFiles = taskVariables.get(key);

            final String[] projectFilesList = projectFiles.split(";");
            for (String projectFile : projectFilesList) {
                fileList.add(projectFile);
            }
        }
        return fileList;
    }

    public static String getCurrentUser() {
        final String[] currentUser = {null};
        CommandLine cmdLine = new CommandLine("whoami");
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        executor.setStreamHandler(new ExecuteStreamHandler() {
            @Override
            public void setProcessInputStream(OutputStream os) throws IOException {
            }

            @Override
            public void setProcessErrorStream(InputStream is) throws IOException {

            }

            @Override
            public void setProcessOutputStream(InputStream is) throws IOException {
                try (final Reader reader = new InputStreamReader(is)) {
                    currentUser[0] = CharStreams.toString(reader).trim();
                }
            }

            @Override
            public void start() throws IOException {

            }

            @Override
            public void stop() throws IOException {

            }
        });
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        try {
            int exitValue = executor.execute(cmdLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return currentUser[0];

    }

    public static String execJavac(String workingDir, List<String> fileNames) {
        final int[] exitCode = {0};
        CommandLine cmdLine = new CommandLine("javac");
        for (String fileName : fileNames) {
            cmdLine.addArgument(fileName);
        }
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setStreamHandler(new ExecuteStreamHandler() {
            @Override
            public void setProcessInputStream(OutputStream os) throws IOException {
            }

            @Override
            public void setProcessErrorStream(InputStream is) throws IOException {
                try (final Reader reader = new InputStreamReader(is)) {
                    String output = CharStreams.toString(reader).trim();
                    System.out.println(output);
                }
            }

            @Override
            public void setProcessOutputStream(InputStream is) throws IOException {
                try (final Reader reader = new InputStreamReader(is)) {
                    String output = CharStreams.toString(reader).trim();
                    System.out.println(output);
                }
            }

            @Override
            public void start() throws IOException {

            }

            @Override
            public void stop() throws IOException {

            }
        });
        executor.setWatchdog(watchdog);
        try {
            executor.setWorkingDirectory(new File(workingDir));
            exitCode[0] = executor.execute(cmdLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(exitCode[0]);

    }

    public static String execJava(String workingDir, String mainClassName) {
        final int[] exitCode = {0};
        CommandLine cmdLine = new CommandLine("java");
        cmdLine.addArgument(mainClassName);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setExitValue(0);
        executor.setStreamHandler(new ExecuteStreamHandler() {
            @Override
            public void setProcessInputStream(OutputStream os) throws IOException {
            }

            @Override
            public void setProcessErrorStream(InputStream is) throws IOException {

            }

            @Override
            public void setProcessOutputStream(InputStream is) throws IOException {
                try (final Reader reader = new InputStreamReader(is)) {
                    String output = CharStreams.toString(reader).trim();
                    FileWriter fileWriter = new FileWriter(new File(workingDir + File.separatorChar + mainClassName + ".OUT"));
                    fileWriter.write(output);
                    fileWriter.close();
                    System.out.println(output);
                }
            }

            @Override
            public void start() throws IOException {

            }

            @Override
            public void stop() throws IOException {

            }
        });
        ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
        executor.setWatchdog(watchdog);
        try {
            executor.setWorkingDirectory(new File(workingDir));
            exitCode[0] = executor.execute(cmdLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return String.valueOf(exitCode[0]);

    }

    public static void dockerizeAndExecute(Map<String, String> instanceVariables) {
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
    }
}
