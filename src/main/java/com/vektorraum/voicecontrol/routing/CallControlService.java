package com.vektorraum.voicecontrol.routing;

import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.routing.instruction.Action;
import com.vektorraum.voicecontrol.routing.instruction.CallControlInstruction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CallControlService {
    public CallControlInstruction inboundCall(Call call) {
        if(call == null) {
            log.warn("CallControlService received a call which was null, this should not happen. Call was rejected.");
            return CallControlInstruction.builder().action(Action.REJECT).build();
        }

        throw new RuntimeException("Not Implemented yet");
    }
}
