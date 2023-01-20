package org.viar.websockets;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

//@Controller
public class ChatController {
	
	static Logger log = LoggerFactory.getLogger("WebSocketService");
	
	private final SimpMessagingTemplate simpMessagingTemplate;
    
    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Scheduled(fixedRate = 10000)
    //@SendTo("/player")
    public void sendMessage() {
        final String time = "Checking time " + new SimpleDateFormat("HH:mm:ss").format(new Date());
        log.info("Tick message: " + time);
        simpMessagingTemplate.convertAndSend("/player", "Checking time " + time);
    }

    @MessageMapping("/player")
    @SendTo("/player")
    public String send(final String message) throws Exception {
        final String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        log.info("Reacting message: " + time);
        return message + " received at" + time;
    }

}
