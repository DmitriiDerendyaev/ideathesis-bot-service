package ru.derendyaev.ideathesis_bot_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.dto.topic.*;
import ru.derendyaev.ideathesis_bot_service.exceptions.BadRequestException;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;

import java.util.List;

@Service
public class TopicServiceClient {

    private final WebClient topicWebClient;

    public TopicServiceClient(@Qualifier("topicWebClient") WebClient topicWebClient) {
        this.topicWebClient = topicWebClient;
    }

    public Mono<GenerateTopicResponse> generateTopics(String studentGuid, GenerateTopicRequest request) {
        return topicWebClient.post()
                .uri("/api/topics/generate")
                .header("X-Student-Guid", studentGuid)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка генерации тем")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(GenerateTopicResponse.class);
    }

    public Mono<Void> selectTopic(String studentGuid, Long topicId) {
        SelectTopicRequest request = new SelectTopicRequest();
        request.setTopicId(topicId);
        return topicWebClient.post()
                .uri("/api/topics/select")
                .header("X-Student-Guid", studentGuid)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка при выборе темы")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(Void.class);
    }

    public Mono<List<GeneratedTopicDto>> getLastTenTopics(String studentGuid) {
        return topicWebClient.get()
                .uri("/api/topics/history")
                .header("X-Student-Guid", studentGuid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения истории тем")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<Void> updateTopicStatus(String studentGuid, TopicStatusUpdateRequest request) {
        return topicWebClient.post()
                .uri("/api/topics/status")
                .header("X-Student-Guid", studentGuid)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка обновления статуса")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(Void.class);
    }
}