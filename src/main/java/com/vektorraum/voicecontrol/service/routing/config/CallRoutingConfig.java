package com.vektorraum.voicecontrol.service.routing.config;

import com.vektorraum.voicecontrol.model.routing.Action;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "callrouting")
@Component
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CallRoutingConfig {
    private String defaultNumber;
    private Action defaultAction = Action.SEND_TO_VOICE_MAIL;
    private int forwardingTimeout = 20;
    private boolean defaultAvailability = true;
}
