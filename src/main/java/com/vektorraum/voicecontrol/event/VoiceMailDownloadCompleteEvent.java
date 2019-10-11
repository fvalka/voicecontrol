package com.vektorraum.voicecontrol.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.context.ApplicationEvent;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class VoiceMailDownloadCompleteEvent extends ApplicationEvent {
    private String callSid;
    private String recordingSid;
    private File file;

    @Builder
    public VoiceMailDownloadCompleteEvent(Object source, String callSid, String recordingSid, File file) {
        super(source);
        this.callSid = callSid;
        this.recordingSid = recordingSid;
        this.file = file;
    }
}
