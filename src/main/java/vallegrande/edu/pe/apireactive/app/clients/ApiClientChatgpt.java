package vallegrande.edu.pe.apireactive.app.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.apireactive.app.models.dto.ApiRequestDto;

import java.util.Map;

@Component
public class ApiClientChatgpt {

    private final WebClient webClient;
    private final String apiKey;

    public ApiClientChatgpt(@Value("${chatgpt.api.url}") String baseUrl,
                            @Value("${chatgpt.api.key}") String apiKey) {
        this.apiKey = apiKey;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-evocortexai-key", apiKey)  // Intentar como encabezado predeterminado
                .build();
    }
//    {
//        "model": "gpt-3.5-turbo",
//            "messages": [{"role": "user", "content": "hola!"}],
//        "stream": true
//    }


    public Mono<String> sendRequest(ApiRequestDto request) {
        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", request.getMessages(),
                "stream", true
        );
        return webClient.post()
                .header("x-evocortexai-key", apiKey)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new RuntimeException("Error en la API: " + response.statusCode() + ", " + errorBody)
                                )))
                .bodyToMono(String.class)
                .doOnSubscribe(subscription -> {
                    System.out.println("Enviando solicitud a Chatgpt API");
                    System.out.println("URL: " + webClient.get().uri("").retrieve().toString());
                    System.out.println("API Key: " + apiKey);
                });
    }
}
