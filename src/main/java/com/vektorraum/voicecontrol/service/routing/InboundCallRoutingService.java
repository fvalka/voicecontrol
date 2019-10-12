package com.vektorraum.voicecontrol.service.routing;

import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.model.routing.Action;
import com.vektorraum.voicecontrol.model.routing.InboundCallRouting;
import com.vektorraum.voicecontrol.repository.InboundCallRoutingRepository;
import com.vektorraum.voicecontrol.service.routing.config.CallRoutingConfig;
import com.vektorraum.voicecontrol.service.routing.instruction.CallControlInstruction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class InboundCallRoutingService  {
    private CallRoutingConfig routingConfig;
    private InboundCallRoutingRepository inboundCallRoutingRepository;

    @Autowired
    public InboundCallRoutingService(CallRoutingConfig routingConfig, InboundCallRoutingRepository inboundCallRoutingRepository) {
        this.routingConfig = routingConfig;
        this.inboundCallRoutingRepository = inboundCallRoutingRepository;
    }

    public CallControlInstruction routeCall(Call call) {
        if(call == null) {
            log.warn("CallControlService received a call which was null, this should not happen. Call was rejected.");
            return CallControlInstruction.builder().action(Action.REJECT).build();
        }

        Optional<InboundCallRouting> routingRule = inboundCallRoutingRepository.findAllByOrderByPriorityDesc().stream()
                .filter(rule -> wildcardMatch(call.getFrom(), rule.getFrom()))
                .filter(rule -> wildcardMatch(call.getTo(), rule.getTo()))
                .findFirst();

        Action action = routingRule.map(InboundCallRouting::getAction).orElse(routingConfig.getDefaultAction());
        String destination = routingRule.map(InboundCallRouting::getDestination).orElse(routingConfig.getDefaultNumber());

        return CallControlInstruction.builder().action(action).destination(destination).build();
    }

    public int getForwardingTimeOut() {
        return routingConfig.getForwardingTimeout();
    }

    private boolean wildcardMatch(String contents, String pattern) {
        return FilenameUtils.wildcardMatch(contents, pattern);
    }
}
