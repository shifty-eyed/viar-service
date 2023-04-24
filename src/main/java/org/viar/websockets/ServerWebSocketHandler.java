package org.viar.websockets;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.vecmath.Point3d;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.SubProtocolCapable;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.HtmlUtils;
import org.viar.core.TrackingListener;
import org.viar.core.model.MarkerNode;
import org.viar.core.model.MarkerRawPosition;

@Component
public class ServerWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable, TrackingListener {
    
    private static final Logger log = LoggerFactory.getLogger(ServerWebSocketHandler.class);
    
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New Client");
        sessions.add(session);
        
        TextMessage message = new TextMessage("one-time message from server");
        log.info("Server sends: {}", message);
        session.sendMessage(message);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Connection closed: {}", status);
        sessions.remove(session);
    }
    
    //@Scheduled(fixedRate = 10000)
    void sendPeriodicMessages() throws IOException {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                String broadcast = "server periodic message " + LocalTime.now();
                log.info("Server sends: {}", broadcast);
                session.sendMessage(new TextMessage(broadcast));
            }
        }
    }
    
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String request = message.getPayload();
        log.info("Received: {}", request);
        
        String response = String.format("response from server to '%s'", HtmlUtils.htmlEscape(request));
        log.info("Send: {}", response);
        session.sendMessage(new TextMessage(response));
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.info("Error: {}", exception.getMessage());
    }
    
    @Override
    public List<String> getSubProtocols() {
        return Collections.singletonList("subprotocol.demo.websocket");
    }

	@Override
	public void trackingUpdated(Map<String, Collection<MarkerRawPosition>> rawData, Map<MarkerNode, Point3d> resolved,
			long timeMillis) {
		
		for (WebSocketSession session : sessions) {
            if (session.isOpen() && !resolved.isEmpty()) {
            	var point = resolved.values().iterator().next();
                String broadcast = String.format("%.3f %.3f %.3f", point.x, point.y, point.z);
                log.info("Server sends: {}", broadcast);
                try {
					session.sendMessage(new TextMessage(broadcast));
				} catch (IOException e) {
					log.error("Message sending failed: {}", e.getMessage());
				}
            }
        }
		
	}
}
