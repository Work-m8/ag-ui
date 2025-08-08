package io.workm8.agui;

import io.workm8.spring.AgUiService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgUiConfig {

    @Bean
    public AgUiService agUiService() {
        return new AgUiService();
    }
}
