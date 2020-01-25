package cloud.reservationservice;

import io.r2dbc.spi.ConnectionFactory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.annotation.Id;
import org.springframework.data.r2dbc.connectionfactory.R2dbcTransactionManager;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@SpringBootApplication
public class ReservationServiceApplication {

    @Bean
    ReactiveTransactionManager reactiveTransactionManager(ConnectionFactory cf) {
        return new R2dbcTransactionManager(cf);
    }

    @Bean
    TransactionalOperator transactionalOperator(ReactiveTransactionManager reactiveTransactionManager) {
        return TransactionalOperator.create(reactiveTransactionManager);
    }

    @Bean
    RouterFunction<ServerResponse> routes(ReservationRepository reservationRepository) {
        return route()
                .GET("/reservations",
                        request -> ok().body(reservationRepository.findAll(), Reservation.class)).build();
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

}

@Service
@RequiredArgsConstructor
class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TransactionalOperator transactionalOperator;

    Flux<Reservation> saveAll(String... names) {
        return this.transactionalOperator.transactional(
                Flux.fromArray(names).map(name -> new Reservation(null, name))
                        .flatMap(this.reservationRepository::save).doOnNext(reservation -> Assert.isTrue(isValid(reservation)
                        , "Name should start with Capital letter.")));
    }

    private boolean isValid(Reservation reservation) {
        return Character.isUpperCase(reservation.getName().charAt(0));
    }
}

@Log4j2
@Component
@RequiredArgsConstructor
class SampleDataInitializer {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {

        var saved = this.reservationService.saveAll("Best Friend", "Good Friend", "Other Good Friend");
        /*Flux.just("Best Friend", "Good Friend", "other Good Friend").map(name -> new Reservation(null,
                name)).flatMap(this.reservationRepository::save);**/
        this.reservationRepository
                .deleteAll()
                .thenMany(saved)
                .thenMany(this.reservationRepository.findAll())
                .subscribe(log::info);
    }
}

interface ReservationRepository extends ReactiveCrudRepository<Reservation, Integer> {
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Reservation {
    @Id
    private Integer id;
    private String name;
}