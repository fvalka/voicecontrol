package com.vektorraum.voicecontrol.twilio;

import com.vektorraum.voicecontrol.model.routing.Action;
import com.vektorraum.voicecontrol.service.routing.InboundCallRoutingService;
import com.vektorraum.voicecontrol.service.routing.instruction.CallControlInstruction;
import com.vektorraum.voicecontrol.twilio.converters.HttpPostToCallConverter;
import com.vektorraum.voicecontrol.twilio.security.TwilioValidationFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@RunWith(SpringRunner.class)
@WebMvcTest(InboundCallRest.class)
public class InboundCallRestTest {
    @MockBean
    private ApplicationEventPublisher eventPublisher;

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

        mockMvc.perform(
                post("/twilio/inbound/")
                        .param("AccountSid", "xafaccountsid")
                        .param("CallSid", RandomStringUtils.randomAlphanumeric(34))
                        .param("From", "+4312345678")
                        .param("To", "+43987654321")
                        .param("Direction", "inbound")
                        .param("CallStatus", "ringing")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/xml;charset=ISO-8859-1"))
                .andExpect(content().xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><Redirect>/twilio/voicemail/</Redirect></Response>"));
    }
}