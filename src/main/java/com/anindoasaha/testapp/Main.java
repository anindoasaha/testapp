package com.anindoasaha.testapp;

import com.anindoasaha.testapp.tasks.AggregateResultsTask;
import com.anindoasaha.testapp.tasks.CreateLabTask;
import com.anindoasaha.testapp.tasks.PublishLabTask;
import com.anindoasaha.testapp.tasks.StudentSubmissionTask;
import com.anindoasaha.workflowengine.prianza.api.WorkflowService;
import com.anindoasaha.workflowengine.prianza.api.impl.WorkflowServiceImpl;
import com.anindoasaha.workflowengine.prianza.bo.Task;
import com.anindoasaha.workflowengine.prianza.bo.TaskExecutionInfo;
import com.anindoasaha.workflowengine.prianza.bo.Workflow;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;
import com.anindoasaha.workflowengine.prianza.bo.impl.simple.SimpleWorkflow;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class Main {

    private  static Map<String, Workflow> workflows = new TreeMap<>();

    public static void main(String[] args) {

        /*Map<InstanceTuple, String> instanceTupleStringMap = new HashMap<>();
        InstanceTuple instanceTuple = new InstanceTuple();
        instanceTuple.course = "csci665";
        instanceTuple.assignment = "hw1-1";
        instanceTuple.language = "java";


        Type type = new TypeToken<Map<InstanceTuple, String>>() {
        }.getType();
        instanceTupleStringMap.put(instanceTuple, "12345678oihgfdsfgfhg");
        System.out.println(new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create()
                .toJson(
                        new Gson().fromJson(new GsonBuilder()
                .enableComplexMapKeySerialization()
                .create()
                .toJson(instanceTupleStringMap, type), type), type));*/

        SimpleWorkflow.Builder workflowBuilder = new SimpleWorkflow.Builder("JavaAppWorkflow");

        Task createLabTask = new CreateLabTask("create_lab");
        Task publishLabTask = new PublishLabTask("publish_lab");
        Task studentSubmissionTask = new StudentSubmissionTask("student_submission");
        Task aggregateResultsTask = new AggregateResultsTask("aggregate_results");

        Workflow javaWorkflow = workflowBuilder
                .addTask(createLabTask)
                .addTask(publishLabTask)
                .addTask(studentSubmissionTask, TaskExecutionInfo.executeMultiple())
                .addTask(aggregateResultsTask)
                .addPipe(createLabTask, publishLabTask)
                .addPipe(publishLabTask, studentSubmissionTask)
                .addPipe(studentSubmissionTask, aggregateResultsTask)
                .initVariables(Map.of(
                                    "TRYRC_FILE_ABSOLUTE_PATH", "~/.tryrc.json"
                        )
                )
                .build();
        workflows.put("java", javaWorkflow);

        Namespace namespace = tryParser("try", args, workflows);
        System.out.println(namespace);
        //delegateToEngine(namespace);
    }

    private static void delegateToEngine(Namespace namespace) {
        if (namespace != null) {

            WorkflowService workflowService = new WorkflowServiceImpl();
            String workflowInstanceId = null;
            WorkflowInstance instance = null;
            List<String> vars = null;
            switch (namespace.getString("subparser")) {
                case "server":
                    // TODO: Future work: start gRPC server which delegates all calls to WorkflowServiceImpl
                    System.out.println("Not implemented");
                    break;

                case "load":
                    Boolean all = namespace.getBoolean("all");
                    Boolean force = namespace.getBoolean("force");

                    String lang = namespace.getString("lang");

                    Map<String, String> langWorkflowId = new TreeMap<>();

                    if(all) {
                        if (force) {
                            for (Map.Entry<String, Workflow> workflowEntry : workflows.entrySet()) {
                                workflowService.addWorkflow(workflowEntry.getValue());
                                langWorkflowId.put(workflowEntry.getKey(), workflowEntry.getValue().getId());
                            }
                            saveIndex(langWorkflowId); // Overwrite everything
                        } else {
                            Map<String, String> index = loadIndex(); // Add what doesnt exist
                            for (Map.Entry<String, Workflow> workflowEntry : workflows.entrySet()) {
                                workflowService.addWorkflow(workflowEntry.getValue());
                                index.putIfAbsent(workflowEntry.getKey(), workflowEntry.getValue().getId());
                            }
                            saveIndex(index);
                        }
                    } else {
                        if (force) { // Overwrite
                            Workflow workflow = workflows.get(lang);
                            workflowService.addWorkflow(workflow);
                            Map<String, String> index = loadIndex();
                            index.put(lang, workflow.getId());
                            saveIndex(index);
                        } else { // Add if absent
                            Workflow workflow = workflows.get(lang);
                            workflowService.addWorkflow(workflow);
                            Map<String, String> index = loadIndex();
                            index.putIfAbsent(lang, workflow.getId());
                            saveIndex(index);
                        }
                    }
                    break;
                case "list":
                    Boolean verbose = namespace.getBoolean("verbose");
                    Map<String, String> index = loadIndex();

                    if(verbose) {
                        System.out.println("Language\t\t Workflow ID");
                        for (Map.Entry<String, String> workflowEntry : index.entrySet()) {
                            System.out.println(workflowEntry.getKey() + "\t\t" + workflowEntry.getValue());
                        }
                    } else {
                        System.out.println("Language\t\t");
                        for (Map.Entry<String, String> workflowEntry : index.entrySet()) {
                            System.out.println(workflowEntry.getKey());
                        }
                    }
                    break;
                case "create":
                    String course = namespace.getString("course");
                    String assignment = namespace.getString("assignment");
                    String language = namespace.getString("language");
                    Map<String, String> parameters = new HashMap<>();
                    parameters.put("course_name", course);
                    parameters.put("project_name", assignment);
                    parameters.put("try_dir_path", "~/.try");
                    String workflowId = loadIndex().get(language);
                    workflowInstanceId = workflowService.createWorkflowInstance(workflowId, parameters);
                    workflowService.startWorkflowInstance(workflowInstanceId);

                    Map<InstanceTuple, String> instanceTupleMap = loadInstanceIndex();
                    instanceTupleMap.put(new InstanceTuple(course, assignment, language), workflowInstanceId);
                    saveInstanceIndex(instanceTupleMap);

                    System.out.println("Instance created and started with ID: " + workflowInstanceId);
                    break;
                case "lab":
                    course = namespace.getString("course");
                    assignment = namespace.getString("assignment");
                    language = namespace.getString("language");
                    Map<InstanceTuple, String> map = loadInstanceIndex();
                    workflowInstanceId = map.get(new InstanceTuple(course, assignment, language));
                    workflowService.executeWorkflowInstance(workflowInstanceId, convertToMap(vars));
                    break;
                case "publish":
                    course = namespace.getString("course");
                    assignment = namespace.getString("assignment");
                    language = namespace.getString("language");
                    map = loadInstanceIndex();
                    workflowInstanceId = map.get(new InstanceTuple(course, assignment, language));
                    workflowService.executeWorkflowInstance(workflowInstanceId, convertToMap(vars));
                    break;
                case "submit":
                    course = namespace.getString("course");
                    assignment = namespace.getString("assignment");
                    language = namespace.getString("language");
                    map = loadInstanceIndex();
                    workflowInstanceId = map.get(new InstanceTuple(course, assignment, language));
                    // Always picks the first available task, hence works for this linear workflow
                    workflowService.executeWorkflowInstance(workflowInstanceId, convertToMap(vars));
                    break;
                case "close":
                    course = namespace.getString("course");
                    assignment = namespace.getString("assignment");
                    language = namespace.getString("language");
                    map = loadInstanceIndex();
                    workflowInstanceId = map.get(new InstanceTuple(course, assignment, language));
                    workflowService.executeWorkflowInstance(workflowInstanceId, convertToMap(vars));
                    break;
                case "results":
                    course = namespace.getString("course");
                    assignment = namespace.getString("assignment");
                    language = namespace.getString("language");
                    map = loadInstanceIndex();
                    workflowInstanceId = map.get(new InstanceTuple(course, assignment, language));
                    workflowService.executeWorkflowInstance(workflowInstanceId, convertToMap(vars));
                    break;
                case "run":
                    workflowInstanceId = namespace.getString("instance_id");
                    String taskId = namespace.getString("task_id");
                    vars = namespace.getList("vars");
                    instance = workflowService.getWorkflowInstanceByWorkflowInstanceId(workflowInstanceId);

                    // Read task variables
                    workflowService.executeWorkflowInstance(workflowInstanceId, taskId, convertToMap(vars));
                    break;
                case "proceed":
                    workflowInstanceId = namespace.getString("instance_id");
                    String proceedTaskId = namespace.getString("task_id");
                    vars = namespace.getList("vars");
                    instance = workflowService.getWorkflowInstanceByWorkflowInstanceId(workflowInstanceId);

                    // Read task variables
                    workflowService.proceedWorkflowInstance(workflowInstanceId, proceedTaskId, convertToMap(vars));

                    break;
            }
        }
    }

    private static void saveIndex(Map<String, String> langWorkflowId) {
        try (FileWriter fileWriter = new FileWriter("~/.try/index.json")) {
            new GsonBuilder()
                    .setPrettyPrinting()
                    .serializeNulls()
                    .create()
                    .toJson(langWorkflowId, fileWriter);
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void saveInstanceIndex(Map<InstanceTuple, String> instanceTuples) {
        try (FileWriter fileWriter = new FileWriter("~/.try/instance_index.json")) {
            Type type = new TypeToken<Map<InstanceTuple, String>>() {
            }.getType();
            new GsonBuilder()
                    .setPrettyPrinting()
                    .enableComplexMapKeySerialization()
                    .serializeNulls()
                    .create()
                    .toJson(instanceTuples, type, fileWriter);
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
        }
    }

    private static class InstanceTuple {
        String course;
        String assignment;
        String language;

        public InstanceTuple(String course, String assignment, String language) {
            this.course = course;
            this.assignment = assignment;
            this.language = language;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InstanceTuple that = (InstanceTuple) o;
            return Objects.equals(course, that.course) &&
                    Objects.equals(assignment, that.assignment) &&
                    Objects.equals(language, that.language);
        }

        @Override
        public int hashCode() {
            return Objects.hash(course, assignment, language);
        }
    }

    private static Map<String, String> loadIndex() {
        Map<String, String> index = null;

        FileReader fileReader = null;
        try {
            File file = new File("~/.try", "index.json");
            if (file.createNewFile()) {
                index = new HashMap<>();
            } else {
                fileReader = new FileReader(file);
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                index = new GsonBuilder()
                        .enableComplexMapKeySerialization()
                        .create()
                        .fromJson(fileReader, type);
                if (index == null) {
                    index = new HashMap<>();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return index;
    }

    private static Map<InstanceTuple, String> loadInstanceIndex() {
        Map<InstanceTuple, String> index = null;

        FileReader fileReader = null;
        try {
            File file = new File("~/.try", "instance_index.json");
            if (file.createNewFile()) {
                index = new HashMap<>();
            } else {
                fileReader = new FileReader(file);
                Type type = new TypeToken<Map<InstanceTuple, String>>() {
                }.getType();
                index = new GsonBuilder()
                        .create()
                        .fromJson(fileReader, type);
                if (index == null) {
                    index = new HashMap<>();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return index;
    }

    public static Namespace tryParser(String program, String args[], Map<String, Workflow> workflows) {
        ArgumentParser argumentParser = ArgumentParsers.newFor(program).build();

        Subparsers subparsers = argumentParser.addSubparsers()
                .dest("subparser")
                .title("subcommands")
                .description("valid subcommands")
                .metavar("COMMAND");

        // Run workflow engine as a server
        Subparser subparserServer = subparsers.addParser("server").defaultHelp(true).help("-h");

        // Load all inbuilt workflows or load/reload language-specific workflow
        Subparser subparserLoad = subparsers.addParser("load")
                                            .defaultHelp(true)
                                            .help("-h")
                                            .setDefault("lang", "java");
        subparserLoad.addArgument("-a", "--all").action(Arguments.storeTrue());
        subparserLoad.addArgument("-f", "--force").action(Arguments.storeTrue());
        subparserLoad.addArgument("-l", "--lang").choices(workflows.keySet());

        // List all loaded workflows
        Subparser subparserList = subparsers.addParser("list").defaultHelp(true).help("-h");
        subparserList.addArgument("-v", "--verbose").action(Arguments.storeTrue());

        // Create a workflow instance for an assignment
        Subparser subparserCreate = subparsers.addParser("create").defaultHelp(true).help("-h");
        subparserCreate.addArgument("course").required(true);
        subparserCreate.addArgument("assignment").required(true);
        subparserCreate.addArgument("language").required(true);


        // Create lab task
        Subparser subparserCreateTasks = subparsers.addParser("lab").help("-h");
        subparserCreateTasks.addArgument("course").required(true);
        subparserCreateTasks.addArgument("assignment").required(true);
        subparserCreateTasks.addArgument("language").required(true);
        subparserCreateTasks.addArgument("-g", "--given").required(true).nargs("*");
        subparserCreateTasks.addArgument("-t", "--test").required(true).nargs("*");
        subparserCreateTasks.addArgument("-a", "--answer").required(true).nargs("*");
        subparserCreateTasks.addArgument("-d", "--test-data").required(true).nargs("*");

        // Publish lab all tasks
        Subparser subparserTasks = subparsers.addParser("publish").help("-h");
        subparserTasks.addArgument("course").required(true);
        subparserTasks.addArgument("assignment").required(true);
        subparserTasks.addArgument("language").required(true);
        subparserTasks.addArgument("-m", "--md-file")
                                .required(true)
                                .help("Markdown file");
        // TODO Configurable publish directory

        // Submit task
        Subparser subparserRun = subparsers.addParser("submit").help("-h");
        subparserRun.addArgument("course").required(true);
        subparserRun.addArgument("assignment").required(true);
        subparserRun.addArgument("language").required(true);
        subparserRun.addArgument("-a", "--answer").required(true).nargs("*");

        // Proceed
        Subparser subparserProceed = subparsers.addParser("close").help("-h");
        subparserProceed.addArgument("course").required(true);
        subparserProceed.addArgument("assignment").required(true);
        subparserProceed.addArgument("language").required(true);

        // Results
        Subparser subparserResults = subparsers.addParser("results").help("-h");
        subparserResults.addArgument("course").required(true);
        subparserResults.addArgument("assignment").required(true);
        subparserResults.addArgument("language").required(true);


        Namespace namespace = null;
        try {
            namespace = argumentParser.parseArgs(args);
        } catch (ArgumentParserException e) {
            argumentParser.handleError(e);
        }

        return namespace;
    }


    private static Map<String, String> convertToMap(List<String> vars) {
        if (vars == null || vars.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> parameters = new HashMap<>();
        for (String var : vars) {
            String[] split = var.split("=");
            parameters.put(split[0], split[1]);
        }
        return parameters;
    }

}
