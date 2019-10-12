package com.vektorraum.voicecontrol.service.voicemail;

import com.vektorraum.voicecontrol.event.VoiceMailDownloadCompleteEvent;
import com.vektorraum.voicecontrol.event.VoiceMailRecordingCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class VoiceMailService {
    private ApplicationEventPublisher eventPublisher;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyddMM-HHmmss");

    @Autowired
    public VoiceMailService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    @Async
    public void onCompletedRecording(@NotNull VoiceMailRecordingCompletedEvent event) {
        log.info("Voicemail service received a new recording completed event={}", event);

        if (StringUtils.isEmpty(event.getRecordingUrl())) {
            log.warn("Received event with empty recording url event={}", event);
            return;
        }

        RestTemplate restTemplate = new RestTemplate();

        File file = restTemplate.execute(event.getRecordingUrl(), HttpMethod.GET, null, clientHttpResponse -> {
            Path path = Paths.get("voicemails", generateFileName());
            File ret = path.toFile();
            StreamUtils.copy(clientHttpResponse.getBody(), new FileOutputStream(ret));
            return ret;
        });

        VoiceMailDownloadCompleteEvent eventOut = VoiceMailDownloadCompleteEvent.builder()
                .source(this)
                .callSid(event.getCallSid())
                .recordingSid(event.getRecordingSid())
                .file(file)
                .build();

        eventPublisher.publishEvent(eventOut);
    }

    private String generateFileName() {
        String dateString = dateTimeFormatter.format(ZonedDateTime.now(ZoneId.of("UTC")));
        String randomString = RandomStringUtils.randomAlphanumeric(6);
        return dateString + "-" + randomString + ".wav";
    }
}
