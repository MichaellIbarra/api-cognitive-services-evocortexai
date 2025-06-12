package vallegrande.edu.pe.apireactive.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.apireactive.app.models.dto.ApiRequestDto;
import vallegrande.edu.pe.apireactive.app.models.entity.Chat;
import vallegrande.edu.pe.apireactive.app.services.llamaService;

import java.util.Map;


@RestController
@RequestMapping("/api/v1/llama")
public class llamaRest {
    private final llamaService service;

    public llamaRest(llamaService service) {
        this.service = service;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> consumeApi(@RequestBody ApiRequestDto request) {
        return service.processRequest(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/chats")
    public Flux<Chat> getAllChats() {
        return service.getAllChats();
    }

    @PutMapping("/chats/{id}")
    public Mono<ResponseEntity<Chat>> updateChat(@PathVariable Long id, @RequestBody ApiRequestDto request) {
        return service.updateChat(id, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chats/{id}")
    public Mono<ResponseEntity<Void>> deleteChat(@PathVariable Long id) {
        return service.deleteChat(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}