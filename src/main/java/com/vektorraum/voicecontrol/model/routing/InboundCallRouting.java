package com.vektorraum.voicecontrol.model.routing;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document("inbound_call_routing_table")
public class InboundCallRouting {
    @Id
    private String id;

    private String from;
    private String to;
    private Action action;
    private String destination;

    @Indexed
    private int priority;
}
