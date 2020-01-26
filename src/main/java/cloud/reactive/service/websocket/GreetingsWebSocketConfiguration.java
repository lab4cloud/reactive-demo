package cloud.reactive.service.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.Map;

@Configuration
class GreetingsWebSocketConfiguration {

    @Bean
    WebSocketHandler webSocketHandler(GreetingService greetingService) {
        return session -> {
            return session.send(session.receive().map(//-
                    wsMsg -> wsMsg.getPayloadAsText())
                    .map(GreetingRequest::new)
                    .flatMap(greetingService::greet)//-
                    .map(GreetingResponse::getMessage)
                    .map(session::textMessage));
        };

    }

    @Bean
    SimpleUrlHandlerMapping simpleUrlHandlerMapping(WebSocketHandler webSocketHandler) {
        return new SimpleUrlHandlerMapping(Map.of("/ws/greetings", webSocketHandler), 10);
    }

    @Bean
    WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
