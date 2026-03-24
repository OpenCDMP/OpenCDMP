package org.opencdmp;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;

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
    public JsonMapper primaryObjectMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(old ->
                        JsonInclude.Value.construct(
                                JsonInclude.Include.NON_NULL,
                                JsonInclude.Include.NON_NULL
                        )
                )
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(OpenCDMPApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(OpenCDMPApplication.class, args);
    }
}
