package org.oscwii.repositorymanager.treatments.impl;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.oscwii.repositorymanager.model.app.InstalledApp;
import org.oscwii.repositorymanager.model.app.OSCMeta;
import org.oscwii.repositorymanager.model.app.OSCMeta.Treatment;
import org.oscwii.repositorymanager.treatments.BaseTreatmentRunnable;
import org.oscwii.repositorymanager.treatments.TreatmentRegistry;
import org.oscwii.repositorymanager.utils.QuietException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class MetaTreatment
{
    @Autowired
    private MetaTreatment(TreatmentRegistry registry)
    {
        registry.registerTreatment(new Init());
        registry.registerTreatment(new RemoveComments());
        registry.registerTreatment(new RemoveDeclaration());
        registry.registerTreatment(new Set());
    }

    public class Init extends BaseTreatmentRunnable
    {
        private Init()
        {
            super("meta.init");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            OSCMeta meta = app.getMeta();
            Document doc = DocumentHelper.createDocument();
            Path appFiles = workingDir.resolve("apps").resolve(app.getSlug());

            Element root = doc.addElement("app");
            root.addAttribute("version", "1");

            // Default values
            root.addElement("name").addText(meta.name());
            root.addElement("coder").addText(meta.author());
            root.addElement("version").addText(meta.version());
            root.addElement("short_description").addText("No description provided.");

            writeDocument(doc, appFiles.resolve("meta.xml"));
            logger.info("  - Created new meta.xml file");
        }
    }

    public static class RemoveComments extends BaseTreatmentRunnable
    {
        private RemoveComments()
        {
            super("meta.remove_comments");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            Path appFiles = workingDir.resolve("apps").resolve(app.getSlug());
            Path meta = appFiles.resolve("meta.xml");
            String xml = Files.readString(meta);

            try(BufferedWriter bw = Files.newBufferedWriter(meta))
            {
                xml = xml.replaceAll("<!--(.|\\s)*?-->", "");
                bw.write(xml);
            }

            logger.info("  - Removed comments from meta.xml");
        }
    }

    public static class RemoveDeclaration extends BaseTreatmentRunnable
    {
        private RemoveDeclaration()
        {
            super("meta.remove_declaration");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            Path appFiles = workingDir.resolve("apps").resolve(app.getSlug());
            Path meta = appFiles.resolve("meta.xml");
            String xml = Files.readString(meta);

            try(BufferedWriter bw = Files.newBufferedWriter(meta))
            {
                xml = xml.split("\n", 2)[1];

                bw.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
                bw.write(xml);
            }

            logger.info("- Removed potentially broken unicode declaration and added correct " +
                    "XML declaration to meta.xml");
        }
    }

    public class Set extends BaseTreatmentRunnable
    {
        private Set()
        {
            super("meta.set");
        }

        @Override
        public void run(InstalledApp app, Path workingDir, Treatment treatment) throws IOException
        {
            String[] arguments = treatment.arguments();
            Path appFiles = workingDir.resolve("apps").resolve(app.getSlug());
            Assert.isTrue(arguments.length >= 2, "Meta set treatment requires two arguments!");

            String key = arguments[0];
            String value = arguments[1];

            try(InputStream is = Files.newInputStream(appFiles.resolve("meta.xml")))
            {
                SAXReader reader = SAXReader.createDefault();
                Document doc = reader.read(is);

                Element root = doc.elementByID("app");
                Element element = root.element(key);
                if(element == null)
                    root.addElement(key).addText(value);
                else
                    element.setText(value);

                writeDocument(doc, appFiles.resolve("meta.xml"));
            }
            catch(DocumentException e)
            {
                throw new QuietException("Failed to parse XML document:", e);
            }

            logger.info("  - Set {} to {} in meta.xml", key, value);
        }
    }

    private void writeDocument(Document doc, Path meta) throws IOException
    {
        OutputFormat formatter = createFormatter();
        try(OutputStream os = Files.newOutputStream(meta))
        {
            XMLWriter xmlWriter = new XMLWriter(os, formatter);
            xmlWriter.write(doc);
        }
    }

    private OutputFormat createFormatter()
    {
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndentSize(4);
        format.setSuppressDeclaration(false);
        format.setNewLineAfterDeclaration(false);
        return format;
    }
}
