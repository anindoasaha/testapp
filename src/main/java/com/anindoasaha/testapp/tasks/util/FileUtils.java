package com.anindoasaha.testapp.tasks.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileUtils {

    private static final String COURSE_NAME = "course_name";
    private static final String PROJECT_NAME = "project_name";
    private static final String TRY_DIR_PATH = "try_dir_path";

    private static final String GIVEN_PROJECT_FILES = "given_project_files";
    private static final String ANSWER_PROJECT_FILES = "answer_project_files";
    private static final String TEST_PROJECT_FILES = "test_project_files";

    public static String createFolders(Map<String, String> instanceVariables) {
        final String courseName = instanceVariables.get(COURSE_NAME);
        final String projectName = instanceVariables.get(PROJECT_NAME);

        // Go into TRYRC_FILE_ABSOLUTE_PATH and read try_dir_path config
        final String tryDirPath = instanceVariables.get(TRY_DIR_PATH); // TODO Read from initVariables

        // Create directory course_name/project_name inside try_dir_path
        String pathname = tryDirPath + File.separatorChar + courseName + File.separatorChar + projectName;
        return pathname;
    }

    public static void copyFiles(Map<String, String> instanceVariables, String pathname, String fileKey) {
        final String projectFiles = instanceVariables.get(fileKey);
        File filePathDir = new File(pathname + File.separatorChar + fileKey);
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
}
