package com.vektorraum.voicecontrol.twilio.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "twilio")
@Component
@Data
public class TwilioConfig {
    private String accountSid;
    private String authToken;
    private String serverBaseUrl = null;
}
