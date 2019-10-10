package com.vektorraum.voicecontrol.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "call")
@Data
@Builder
public class Call {
    @Id
    private String callSid;

    @Indexed
    private String from;
    @Indexed
    private String to;

    private String accountSid;
    private Integer callDuration;
    private Direction direction;
    private Status status;
}
