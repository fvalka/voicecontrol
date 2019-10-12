package com.vektorraum.voicecontrol.twilio.converters;

import com.vektorraum.voicecontrol.model.Call;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;

import java.util.function.Function;

@Component
@Slf4j
public class HttpPostToCallConverter implements Function<MultiValueMap<String, String>, Call> {
    /**
     * Converts a Twilio HTTP Post {@link MultiValueMap } to a {@link Call} object.
     *
     * @param body Twilio HTTP Post request map
     * @return Call object, with fields not contained in the post being null
     */
    @Override
    public Call apply(MultiValueMap<String, String> body) {
        log.trace("Converting http post map to call object");
        if(body == null) {
            return null;
        }

        String callSid = body.getFirst("CallSid");
        String accountSid = body.getFirst("AccountSid");
        String from = body.getFirst("From");
        String to = body.getFirst("To");

        String callDuration = body.getFirst("CallDuration");
        Integer callDurationConverted = null;
        if(callDuration != null) {
            try {
                callDurationConverted = Integer.parseInt(callDuration);
            } catch (NumberFormatException nfe) {
                log.error("Call duration was not empty, but could not be parsed to Integer");
            }
        }

        return Call.builder()
                .accountSid(accountSid)
                .callSid(callSid)
                .from(from)
                .to(to)
                .callDuration(callDurationConverted)
                .build();
    }
}
