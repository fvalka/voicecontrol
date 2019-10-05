package com.vektorraum.voicecontrol;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.telegram.telegrambots.ApiContextInitializer;

@SpringBootApplication
@EnableConfigurationProperties
public class VoicecontrolApplication {
    static {
        // Telegram Bots
        ApiContextInitializer.init();
    }

    public static void main(String[] args) {
        SpringApplication.run(VoicecontrolApplication.class, args);
    }

}
