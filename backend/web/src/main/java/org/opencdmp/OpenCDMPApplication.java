package org.opencdmp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "org.opencdmp", 
        "org.opencdmp.depositbase", 
        "gr.cite", 
        "gr.cite.tools", 
        "gr.cite.commons",
        "org.opencdmp.controllers.controllerhandler"
})
@EntityScan({
        "org.opencdmp.data"})
@EnableAsync
public class OpenCDMPApplication extends SpringBootServletInitializer {
    @Bean
    @Primary
    public ObjectMapper primaryObjectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder.modulesToInstall(new JavaTimeModule()).build();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(OpenCDMPApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(OpenCDMPApplication.class, args);
    }
}
