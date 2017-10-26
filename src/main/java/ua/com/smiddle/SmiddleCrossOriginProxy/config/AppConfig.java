package ua.com.smiddle.SmiddleCrossOriginProxy.config;

import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
@ComponentScan("ua.com.smiddle")
@PropertySource("classpath:application.properties")
public class AppConfig extends WebMvcConfigurerAdapter {
    private final Environment environment;


    //Constructor
    public AppConfig(Environment environment) {
        this.environment = environment;
    }


    //Methods
    @Bean(name = "multipartResolver")
    @Description("Обертка для Apache multipart httpReq")
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSizePerFile(100000000);
        resolver.setMaxInMemorySize(100000000);
        resolver.setDefaultEncoding("utf8");
        return resolver;
    }

}
