package com.vektorraum.voicecontrol.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class VoiceMailRecordingCompletedEvent extends ApplicationEvent {
    private String callSid;
    private String recordingSid;
    private String recordingUrl;

    @Builder
    public VoiceMailRecordingCompletedEvent(Object source, String callSid, String recordingSid, String recordingUrl) {
        super(source);
        this.callSid = callSid;
        this.recordingSid = recordingSid;
        this.recordingUrl = recordingUrl;
    }
}
