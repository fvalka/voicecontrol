package com.vektorraum.voicecontrol.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.List;

@Document(collection = "callproperties")
@Data
public class CallProperties {
    @Id
    private String internalId;

    private String accountSid;
    private String annotation;
    private String answeredBy;
    private String apiVersion;
    private String callerName;
    private ZonedDateTime dateCreated;
    private ZonedDateTime dateUpdated;
    private Direction direction;
    private Integer duration;
    private ZonedDateTime endTime;
    private String forwardedFrom;
    private String from;
    private String fromFormatted;
    private String groupSid;
    private String parentCallSid;
    private String phoneNumberSid;
    private BigDecimal price;
    private Currency currency;
    @Indexed
    private String sid;
    private ZonedDateTime startTime;
    private Status status;
    private List<String> subresourceUris;
    @Indexed
    private String to;
    private String toFormatted;
    private String uri;


}
