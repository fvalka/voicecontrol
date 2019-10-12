package com.vektorraum.voicecontrol.bot;

import com.vektorraum.voicecontrol.bot.config.BotConfig;
import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.event.VoiceMailCalledEvent;
import com.vektorraum.voicecontrol.event.VoiceMailDownloadCompleteEvent;
import com.vektorraum.voicecontrol.event.VoiceMailTranscriptionsEvent;
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

import java.util.stream.Collectors;

@Component
@Slf4j
public class VoiceBot extends AbilityBot {
    private static final String INCOMING_CALL_MESSAGE = "\uD83D\uDCF2 %s\nTo: %s\n**Incoming call**";
    private static final String VOICE_MAIL_CALLED = "\uD83D\uDCFC %s\n**Sent to voicemail**";
    private static final String VOICE_MAIL_AUDIO_CAPTION = "\uD83D\uDCFC %s **new voicemail**";
    private static final String SEND_AUDIO_ERROR = "Received voicemail from: %s but couldn't send the audio file!";
    private static final String TRANSCRIPTIONS_MESSAGE = "Transcription for voicemail from: %s\n%s";

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
        String from = findFromByCallSid(event.getCallSid());

        SendAudio sendAudio = new SendAudio();
        sendAudio.setAudio(event.getFile());
        sendAudio.setCaption(String.format(VOICE_MAIL_AUDIO_CAPTION, from));
        sendAudio.setChatId(botConfig.getCreatorId());
        sendAudio.setPerformer(from);

        try {
            sender.sendAudio(sendAudio);
        } catch (TelegramApiException e) {
            sendToSubscribers(String.format(SEND_AUDIO_ERROR, from));
            log.warn("Failed to send audio file for event={}", event, e);
        }
    }

    @EventListener
    public void onVoiceMailTranscription(@NotNull VoiceMailTranscriptionsEvent event) {
        String from = findFromByCallSid(event.getCallSid());
        String transcriptions = event.getTranscriptions().stream()
                .map(it -> String.format("Confidence: %s\n%s", it.getConfidence(), it.getTranscription()))
                .collect(Collectors.joining("\n"));
        String message = String.format(TRANSCRIPTIONS_MESSAGE, from, transcriptions);
        sendToSubscribers(message);
    }

    @NotNull
    private String findFromByCallSid(String callSid) {
        return callTrackingService.findCall(callSid).map(Call::getFrom).orElse("UNKOWN");
    }

    private void sendToSubscribers(String message) {
        silent.sendMd(message, botConfig.getCreatorId());
    }
}
