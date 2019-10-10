package com.vektorraum.voicecontrol.bot;

import com.vektorraum.voicecontrol.bot.config.BotConfig;
import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.event.VoiceMailCalledEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;

@Component
@Slf4j
public class VoiceBot extends AbilityBot {
    private static final String INCOMING_CALL_MESSAGE = "Incoming call:\nFrom: %s\nTo: %s";
    private static final String VOICE_MAIL_CALLED = "Call sent to voicemail:\nFrom: %s";
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

    @EventListener
    public void onInboundCall(@NotNull InboundCallEvent event) {
        log.info("Bot received an inbound call event={}", event);
        sendToSubscribers(String.format(INCOMING_CALL_MESSAGE, event.getCall().getFrom(), event.getCall().getTo()));
    }

    @EventListener
    public void onVoiceMailCalled(@NotNull VoiceMailCalledEvent event) {
        log.info("Bot received voice mail called event={}", event);
        sendToSubscribers(String.format(VOICE_MAIL_CALLED, event.getCall().getFrom()));
    }

    private void sendToSubscribers(String message) {
        silent.send(message, botConfig.getCreatorId());
    }
}
