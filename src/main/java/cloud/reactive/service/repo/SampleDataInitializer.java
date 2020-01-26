package cloud.reactive.service.repo;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class SampleDataInitializer {
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void ready() {

        this.reservationService
                .deleteAll()
                .thenMany(reservationService.saveAll("Best Friend", "Good Friend", "Other Good Friend"))
                .thenMany(reservationRepository.findAll())
                .subscribe(log::info);
    }
}
