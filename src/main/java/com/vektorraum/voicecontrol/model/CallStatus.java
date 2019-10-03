package com.vektorraum.voicecontrol.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "callstatus")
public class CallStatus {
    @Id
    private String internalEventId;
    @Indexed
    private String callSid;
    private String accountSid;
    private String from;
    private String to;
    private LocalDateTime time;
}
