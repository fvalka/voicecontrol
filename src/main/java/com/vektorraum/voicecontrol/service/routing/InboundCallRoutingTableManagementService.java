package com.vektorraum.voicecontrol.service.routing;

import com.vektorraum.voicecontrol.model.routing.InboundCallRouting;
import com.vektorraum.voicecontrol.repository.InboundCallRoutingRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class InboundCallRoutingTableManagementService {
    private InboundCallRoutingRepository repository;

    public InboundCallRoutingTableManagementService(InboundCallRoutingRepository repository) {
        this.repository = repository;
    }

    /**
     * Lists all currently stored routes
     *
     * @return All routes, ordered by priority
     */
    public List<InboundCallRouting> listRoutes() {
        return this.repository.findAllByOrderByPriorityDesc();
    }

    /**
     * Adds a routing table entry and stores it in the database
     *
     * @param routingTableEntry A routing table entry
     */
    public void add(@NotNull InboundCallRouting routingTableEntry) {
        this.repository.save(routingTableEntry);
    }
}
