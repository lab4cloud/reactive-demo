package cloud.reactive.service.websocket;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;

import static java.util.stream.Stream.generate;
import static reactor.core.publisher.Flux.fromStream;

@Service
class GreetingService {
    @MessageMapping("/greetings")
    Flux<GreetingResponse> greet(GreetingRequest request) {
        return fromStream(generate(
                () -> new GreetingResponse("Hello " + request.getName() + " at " + Instant.now()))).delayElements(Duration.ofSeconds(1));
    }
}
