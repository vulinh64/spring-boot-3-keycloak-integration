package com.vulinh.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "application-properties")
public record ApplicationProperties(String clientName, List<String> adminPrivilegeUrls) {}
