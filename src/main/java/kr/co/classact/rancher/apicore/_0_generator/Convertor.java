package kr.co.classact.rancher.apicore._0_generator;

import kr.co.classact.rancher.apicore._0_generator.utility.FileUtility;
import lombok.extern.log4j.Log4j2;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Log4j2
public class Convertor {

    private final FileUtility fileUtility = FileUtility.ins();

    public void execute(
            Map<String, Object> dataMap
            , String vm
            , String out
            , boolean removeOption) throws Exception {
        log.debug("start convert.");
        log.debug("template file path: " + vm);
        log.debug("output path: " + out);
        log.debug("support data: " + dataMap);

        checkFile(vm, out, removeOption);

        // create and initialize velocity engine.
        VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.init();

        // create velocity context and put data on velocity context.
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("model", dataMap);

        // load velocity template file.
        Template template = velocityEngine.getTemplate(vm);

        // merge template file and write on writer.
        StringWriter stringWriter = new StringWriter();
        template.merge(velocityContext, stringWriter);

        // write writer content on out file.
        Path resultPath = fileUtility.writeContent(out, stringWriter.toString());
        log.debug("convertor write path: " + resultPath);
    }

    private void checkFile(String ftl, String out, boolean removeOption) throws Exception {
        fileUtility.checkFile(ftl);

        File outFile = new File(out);
        try {
            if (removeOption && outFile.exists() && !outFile.delete()) {
                throw new Exception("Failed to delete output file." + "\n" + "path: " + out);
            }
            fileUtility.checkFile(out);
        } catch (FileNotFoundException e) {
            if (!outFile.createNewFile()) {
                throw new Exception("Failed to create output file." + "\n" + "path: " + out);
            }
        }
    }


}
