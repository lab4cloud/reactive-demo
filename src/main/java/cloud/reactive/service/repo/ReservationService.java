package cloud.reactive.service.repo;

import cloud.reactive.service.entity.Reservation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final TransactionalOperator transactionalOperator;

    Flux<Reservation> saveAll(String... names) {
        return this.transactionalOperator.transactional(
                Flux.fromArray(names).map(name -> new Reservation(null, name))
                        .flatMap(this.reservationRepository::save)
                        .doOnNext(reservation -> Assert.isTrue(isValid(reservation),
                                "Name should start with Capital letter.")));
    }

    Flux<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    Mono<Void> deleteAll() {
        return reservationRepository.deleteAll();
    }

    private boolean isValid(Reservation reservation) {
        return Character.isUpperCase(reservation.getName().charAt(0));
    }
}
