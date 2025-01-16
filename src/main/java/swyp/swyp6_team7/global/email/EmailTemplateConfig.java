package swyp.swyp6_team7.global.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
@Configuration
public class EmailTemplateConfig {
    @Bean
    public AbstractConfigurableTemplateResolver thymeleafTemplateResolver() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(true);
        resolver.setCacheTTLMs(3600000L); // 1 hour
        return resolver;
    }
    @Bean
    public TemplateEngine thymeleafTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(thymeleafTemplateResolver());
        return engine;
    }
}