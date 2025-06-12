package vallegrande.edu.pe.apireactive.app.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vallegrande.edu.pe.apireactive.app.clients.ApiClientChatgpt;
import vallegrande.edu.pe.apireactive.app.models.dto.ApiRequestDto;
import vallegrande.edu.pe.apireactive.app.models.entity.Chat;
import vallegrande.edu.pe.apireactive.app.repositories.ChatRepository;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class Chatgpt4Service {

    private final ApiClientChatgpt apiClientEvoCortexAI;
    private final ChatRepository chatRepository;

    public Chatgpt4Service(ApiClientChatgpt apiClientEvoCortexAI, ChatRepository chatRepository) {
        this.apiClientEvoCortexAI = apiClientEvoCortexAI;
        this.chatRepository = chatRepository;
    }

    public Mono<Map<String, Object>> processRequest(ApiRequestDto request) {
        String userRequest = "";
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ApiRequestDto.Message message : request.getMessages()) {
                if ("user".equals(message.getRole())) {
                    userRequest = message.getContent();
                    break;
                }
            }
        }

        final String finalUserRequest = userRequest;

        return apiClientEvoCortexAI.sendRequest(request)
                .flatMap(responseBody -> {
                    String responseBodyUtf8 = new String(responseBody.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    String[] lines = responseBodyUtf8.split("\n");
                    StringBuilder contentBuilder = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("data:") && !line.contains("[DONE]")) {
                            try {
                                String jsonText = line.substring(5).trim();
                                ObjectMapper mapper = new ObjectMapper();
                                JsonNode jsonNode = mapper.readTree(jsonText);
                                if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                                        jsonNode.get("choices").size() > 0) {
                                    JsonNode delta = jsonNode.get("choices").get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        contentBuilder.append(delta.get("content").asText());
                                    }
                                }
                            } catch (Exception e) {
                            }
                        }
                    }

                    String responseContent = contentBuilder.toString();
                    Map<String, Object> result = Map.of("content", responseContent);

                    Chat chat = new Chat();
                    chat.setRequestContent(finalUserRequest);
                    chat.setResponseContent(responseContent);
                    chat.setModel("gpt-4o");
                    chat.setCreatedAt(LocalDateTime.now());

                    return chatRepository.save(chat)
                            .thenReturn(result);
                });
    }


    public Flux<Chat> getAllChats() {
        return chatRepository.findByModelAndDeletedFalse("gpt-4o");
    }

    // Agrega este método en Chatgpt4Service
    public Mono<String> getResponseContent(ApiRequestDto request) {
        String userRequest = "";
        if (request.getMessages() != null && !request.getMessages().isEmpty()) {
            for (ApiRequestDto.Message message : request.getMessages()) {
                if ("user".equals(message.getRole())) {
                    userRequest = message.getContent();
                    break;
                }
            }
        }

        return apiClientEvoCortexAI.sendRequest(request)
                .map(responseBody -> {
                    String responseBodyUtf8 = new String(responseBody.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                    String[] lines = responseBodyUtf8.split("\n");
                    StringBuilder contentBuilder = new StringBuilder();
                    for (String line : lines) {
                        if (line.startsWith("data:") && !line.contains("[DONE]")) {
                            try {
                                String jsonText = line.substring(5).trim();
                                ObjectMapper mapper = new ObjectMapper();
                                JsonNode jsonNode = mapper.readTree(jsonText);
                                if (jsonNode.has("choices") && jsonNode.get("choices").isArray() &&
                                        jsonNode.get("choices").size() > 0) {
                                    JsonNode delta = jsonNode.get("choices").get(0).get("delta");
                                    if (delta != null && delta.has("content")) {
                                        contentBuilder.append(delta.get("content").asText());
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    }
                    return contentBuilder.toString();
                });
    }

    // Modifica updateChat para usar el método anterior
    public Mono<Chat> updateChat(Long id, ApiRequestDto request) {
        return chatRepository.findByIdAndDeletedFalse(id)
                .flatMap(existingChat ->
                        getResponseContent(request)
                                .map(responseContent -> {
                                    existingChat.setRequestContent(request.getMessages().get(0).getContent());
                                    existingChat.setResponseContent(responseContent);
                                    existingChat.setCreatedAt(LocalDateTime.now());
                                    return existingChat;
                                })
                )
                .flatMap(chatRepository::save);
    }

    public Mono<Void> deleteChat(Long id) {
        return chatRepository.findByIdAndDeletedFalse(id)
                .flatMap(chat -> {
                    chat.setDeleted(true);
                    return chatRepository.save(chat);
                })
                .then();
    }
}