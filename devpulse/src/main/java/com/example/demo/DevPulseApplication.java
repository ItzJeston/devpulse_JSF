package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * DevPulseApplication - Main Spring Boot Application Bootstrap.
 *
 * Annotations:
 * - @SpringBootApplication: Enables auto-configuration, component scanning across com.example.demo.*
 * - @ServletComponentScan: Enables discovery and registration of @WebServlet ("/SessionServlet")
 *
 * Extends:
 * - SpringBootServletInitializer: Supports WAR packaging deployment in embedded and standalone Tomcat
 */
@SpringBootApplication
@ServletComponentScan
public class DevPulseApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(DevPulseApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(DevPulseApplication.class, args);
    }
}
