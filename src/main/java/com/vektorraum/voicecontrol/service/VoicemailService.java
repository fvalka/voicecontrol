package com.vektorraum.voicecontrol.service;

import com.vektorraum.voicecontrol.event.VoiceMailRecordingCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VoicemailService {
    public void onCompletedRecording(@NotNull VoiceMailRecordingCompletedEvent event) {
        log.debug("Voicemail service processing recording event");
    }
}
