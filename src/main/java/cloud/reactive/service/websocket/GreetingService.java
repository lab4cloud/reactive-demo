package cloud.reactive.service.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

@Service
class GreetingService {
    @MessageMapping("/greetings")
    Flux<GreetingResponse> greet(GreetingRequest request) {
        return Flux.fromStream(Stream.generate(
                () -> new GreetingResponse("Hello " + request.getName() + " at " + Instant.now()))).delayElements(Duration.ofSeconds(1));
    }
}
