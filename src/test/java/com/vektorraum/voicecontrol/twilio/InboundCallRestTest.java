package com.vektorraum.voicecontrol.twilio;

import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.model.routing.Action;
import com.vektorraum.voicecontrol.service.routing.InboundCallRoutingService;
import com.vektorraum.voicecontrol.service.routing.instruction.CallControlInstruction;
import com.vektorraum.voicecontrol.twilio.converters.HttpPostToCallConverter;
import com.vektorraum.voicecontrol.twilio.security.TwilioValidationFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@WebMvcTest(InboundCallRest.class)
public class InboundCallRestTest {
    private static final String ACCOUNT_SID = "xafaccountsid";
    private static final String FROM = "+4312345678";
    private static final String TO = "+43987654321";
    private static final String INBOUND = "inbound";
    private static final String RINGING = "ringing";
    private static final String CALLSID = RandomStringUtils.randomAlphanumeric(34);

    @MockBean
    private ApplicationEventPublisher eventPublisher;

    @Captor
    private ArgumentCaptor<InboundCallEvent> eventCaptor;

    @SpyBean
    private HttpPostToCallConverter httpPostToCallConverter;

    @MockBean
    private TwilioValidationFilter twilioValidationFilter;

    @MockBean
    private InboundCallRoutingService routingService;

    private InboundCallRest cut;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        mockMvc = standaloneSetup(new InboundCallRest(eventPublisher, httpPostToCallConverter, routingService)).build();
    }

    @Test
    public void givenPostRequest_whenInputIsValid_thenEventIsFiredAndOutputCorrect() throws Exception {
        CallControlInstruction instruction = CallControlInstruction.builder()
                .action(Action.SEND_TO_VOICE_MAIL)
                .destination("1234")
                .build();
        Mockito.when(routingService.routeCall(any())).thenReturn(instruction);

        postInboundCall()
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=ISO-8859-1"))
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Redirect>/twilio/voicemail/</Redirect></Response>"));

        Mockito.verify(eventPublisher, Mockito.times(1)).publishEvent(eventCaptor.capture());
        InboundCallEvent event = eventCaptor.getValue();
        Assert.assertNotNull(event);
        Assert.assertNotNull(event.getCall());
        Assert.assertEquals(FROM, event.getCall().getFrom());
        Assert.assertEquals(TO, event.getCall().getTo());
        Assert.assertEquals(ACCOUNT_SID, event.getCall().getAccountSid());
    }

    @Test
    public void givenPostRequest_whenForwardingToVoicemail_thenTwiMLIsCorrect() throws Exception {
        CallControlInstruction instruction = CallControlInstruction.builder()
                .action(Action.SEND_TO_VOICE_MAIL)
                .build();

        Mockito.when(routingService.routeCall(any())).thenReturn(instruction);

        postInboundCall()
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Response>" +
                        "<Redirect>/twilio/voicemail/</Redirect>" +
                        "</Response>"));
    }

    @Test
    public void givenPostRequest_whenRejectingCall_thenTwiMLIsCorrect() throws Exception {
        CallControlInstruction instruction = CallControlInstruction.builder()
                .action(Action.REJECT)
                .build();

        Mockito.when(routingService.routeCall(any())).thenReturn(instruction);

        postInboundCall()
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Response>" +
                        "<Say voice=\"Polly.Matthew\">The number you have called is currently unavailable!</Say>" +
                        "<Hangup/>" +
                        "</Response>"));
    }

    @Test
    public void givenPostRequest_whenForwardingCall_thenTwiMLIsCorrect() throws Exception {
        String destination = "+434343434343";
        CallControlInstruction instruction = CallControlInstruction.builder()
                .action(Action.FORWARD)
                .destination(destination)
                .build();

        Mockito.when(routingService.routeCall(any())).thenReturn(instruction);
        Mockito.when(routingService.getForwardingTimeOut()).thenReturn(35);

        postInboundCall()
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Response>" +
                        "<Dial action=\"/twilio/inbound/dial_complete\" timeout=\"35\">+434343434343</Dial>" +
                        "</Response>"));
    }

    @Test
    public void givenPostRequestToDialComplete_whenDialWasUnsuccessful_thenTwiMLForwardsToVoiceMail() throws Exception {
        mockMvc.perform(
                post("/twilio/inbound/dial_complete")
                        .param("AccountSid", ACCOUNT_SID)
                        .param("CallSid", CALLSID)
                        .param("From", FROM)
                        .param("To", TO)
                        .param("Direction", INBOUND)
                        .param("CallStatus", RINGING)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Response>" +
                        "<Redirect>/twilio/voicemail/</Redirect>" +
                        "</Response>"));
    }

    @Test
    public void givenPostRequestToDialComplete_whenDialWasSuccessfull_thenTwiMLHangsUp() throws Exception {
        mockMvc.perform(
                post("/twilio/inbound/dial_complete")
                        .param("AccountSid", ACCOUNT_SID)
                        .param("CallSid", CALLSID)
                        .param("From", FROM)
                        .param("To", TO)
                        .param("Direction", INBOUND)
                        .param("CallStatus", RINGING)
                        .param("DialCallStatus", "completed")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<Response>" +
                        "<Hangup/>" +
                        "</Response>"));
    }

    @NotNull
    private ResultActions postInboundCall() throws Exception {
        return mockMvc.perform(
                post("/twilio/inbound/")
                        .param("AccountSid", ACCOUNT_SID)
                        .param("CallSid", CALLSID)
                        .param("From", FROM)
                        .param("To", TO)
                        .param("Direction", INBOUND)
                        .param("CallStatus", RINGING)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print());
    }
}