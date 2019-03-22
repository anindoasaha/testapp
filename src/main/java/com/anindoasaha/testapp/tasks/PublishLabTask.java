package com.anindoasaha.testapp.tasks;

import com.anindoasaha.workflowengine.prianza.bo.AbstractTask;
import com.anindoasaha.workflowengine.prianza.bo.WorkflowInstance;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.options.MutableDataSet;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class PublishLabTask extends AbstractTask {

    public PublishLabTask(String name) {
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
        String projectDescriptionFilePath = getTaskVariables().get("project_description_file_path");
        convertToHtml(instanceVariables.get("working_dir"), projectDescriptionFilePath);
        // TODO Move to web server
        return null;
    }

    public static void convertToHtml(String path, String fileName) {
        MutableDataSet options = new MutableDataSet();

        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        Node document = null;
        try {
            File file = new File(fileName);
            document = parser.parseReader(new FileReader(file));
            renderer.render(document, new FileWriter(path + File.separatorChar + file.getName() + ".html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
