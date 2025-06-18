package vallegrande.edu.pe.apireactive.app.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.apireactive.app.models.dto.ApiRequestDto;
import vallegrande.edu.pe.apireactive.app.models.entity.Chat;
import vallegrande.edu.pe.apireactive.app.services.Chatgpt4Service;

import java.util.Map;
@CrossOrigin("*")
@RestController
@RequestMapping("/api/v1/chatgpt4")
public class Chatgpt4Rest {
    private final Chatgpt4Service chatgpt4Service;

    public Chatgpt4Rest(Chatgpt4Service chatgpt4Service) {
        this.chatgpt4Service = chatgpt4Service;
    }

    @PostMapping
    public Mono<ResponseEntity<Map<String, Object>>> consumeApi(@RequestBody ApiRequestDto request) {
        return chatgpt4Service.processRequest(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/chats")
    public Flux<Chat> getAllChats() {
        return chatgpt4Service.getAllChats();
    }

    @PutMapping("/chats/{id}")
    public Mono<ResponseEntity<Chat>> updateChat(@PathVariable Long id, @RequestBody ApiRequestDto request) {
        return chatgpt4Service.updateChat(id, request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chats/{id}")
    public Mono<ResponseEntity<Void>> deleteChat(@PathVariable Long id) {
        return chatgpt4Service.deleteChat(id)
                .thenReturn(ResponseEntity.noContent().build());
    }
}