package vallegrande.edu.pe.apireactive.app.repositories;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.apireactive.app.models.entity.Chat;

public interface ChatRepository extends ReactiveCrudRepository<Chat, Long> {
    Flux<Chat> findAllByOrderByCreatedAtDesc();
    Flux<Chat> findByModelAndDeletedFalse(String model);
    Mono<Chat> findByIdAndDeletedFalse(Long id);
}