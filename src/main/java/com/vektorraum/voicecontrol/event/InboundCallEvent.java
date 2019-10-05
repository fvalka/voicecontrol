package com.vektorraum.voicecontrol.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class InboundCallEvent extends CallEvent {
    @Builder
    public InboundCallEvent(Object source, String from, String to, ZonedDateTime time, String callId) {
        super(source, from, to, time, callId);
    }
}
