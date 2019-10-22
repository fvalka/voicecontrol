package com.vektorraum.voicecontrol.service.routing;

import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.model.routing.Action;
import com.vektorraum.voicecontrol.model.routing.AvailabilitySetting;
import com.vektorraum.voicecontrol.model.routing.InboundCallRouting;
import com.vektorraum.voicecontrol.repository.AvailabilitySettingRepository;
import com.vektorraum.voicecontrol.repository.InboundCallRoutingRepository;
import com.vektorraum.voicecontrol.service.routing.config.CallRoutingConfig;
import com.vektorraum.voicecontrol.service.routing.instruction.CallControlInstruction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class InboundCallRoutingServiceTest {
    @Mock
    private InboundCallRoutingRepository callRoutingRepository;

    @Mock
    private AvailabilitySettingRepository availabilitySettingRepository;

    private CallRoutingConfig config = CallRoutingConfig.builder()
            .defaultAction(Action.FORWARD)
            .defaultNumber("+120909090909")
            .forwardingTimeout(45)
            .defaultAvailability(true)
            .build();

    @Test
    public void givenOnlyDefaultRouting_thenCorrectResponseIsGiven() {
        Mockito.when(callRoutingRepository.findAllByOrderByPriorityDesc()).thenReturn(List.of());
        Mockito.when(availabilitySettingRepository.findById(any())).thenReturn(Optional.empty());
        InboundCallRoutingService cut = new InboundCallRoutingService(config, callRoutingRepository, availabilitySettingRepository);
        Call call = Call.builder()
                .accountSid("asdadsadsasd")
                .to("+324232323")
                .from("+4949494939")
                .build();

        CallControlInstruction instruction = cut.routeCall(call);

        assertEquals(config.getDefaultAction(), instruction.getAction());
        assertEquals(config.getDefaultNumber(), instruction.getDestination());
    }

    @Test
    public void givenRoutingTable_withMultipleMatchingEntries_thenFirstOneWins() {
        InboundCallRouting nonMatchingRule = InboundCallRouting.builder()
                .action(Action.FORWARD)
                .from("+1*")
                .to("*")
                .destination("+9594282")
                .priority(100)
                .build();

        InboundCallRouting firstMatchingRule = InboundCallRouting.builder()
                .action(Action.FORWARD)
                .from("+43*")
                .to("*")
                .destination("+439595959")
                .priority(90)
                .build();

        InboundCallRouting secondMatchingRule = InboundCallRouting.builder()
                .action(Action.REJECT)
                .from("+4*")
                .to("*")
                .destination("+96969696")
                .priority(80)
                .build();

        Call call = Call.builder()
                .from("+4367670707070")
                .to("+439696969669")
                .build();

        List<InboundCallRouting> rules = List.of(nonMatchingRule, firstMatchingRule, secondMatchingRule);
        Mockito.when(callRoutingRepository.findAllByOrderByPriorityDesc()).thenReturn(rules);
        Mockito.when(availabilitySettingRepository.findById(any())).thenReturn(Optional.empty());
        InboundCallRoutingService cut = new InboundCallRoutingService(config, callRoutingRepository, availabilitySettingRepository);

        CallControlInstruction instruction = cut.routeCall(call);

        assertEquals(firstMatchingRule.getAction(), instruction.getAction());
        assertEquals(firstMatchingRule.getDestination(), instruction.getDestination());
    }

    @Test
    public void givenMultipleRules_whenNonMatches_thenDefaultIsUsed() {
        InboundCallRouting firstMatchingRule = InboundCallRouting.builder()
                .action(Action.REJECT)
                .from("+43*")
                .to("*")
                .destination("+439595959")
                .priority(90)
                .build();

        InboundCallRouting secondMatchingRule = InboundCallRouting.builder()
                .action(Action.REJECT)
                .from("+4*")
                .to("*")
                .destination("+96969696")
                .priority(80)
                .build();

        Call call = Call.builder()
                .from("+9667670707070")
                .to("+439696969669")
                .build();

        List<InboundCallRouting> rules = List.of(firstMatchingRule, secondMatchingRule);
        Mockito.when(callRoutingRepository.findAllByOrderByPriorityDesc()).thenReturn(rules);
        Mockito.when(availabilitySettingRepository.findById(any())).thenReturn(Optional.empty());
        InboundCallRoutingService cut = new InboundCallRoutingService(config, callRoutingRepository, availabilitySettingRepository);

        CallControlInstruction instruction = cut.routeCall(call);


        assertEquals(config.getDefaultAction(), instruction.getAction());
        assertEquals(config.getDefaultNumber(), instruction.getDestination());
    }

    @Test
    public void givenRulesAndUnavailableNumber_thenCallIsSentToVoiceMail() {
        String destination = "+6794828289";
        InboundCallRouting nonMatchingRule = InboundCallRouting.builder()
                .action(Action.FORWARD)
                .from("+1*")
                .to("*")
                .destination("+9594282")
                .priority(100)
                .build();

        InboundCallRouting firstMatchingRule = InboundCallRouting.builder()
                .action(Action.FORWARD)
                .from("+43*")
                .to("*")
                .destination(destination)
                .priority(90)
                .build();

        InboundCallRouting secondMatchingRule = InboundCallRouting.builder()
                .action(Action.FORWARD)
                .from("+4*")
                .to("*")
                .destination("+96969696")
                .priority(80)
                .build();

        AvailabilitySetting unavailable = new AvailabilitySetting(destination, false);

        Call call = Call.builder()
                .from("+4367670707070")
                .to("+439696969669")
                .build();

        List<InboundCallRouting> rules = List.of(nonMatchingRule, firstMatchingRule, secondMatchingRule);
        Mockito.when(callRoutingRepository.findAllByOrderByPriorityDesc()).thenReturn(rules);
        Mockito.when(availabilitySettingRepository.findById(any())).thenReturn(Optional.of(unavailable));
        InboundCallRoutingService cut = new InboundCallRoutingService(config, callRoutingRepository, availabilitySettingRepository);

        CallControlInstruction instruction = cut.routeCall(call);

        assertEquals(Action.SEND_TO_VOICE_MAIL, instruction.getAction());
        assertEquals(destination, instruction.getDestination());
    }

}