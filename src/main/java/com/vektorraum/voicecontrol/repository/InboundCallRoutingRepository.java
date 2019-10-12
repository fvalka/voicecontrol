package com.vektorraum.voicecontrol.repository;

import com.vektorraum.voicecontrol.model.routing.InboundCallRouting;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface InboundCallRoutingRepository extends CrudRepository<InboundCallRouting, String> {
    List<InboundCallRouting> findAllByOrderByPriorityDesc();
}
