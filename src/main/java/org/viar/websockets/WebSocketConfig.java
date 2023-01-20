package org.viar.websockets;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

//@Configuration
//@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	
	static class LogHandshakeInterceptor implements HandshakeInterceptor {
		
		private Logger log;
		public LogHandshakeInterceptor(String name) {
			log = LoggerFactory.getLogger(name);
		}

		@Override
		public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
				WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
			log.info("New client: " + request.getURI().toString());
			return true;
		}

		@Override
		public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
				WebSocketHandler wsHandler, Exception exception) {
			//log.info("Disconnected client: " + request.getURI().toString());
		}
	}

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		//config.enableSimpleBroker("/player");
		//config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		registry.addEndpoint("/player").addInterceptors(new LogHandshakeInterceptor("PlayerService"));
		//registry.addEndpoint("/chat").addInterceptors(new LogHandshakeInterceptor("WebSocketService"));
		//registry.addEndpoint("/player").withSockJS().setInterceptors(new LogHandshakeInterceptor("WebSocketService2"));;
	}

}