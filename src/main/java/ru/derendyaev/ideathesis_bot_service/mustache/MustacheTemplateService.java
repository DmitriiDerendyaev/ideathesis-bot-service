package ru.derendyaev.ideathesis_bot_service.mustache;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Map;

@Service
public class MustacheTemplateService {
    private final MustacheFactory mustacheFactory;

    public MustacheTemplateService() {
        this.mustacheFactory = new DefaultMustacheFactory("templates");
    }

    public String render(String templateName, Object context) {
        try {
            Mustache mustache = mustacheFactory.compile(templateName);
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при рендеринге шаблона " + templateName, e);
        }
    }
}