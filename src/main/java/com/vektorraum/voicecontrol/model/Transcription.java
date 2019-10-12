package com.vektorraum.voicecontrol.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Transcription {
    private final float confidence;
    private final String transcription;
}
