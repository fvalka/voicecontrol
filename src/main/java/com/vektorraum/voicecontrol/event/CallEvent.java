package com.vektorraum.voicecontrol.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CallEvent extends ApplicationEvent {
    private final String from;
    private final String to;
    private final ZonedDateTime time;
    private final String callId;

    public CallEvent(Object source, String from, String to, ZonedDateTime time, String callId) {
        super(source);
        this.from = from;
        this.to = to;
        this.time = time;
        this.callId = callId;
    }
}
