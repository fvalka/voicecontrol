package com.vektorraum.voicecontrol.event;

import com.vektorraum.voicecontrol.model.Transcription;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class VoiceMailTranscriptionsEvent extends ApplicationEvent {
    private String callSid;
    private String recordingSid;
    private List<Transcription> transcriptions;

    @Builder
    public VoiceMailTranscriptionsEvent(Object source, String callSid, String recordingSid, List<Transcription> transcriptions) {
        super(source);
        this.callSid = callSid;
        this.recordingSid = recordingSid;
        this.transcriptions = transcriptions;
    }
}
