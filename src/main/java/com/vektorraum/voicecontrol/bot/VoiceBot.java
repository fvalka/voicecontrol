package com.vektorraum.voicecontrol.bot;

import com.vektorraum.voicecontrol.bot.config.BotConfig;
import com.vektorraum.voicecontrol.event.InboundCallEvent;
import com.vektorraum.voicecontrol.event.VoiceMailCalledEvent;
import com.vektorraum.voicecontrol.event.VoiceMailDownloadCompleteEvent;
import com.vektorraum.voicecontrol.event.VoiceMailTranscriptionsEvent;
import com.vektorraum.voicecontrol.model.Call;
import com.vektorraum.voicecontrol.model.routing.Action;
import com.vektorraum.voicecontrol.model.routing.InboundCallRouting;
import com.vektorraum.voicecontrol.service.routing.InboundCallRoutingTableManagementService;
import com.vektorraum.voicecontrol.service.tracking.CallTrackingService;
import com.vektorraum.voicecontrol.util.E164Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.stream.Collectors;

@Component
@Slf4j
public class VoiceBot extends AbilityBot {
    private static final String INCOMING_CALL_MESSAGE = "\uD83D\uDCF2 %s\nTo: %s\n**Incoming call**";
    private static final String VOICE_MAIL_CALLED = "\uD83D\uDCFC %s\n**Sent to voicemail**";
    private static final String VOICE_MAIL_AUDIO_CAPTION = "\uD83D\uDCFC %s **new voicemail**";
    private static final String SEND_AUDIO_ERROR = "Received voicemail from: %s but couldn't send the audio file!";
    private static final String TRANSCRIPTIONS_MESSAGE = "Transcription for voicemail from: %s\n%s";

    private BotConfig botConfig;
    private CallTrackingService callTrackingService;
    private InboundCallRoutingTableManagementService routingTableService;

    @Autowired
    public VoiceBot(BotConfig botConfig, CallTrackingService callTrackingService,
                    InboundCallRoutingTableManagementService routingTableService) {
        super(botConfig.getToken(), botConfig.getUsername());
        this.botConfig = botConfig;
        this.callTrackingService = callTrackingService;
        this.routingTableService = routingTableService;
    }

    @Override
    public int creatorId() {
        return (int) botConfig.getCreatorId();
    }

