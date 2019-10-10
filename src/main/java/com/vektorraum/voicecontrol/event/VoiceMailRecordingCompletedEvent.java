package com.vektorraum.voicecontrol.event;

import com.vektorraum.voicecontrol.model.Call;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VoiceMailRecordingCompletedEvent extends CallEvent {
    private String recordingSid;
    private String recordingUrl;

    @Builder
    public VoiceMailRecordingCompletedEvent(Object source, Call call, String recordingSid) {
        super(source, call);
        this.recordingSid = recordingSid;
    }
}
