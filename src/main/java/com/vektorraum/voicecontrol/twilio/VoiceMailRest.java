package com.vektorraum.voicecontrol.twilio;

import com.twilio.http.HttpMethod;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Hangup;
import com.twilio.twiml.voice.Record;
import com.twilio.twiml.voice.Say;
import com.vektorraum.voicecontrol.event.VoiceMailCalledEvent;
import com.vektorraum.voicecontrol.event.VoiceMailRecordingCompletedEvent;
import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.twilio.converters.HttpPostToCallConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides voicemail functionality through call recording and forwarding.
 *
 * Forwards the caller to the thankyoubye endpoint, if the recording times out, reaches its maximum length
 * or the customer ends the recording through key input.
 */
@Slf4j
@RestController
@RequestMapping("/twilio/voicemail")
public class VoiceMailRest {
    private ApplicationEventPublisher applicationEventPublisher;
    private HttpPostToCallConverter toCallConverter;

    @Autowired
    public VoiceMailRest(ApplicationEventPublisher applicationEventPublisher, HttpPostToCallConverter toCallConverter) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.toCallConverter = toCallConverter;
    }

    /**
     * Initial voicemail webhook point.
     *
     * A message will be played to the caller, and then the recording started. Finally the caller will be redirected
     * to a thankyou point or informed that no recording has been received.
     *
     * @param body All request parameters, contains From, To, CallSid, etc.
     * @return TwiML for voicemail, with redirect to thankyoubye when finished
     */
    @PostMapping(value = "/", produces = "application/xml")
    public String voicemail(@RequestBody MultiValueMap<String, String> body) {
        Call call = toCallConverter.apply(body);
        log.info("Voicemail called call={}", call);

        VoiceMailCalledEvent event = new VoiceMailCalledEvent(this, call);
        applicationEventPublisher.publishEvent(event);

        Say voiceMailGreeting = new Say.Builder("Hi! You have reached the voice mail of Fabian Valka. Please leave a message after the beep")
                .voice(Say.Voice.POLLY_MATTHEW)
                .build();

        Record voiceMailRecord = new Record.Builder()
                .action("/twilio/voicemail/thankyoubye")
                .timeout(5)
                .playBeep(true)
                .finishOnKey("*")
                .recordingStatusCallback("/twilio/voicemail/recordingstatus")
                .recordingStatusCallbackMethod(HttpMethod.POST)
                .trim(Record.Trim.TRIM_SILENCE)
                .build();

        Say noMessageReceived = new Say.Builder("No message recorded. Thank you! Bye!")
                .voice(Say.Voice.POLLY_MATTHEW)
                .build();

        return new VoiceResponse.Builder()
                .say(voiceMailGreeting)
                .record(voiceMailRecord)
                .say(noMessageReceived)
                .hangup(new Hangup.Builder().build())
                .build()
                .toXml();
    }

    /**
     * Plays a thank you message to the caller, after the voice mail has been compelted.
     * @param body
     * @return
     */
    @PostMapping(value = "/thankyoubye", produces = "application/xml")
    public String thankYouBye(@RequestBody MultiValueMap<String, String> body) {
        Say thankYou = new Say.Builder("Thank you for leaving a message! I will get back to you as soon as possible!")
                .voice(Say.Voice.POLLY_MATTHEW)
                .build();

        return new VoiceResponse.Builder()
                .say(thankYou)
                .hangup(new Hangup.Builder().build())
                .build()
                .toXml();
    }


    /**
     * Handles the recording webhook callback
     *
     * @param body
     */
    @PostMapping("/recordingstatus")
    public void recordingStatus(@RequestBody MultiValueMap<String, String> body) {
        Call call = toCallConverter.apply(body);
        log.info("New voice mail recording status update received for call={}", call);

        if("completed".equals(body.getFirst("RecordingStatus"))) {
            VoiceMailRecordingCompletedEvent event = VoiceMailRecordingCompletedEvent.builder()
                    .source(this)
                    .callSid(body.getFirst("CallSid"))
                    .recordingSid(body.getFirst("RecordingSid"))
                    .recordingUrl(body.getFirst("RecordingUrl"))
                    .build();
            applicationEventPublisher.publishEvent(event);
        } else {
            log.warn("Received status update on incomplete recording call={}", call);
        }
    }
}
