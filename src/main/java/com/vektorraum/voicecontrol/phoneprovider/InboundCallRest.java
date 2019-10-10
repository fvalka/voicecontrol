package com.vektorraum.voicecontrol.phoneprovider;

import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Say;
import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.phoneprovider.converters.HttpPostToCallConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Twilio based PhoneManagementService which reacts to twilio webhooks and produces TwiML XML instructions.
 *
 * Additionally internal events are generated by the handler methods, which can be used for further
 * processing internally.
 */
@RestController
@Slf4j
public class InboundCallRest {
    private ApplicationEventPublisher applicationEventPublisher;
    private HttpPostToCallConverter toCallConverter;

    @Autowired
    public InboundCallRest(ApplicationEventPublisher applicationEventPublisher, HttpPostToCallConverter toCallConverter) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.toCallConverter = toCallConverter;
    }

    /**
     * Handles an inbound call and produces a TwiML XML output
     *
     * @param body All request parameters, contains From, To, CallSid, etc.
     * @return TwiML XML instructions for how the call should proceed
     */
    @PostMapping(value = "/inbound", produces = "application/xml")
    public String inboundCall(@RequestBody MultiValueMap<String, String> body) {
        log.trace("Inbound call body={}", body.toString());

        Call call = toCallConverter.apply(body);

        log.info("Received inbound call={}", call);

        InboundCallEvent inboundCallEvent = new InboundCallEvent(this, call);

        applicationEventPublisher.publishEvent(inboundCallEvent);

        VoiceResponse.Builder voiceBuilder = new VoiceResponse.Builder();
        Dial callForward = new Dial.Builder().number("+4369912916769")
                .timeout(20)
                .action("/voicemail/")
                .build();

        Redirect redirectToVoiceMail = new Redirect.Builder("/voicemail/").build();

        return voiceBuilder
                //.dial(callForward)
                .redirect(redirectToVoiceMail)
                //.hangup(new Hangup.Builder().build())
                .build()
                .toXml();
    }
}