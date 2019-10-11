package com.vektorraum.voicecontrol.service.tracking;

import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.repository.CallRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CallTrackingService {
    private CallRepository callRepository;

    @Autowired
    public CallTrackingService(CallRepository callRepository) {
        this.callRepository = callRepository;
    }

    public Optional<Call> findCall(String callSid) {
        return callRepository.findById(callSid);
    }

    @EventListener
    public void onInboundCall(@NotNull InboundCallEvent event) {
        log.debug("Storing call into database");
        callRepository.save(event.getCall());
    }
}
