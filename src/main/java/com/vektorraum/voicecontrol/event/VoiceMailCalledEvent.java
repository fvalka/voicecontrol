package com.vektorraum.voicecontrol.event;

import com.vektorraum.voicecontrol.model.Call;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class VoiceMailCalledEvent extends CallEvent {
    public VoiceMailCalledEvent(Object source, Call call) {
        super(source, call);
    }
}
