package com.hionstudios.mypersonalinvite.Mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.context.Context;


import com.hionstudios.MapResponse;

@Configuration
public class HionTemplateConfig {
    @Bean
    SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(templateResolver());
        return templateEngine;
    }

    private ITemplateResolver templateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setTemplateMode("HTML");
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setOrder(1);
        resolver.setCacheable(true);
        return resolver;
    }

    public static String toString(String template, Context context) {
        return new HionTemplateConfig().templateEngine().process(template, context);
    }

    public static String toString(String template, MapResponse variables) {
        Context context = new Context();
        context.setVariables(variables);
        return new HionTemplateConfig().templateEngine().process(template, context);
    }
}