    public Ability listRoutes() {
        return Ability.builder()
                .name("routes")
                .info("lists all call routes")
                .privacy(Privacy.ADMIN)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    String routesFormatted = routingTableService.listRoutes().stream()
                            .map(route -> {
                                String result = "";
                                result += String.format("From: %s\n", route.getFrom());
                                result += String.format("To: %s\n", route.getTo());
                                result += String.format("Action: %s\n", route.getAction());
                                if (route.getAction() == Action.FORWARD) {
                                    result += String.format("Destination: %s\n", route.getDestination());
                                }
                                result += String.format("Priority: %s\n", route.getPriority());
                                result += String.format("Id: %s\n", route.getId());

                                return result;
                            }).collect(Collectors.joining("\n\n"));

                    if(!StringUtils.isEmpty(routesFormatted)) {
                        silent.send(routesFormatted, ctx.chatId());
                    } else {
                        silent.send("No routes set", ctx.chatId());
                    }
                })
                .build();
    }

    public Ability addRoutes() {
        return Ability.builder()
                .name("routeadd")
                .info("add route for a call")
                .privacy(Privacy.ADMIN)
                .locality(Locality.USER)
                .input(0)
                .action(ctx -> {
                    // from to action destination priority
                    if (ctx.arguments().length < 4 || ctx.arguments().length > 5) {
                        silent.send("To add a route please use the following format:\n" +
                                "<from> <to> <action> [destination] <priority>\n" +
                                "Where from, to and destination are numbers in E.164 format\n" +
                                "e.g. +431720495385", ctx.chatId());
                        return;
                    }

                    if (!E164Utils.isValid(ctx.arguments()[0]) || !E164Utils.isValid(ctx.arguments()[1])) {
                        silent.send("Please enter a number in E.164 format!\n" +
                                "e.g. +19494838245", ctx.chatId());
                        return;
                    }

                    Action action = EnumUtils.getEnumIgnoreCase(Action.class, ctx.arguments()[2]);
                    if (action == null) {
                        String validEnums = EnumUtils.getEnumList(Action.class).stream()
                                .map(Enum::toString)
                                .collect(Collectors.joining(", "));

                        silent.send("Please use one of the following action: " + validEnums, ctx.chatId());
                        return;
                    }

                    if (action == Action.FORWARD && ctx.arguments().length != 5) {
                        silent.send("Please provide a destination number in E.164 format when using forwarding", ctx.chatId());
                        return;
                    }

                    if (action == Action.FORWARD && !E164Utils.isValid(ctx.arguments()[3])) {
                        silent.send("Please provide the destination number in E.164 format.\n" +
                                "e.g. +12348848334", ctx.chatId());
                        return;
                    }

                    try {
                        int priority = Integer.parseInt(ctx.arguments()[4]);

                        InboundCallRouting routingTableEntry = InboundCallRouting.builder()
                                .from(ctx.arguments()[0])
                                .to(ctx.arguments()[1])
                                .action(action)
                                .priority(priority)
                                .build();

                        if (action == Action.FORWARD) {
                            routingTableEntry.setDestination(ctx.arguments()[3]);
                        }

                        routingTableService.add(routingTableEntry);
                    } catch (NumberFormatException nfe) {
                        silent.send("Please provide the priority as number.\n" +
                                "e.g. 100", ctx.chatId());
                    }
                })
                .build();
    }

    @EventListener
    public void onInboundCall(@NotNull InboundCallEvent event) {
        log.info("Bot received an inbound call event={}", event);
        sendToSubscribers(String.format(INCOMING_CALL_MESSAGE, event.getCall().getFrom(), event.getCall().getTo()));
    }

    @EventListener
    public void onVoiceMailCalled(@NotNull VoiceMailCalledEvent event) {
        log.info("Bot received voice mail called event={}", event);
        sendToSubscribers(String.format(VOICE_MAIL_CALLED, event.getCall().getFrom()));
    }

    @EventListener
    public void onVoiceMailDownloaded(@NotNull VoiceMailDownloadCompleteEvent event) {
        log.info("Bot received voice mail download complete event even={}", event);
        String from = findFromByCallSid(event.getCallSid());

        for (Integer admin : admins()) {
            SendAudio sendAudio = new SendAudio();
            sendAudio.setAudio(event.getFile());
            sendAudio.setCaption(String.format(VOICE_MAIL_AUDIO_CAPTION, from));
            sendAudio.setChatId(admin.longValue());
            sendAudio.setPerformer(from);

            try {
                sender.sendAudio(sendAudio);
            } catch (TelegramApiException e) {
                sendToSubscribers(String.format(SEND_AUDIO_ERROR, from));
                log.warn("Failed to send audio file for event={}", event, e);
            }
        }
    }

    @EventListener
    public void onVoiceMailTranscription(@NotNull VoiceMailTranscriptionsEvent event) {
        String from = findFromByCallSid(event.getCallSid());
        String transcriptions = event.getTranscriptions().stream()
                .map(it -> String.format("Confidence: %s\n%s", it.getConfidence(), it.getTranscription()))
                .collect(Collectors.joining("\n"));
        String message = String.format(TRANSCRIPTIONS_MESSAGE, from, transcriptions);
        sendToSubscribers(message);
    }

    @NotNull
    private String findFromByCallSid(String callSid) {
        return callTrackingService.findCall(callSid).map(Call::getFrom).orElse("UNKOWN");
    }

    private void sendToSubscribers(String message) {
        this.admins().forEach(admin -> silent.sendMd(message, admin));
    }
}
