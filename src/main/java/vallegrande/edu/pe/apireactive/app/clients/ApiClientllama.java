package vallegrande.edu.pe.apireactive.app.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.apireactive.app.models.dto.ApiRequestDto;

@Component
public class ApiClientllama {

    private final WebClient webClient;
    private final String apiKey;

    public ApiClientllama(@Value("${llama.api.url}") String baseUrl,
                          @Value("${llama.api.key}") String apiKey) {
        this.apiKey = apiKey;
        // Configuración detallada del WebClient
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-evocortexai-key", apiKey)  // Intentar como encabezado predeterminado
                .build();
    }

    public Mono<String> sendRequest(ApiRequestDto request) {
        return webClient.post()
                // Asegurar que se envía en cada solicitud también
                .header("x-evocortexai-key", apiKey)
                .bodyValue(request)
                .retrieve()  // Usar retrieve en lugar de exchangeToMono para simplificar
                .onStatus(status -> !status.is2xxSuccessful(),
                        response -> response.bodyToMono(String.class)
                                .flatMap(errorBody -> Mono.error(
                                        new RuntimeException("Error en la API: " + response.statusCode() + ", " + errorBody)
                                )))
                .bodyToMono(String.class)
                .doOnSubscribe(subscription -> {
                    System.out.println("Enviando solicitud a LLaMA API");
                    System.out.println("URL: " + webClient.get().uri("").retrieve().toString());
                    System.out.println("API Key: " + apiKey);
                });
    }
}