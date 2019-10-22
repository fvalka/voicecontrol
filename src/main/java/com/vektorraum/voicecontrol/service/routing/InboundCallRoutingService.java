package com.vektorraum.voicecontrol.service.routing;

import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.model.routing.Action;
import com.vektorraum.voicecontrol.model.routing.AvailabilitySetting;
import com.vektorraum.voicecontrol.model.routing.InboundCallRouting;
import com.vektorraum.voicecontrol.repository.AvailabilitySettingRepository;
import com.vektorraum.voicecontrol.repository.InboundCallRoutingRepository;
import com.vektorraum.voicecontrol.service.routing.config.CallRoutingConfig;
import com.vektorraum.voicecontrol.service.routing.instruction.CallControlInstruction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Slf4j
public class InboundCallRoutingService  {
    private CallRoutingConfig routingConfig;
    private InboundCallRoutingRepository inboundCallRoutingRepository;
    private AvailabilitySettingRepository availabilitySettingRepository;
    private Pattern e164Regex = Pattern.compile("^\\+?[1-9]\\d{1,14}$");


    @Autowired
    public InboundCallRoutingService(CallRoutingConfig routingConfig, InboundCallRoutingRepository inboundCallRoutingRepository,
                                     AvailabilitySettingRepository availabilitySettingRepository) {
        this.routingConfig = routingConfig;
        this.inboundCallRoutingRepository = inboundCallRoutingRepository;
        this.availabilitySettingRepository = availabilitySettingRepository;
    }

    /**
     * Routes a call based upon the routing table stored in {@link InboundCallRouting} and the call availability
     * stored in {@link AvailabilitySetting}.
     *
     * @param call The call to route. With from and to numbers in E.164 format.
     * @return Call routing instructions
     */
    public CallControlInstruction routeCall(Call call) {
        if(call == null) {
            log.warn("CallControlService received a call which was null, this should not happen. Call was rejected.");
            return CallControlInstruction.builder().action(Action.REJECT).build();
        }

        Optional<InboundCallRouting> routingRule = inboundCallRoutingRepository.findAllByOrderByPriorityDesc().stream()
                .filter(rule -> wildcardMatch(call.getFrom(), rule.getFrom()))
                .filter(rule -> wildcardMatch(call.getTo(), rule.getTo()))
                .findFirst();

        Action action = routingRule.map(InboundCallRouting::getAction).orElse(routingConfig.getDefaultAction());
        String destination = routingRule.map(InboundCallRouting::getDestination).orElse(routingConfig.getDefaultNumber());

        if (!this.isAvailable(destination)) {
            action = Action.SEND_TO_VOICE_MAIL;
        }

        return CallControlInstruction.builder().action(action).destination(destination).build();
    }

    /**
     * Set the availability for a specific number.
     *
     * @param number Number to set the availability for, in E.164 format.
     *               Can be {@code null}, in this case the default number will be set
     *               e.g. +1494883828248
     * @param value True if number is available
     */
    public void setAvailability(String number, boolean value) {
        if (number == null) {
            number = routingConfig.getDefaultNumber();
        }

        if (!e164Regex.matcher(number).matches()) {
            throw new NumberFormatException("The input number doesn't match the E.164 standard");
        }

        AvailabilitySetting setting = new AvailabilitySetting(number, value);
        availabilitySettingRepository.save(setting);
    }

    /**
     * Checks the current availability setting on the given number
     *
     * @param number Number to find the availability for in E.164 format. e.g. +43858282838
     * @return Availability setting in database or default availability if none is found
     */
    public boolean isAvailable(String number) {
        if (number == null) {
            number = routingConfig.getDefaultNumber();
        }

        if (!e164Regex.matcher(number).matches()) {
            throw new NumberFormatException("The input number doesn't match the E.164 standard");
        }

        return availabilitySettingRepository.findById(number)
                .map(AvailabilitySetting::isAvailable)
                .orElse(routingConfig.isDefaultAvailability());
    }

    /**
     * Timeout before the caller should be forwarded to voicemail.
     *
     * Set to a fixed value in the configuration.
     *
     * @return Timeout for forwarding to voice mail in seconds
     */
    public int getForwardingTimeOut() {
        return routingConfig.getForwardingTimeout();
    }

    private boolean wildcardMatch(String contents, String pattern) {
        return FilenameUtils.wildcardMatch(contents, pattern);
    }
}
