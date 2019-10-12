package com.vektorraum.voicecontrol.service.routing.instruction;

import com.vektorraum.voicecontrol.model.routing.Action;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallControlInstruction {
    private final Action action;
    private final String destination;
}
