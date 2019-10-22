package com.vektorraum.voicecontrol.model.routing;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("availability")
@AllArgsConstructor
public class AvailabilitySetting {
    @Id
    private String number;
    private boolean available;
}
