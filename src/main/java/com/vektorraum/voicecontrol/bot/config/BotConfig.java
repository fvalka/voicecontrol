package com.vektorraum.voicecontrol.bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "bot")
@Component
@Data
public class BotConfig {
    private String token;
    private String username;
    private int creatorId;
}