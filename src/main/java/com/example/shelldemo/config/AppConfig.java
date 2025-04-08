package com.example.shelldemo.config;

import com.example.shelldemo.util.ScriptGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    
    @Bean
    public ScriptGenerator scriptGenerator() {
        return new ScriptGenerator();
    }
} 