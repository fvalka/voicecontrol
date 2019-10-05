package com.vektorraum.voicecontrol.bot;

import com.vektorraum.voicecontrol.bot.config.BotConfig;
import com.vektorraum.voicecontrol.event.InboundCallEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;

@Component
@Slf4j
public class VoiceBot extends AbilityBot implements ApplicationListener<InboundCallEvent> {
    private static final String INCOMING_CALL_MESSAGE = "Incoming call:\nFrom: %s\nTo: %s";
    private BotConfig botConfig;

    @Autowired
    public VoiceBot(BotConfig botConfig) {
        super(botConfig.getToken(), botConfig.getUsername());
        this.botConfig = botConfig;
    }

    @Override
    public int creatorId() {
        return botConfig.getCreatorId();
    }

    @Override
    public void onApplicationEvent(@NotNull InboundCallEvent event) {
        log.info("Bot received an inbound call event={}", event);
        silent.send(String.format(INCOMING_CALL_MESSAGE, event.getFrom(), event.getTo()), botConfig.getCreatorId());
    }
}
