package com.vektorraum.voicecontrol.service.voicemail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "voicemail")
@Component
@Data
public class VoiceMailConfig {
    private boolean transcriptionEnabled = false;
    private List<String> transcriptionLanguages = List.of("en-US");
    private float transcriptionConfidenceCutOff = 0.0f;
    private long transcriptionTimeout = 600;
}
