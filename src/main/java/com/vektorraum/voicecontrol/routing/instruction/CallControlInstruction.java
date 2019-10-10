package com.vektorraum.voicecontrol.routing.instruction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CallControlInstruction {
    private final Action action;
    private final String destination;
}
