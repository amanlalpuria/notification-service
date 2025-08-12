package com.vedvix.notification.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final Configuration freemarkerConfig;

    public String renderTemplate(String path, Map<String, Object> model) {
        try {
            Template template = freemarkerConfig.getTemplate(path);
            StringWriter writer = new StringWriter();
            template.process(model, writer);
            return writer.toString();
        } catch (IOException | TemplateException e) {
            throw new RuntimeException("Error rendering template: " + path, e);
        }
    }
}
