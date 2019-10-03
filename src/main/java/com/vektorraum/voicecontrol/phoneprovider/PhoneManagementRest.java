package com.vektorraum.voicecontrol.phoneprovider;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Hangup;
import com.twilio.twiml.voice.Say;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class PhoneManagementRest {
    @PostMapping(value = "/inbound", produces = "application/xml")
    public String inboundCall(@RequestBody MultiValueMap<String, String> body) {
        log.debug(body.toString());

        VoiceResponse.Builder voiceBuilder = new VoiceResponse.Builder();
        Say greeting = new Say.Builder("You have reached the end of the internet").voice(Say.Voice.POLLY_MATTHEW).build();

        return voiceBuilder.say(greeting)
                .hangup(new Hangup.Builder().build())
                .build()
                .toXml();
    }

    @PostMapping("/status")
    public void status(@RequestBody MultiValueMap<String, String> body) {

    }

    @GetMapping("/test")
    public String test() {
        return "Test";
    }
}
