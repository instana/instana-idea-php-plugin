package com.instana.idea.php.codeInsight.template;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.impl.TemplateImpl;

public class LiveTemplateFactory {
    public static Template createWrapInPHPSpanTemplate() {
        StringBuilder sb = new StringBuilder();

        sb
                .append("$tracer = new \\Instana\\Tracer();\n")
                .append("$span = $tracer->createSpan('$SPAN_TYPE$');\n")
                .append("$STATEMENT$\n")
                .append("$span->stop();$END$\n")
        ;

        TemplateImpl template = new TemplateImpl("instana:trace", sb.toString(), "PHP");
        template.setDescription("Wrap statement in SDK span.");
        template.setToReformat(true);

        return template;
    }
}
