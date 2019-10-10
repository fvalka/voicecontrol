package com.vektorraum.voicecontrol.event;

import com.vektorraum.voicecontrol.model.Call;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class CallEvent extends ApplicationEvent {
    private final Call call;
    private final ZonedDateTime eventCreated = ZonedDateTime.now();

    public CallEvent(Object source, Call call) {
        super(source);
        this.call = call;
    }
}
