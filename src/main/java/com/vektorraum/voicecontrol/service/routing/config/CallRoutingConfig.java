package com.vektorraum.voicecontrol.service.routing.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "callrouting")
@Component
@Data
public class CallRoutingConfig {
    private String defaultNumber;
}
