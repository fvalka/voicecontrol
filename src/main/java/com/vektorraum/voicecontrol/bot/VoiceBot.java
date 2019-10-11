package com.vektorraum.voicecontrol.bot;

import com.vektorraum.voicecontrol.bot.config.BotConfig;
import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.event.VoiceMailCalledEvent;
import com.vektorraum.voicecontrol.event.VoiceMailDownloadCompleteEvent;
import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.service.tracking.CallTrackingService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@Slf4j
public class VoiceBot extends AbilityBot {
    private static final String INCOMING_CALL_MESSAGE = "Incoming call:\nFrom: %s\nTo: %s";
    private static final String VOICE_MAIL_CALLED = "Call sent to voicemail:\nFrom: %s";
    private static final String VOICE_MAIL_AUDIO_CAPTION = "New voicemail from: %s";
    private static final String SEND_AUDIO_ERROR = "Received voicemail from: %s but couldn't send the audio file!";
    private BotConfig botConfig;
    private CallTrackingService callTrackingService;

    @Autowired
    public VoiceBot(BotConfig botConfig, CallTrackingService callTrackingService) {
        super(botConfig.getToken(), botConfig.getUsername());
        this.botConfig = botConfig;
        this.callTrackingService = callTrackingService;
    }

    @Override
    public int creatorId() {
        return (int) botConfig.getCreatorId();
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

    @EventListener
    public void onVoiceMailDownloaded(@NotNull VoiceMailDownloadCompleteEvent event) {
        log.info("Bot received voice mail download complete event even={}", event);
        String from = callTrackingService.findCall(event.getCallSid()).map(Call::getFrom).orElse("UNKOWN");

        SendAudio sendAudio = new SendAudio();
        sendAudio.setAudio(event.getFile());
        sendAudio.setCaption(String.format(VOICE_MAIL_AUDIO_CAPTION, from));
        sendAudio.setChatId(botConfig.getCreatorId());

        try {
            sender.sendAudio(sendAudio);
        } catch (TelegramApiException e) {
            sendToSubscribers(String.format(SEND_AUDIO_ERROR, from));
            log.warn("Failed to send audio file for event={}", event, e);
        }
    }

    private void sendToSubscribers(String message) {
        silent.send(message, botConfig.getCreatorId());
    }
}
