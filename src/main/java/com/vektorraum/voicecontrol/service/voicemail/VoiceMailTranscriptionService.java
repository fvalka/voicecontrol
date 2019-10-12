package com.vektorraum.voicecontrol.service.voicemail;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1p1beta1.*;
import com.google.protobuf.ByteString;
import com.vektorraum.voicecontrol.event.VoiceMailDownloadCompleteEvent;
import com.vektorraum.voicecontrol.event.VoiceMailTranscriptionsEvent;
import com.vektorraum.voicecontrol.model.Transcription;
import com.vektorraum.voicecontrol.service.voicemail.config.VoiceMailConfig;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Transcribes voice mail messages from speech to text using the Google Cloud Platform speech to text service
 *
 * Triggered by a {@link VoiceMailDownloadCompleteEvent}
 */
@Service
@Slf4j
public class VoiceMailTranscriptionService {
    private VoiceMailConfig voiceMailConfig;
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public VoiceMailTranscriptionService(VoiceMailConfig voiceMailConfig, ApplicationEventPublisher eventPublisher) {
        this.voiceMailConfig = voiceMailConfig;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Async
    public void transcribeVoiceMail(@NotNull VoiceMailDownloadCompleteEvent event) {
        if (!voiceMailConfig.isTranscriptionEnabled()) {
            log.debug("Transcription disabled, skipping event handling");
            return;
        }

        try (SpeechClient speechClient = SpeechClient.create()) {
            byte[] rawAudio = Files.readAllBytes(event.getFile().toPath());
            RecognitionConfig config = getTranscriptionConfiguration();
            RecognitionAudio content = RecognitionAudio.newBuilder()
                    .setContent(ByteString.copyFrom(rawAudio))
                    .build();

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> futureResponse =
                    speechClient.longRunningRecognizeAsync(config, content);
            LongRunningRecognizeResponse response = futureResponse.get(voiceMailConfig.getTranscriptionTimeout(), TimeUnit.SECONDS);

            if(response.getResultsList().isEmpty()) {
                log.warn("Transcription failed, received only an empty result");
                return;
            }

            List<Transcription> transcriptions = response.getResultsList().stream()
                    .flatMap(it -> it.getAlternativesList().stream())
                    .map(it -> Transcription.builder()
                            .confidence(it.getConfidence())
                            .transcription(it.getTranscript())
                            .build())
                    .filter(it -> it.getConfidence() >= voiceMailConfig.getTranscriptionConfidenceCutOff())
                    .collect(Collectors.toList());

            if(!transcriptions.isEmpty()) {
                log.info("Received the following transcriptions={} for the event={}", transcriptions, event);

                VoiceMailTranscriptionsEvent eventOut = VoiceMailTranscriptionsEvent.builder()
                        .callSid(event.getCallSid())
                        .recordingSid(event.getRecordingSid())
                        .transcriptions(transcriptions)
                        .source(this)
                        .build();

                eventPublisher.publishEvent(eventOut);

            } else {
                log.info("Received no transcriptions for event={}", event);
            }
        } catch (Exception ex) {
            log.error("Could not transcribe voice mail event={}", event, ex);
        }
    }

    @NotNull
    private RecognitionConfig getTranscriptionConfiguration() {
        return RecognitionConfig.newBuilder()
                        .setLanguageCode(voiceMailConfig.getTranscriptionLanguages().get(0))
                        .addAllAlternativeLanguageCodes(
                                voiceMailConfig.getTranscriptionLanguages())
                        .setSampleRateHertz(8000)
                        .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16)
                        .setUseEnhanced(true)
                        .build();
    }
}
