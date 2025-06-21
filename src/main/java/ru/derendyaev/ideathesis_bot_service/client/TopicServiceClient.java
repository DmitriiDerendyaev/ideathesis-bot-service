package ru.derendyaev.ideathesis_bot_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.dto.PaginatedResponse;
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

    public Mono<Void> selectTopic(String studentGuid, Long topicId, String supervisorGuid) {
        SelectTopicRequest request = new SelectTopicRequest();
        request.setTopicId(topicId);
        request.setSupervisorGuid(supervisorGuid);
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
        return getLastTenTopicsPaginated(studentGuid, 0, 10)
                .map(PaginatedResponse::getContent);
    }

    public Mono<PaginatedResponse<GeneratedTopicDto>> getLastTenTopicsPaginated(String studentGuid, int page, int size) {
        return topicWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/topics/history")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .header("X-Student-Guid", studentGuid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения истории тем")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<GeneratedTopicDto>>() {});
    }

    @Deprecated
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

    public Mono<List<StudentTopicSelectionDto>> getActiveTopicsForStudent(String studentGuid) {
        return getActiveTopicsForStudent(studentGuid, null);
    }

    public Mono<List<StudentTopicSelectionDto>> getActiveTopicsForStudent(String studentGuid, TopicStatus status) {
        return topicWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/topics/students/{studentGuid}/topics/active");
                    if (status != null) {
                        builder.queryParam("status", status.name());
                    }
                    return builder.build(studentGuid);
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения активных тем")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<List<StudentTopicSelectionDto>>() {});
    }

    public Mono<Void> withdrawTopic(Long topicId, String studentGuid) {
        return topicWebClient.post()
                .uri("/api/topics/topics/{topicId}/withdraw", topicId)
                .header("X-Student-Guid", studentGuid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка отзыва заявки")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(Void.class);
    }

    public Mono<GeneratedTopicDto> getTopic(Long topicId) {
        return topicWebClient.get()
                .uri("/api/topics/{topicId}", topicId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения темы")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(GeneratedTopicDto.class);
    }

    public Mono<Void> updateTopic(Long topicId, String studentGuid, UpdateTopicRequest request) {
        return topicWebClient.put()
                .uri("/api/topics/{topicId}", topicId)
                .header("X-Student-Guid", studentGuid)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка обновления темы")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(Void.class);
    }

    public Mono<PaginatedResponse<GeneratedTopicDto>> getGeneratedTopics(String studentGuid, int page, int size) {
        return topicWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/topics/students/{studentGuid}/topics/generated")
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build(studentGuid))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения сгенерированных тем")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<PaginatedResponse<GeneratedTopicDto>>() {});
    }

    public Mono<List<PendingTopicSelectionDto>> getPendingTopicsForTeacher(String teacherGuid) {
        return topicWebClient.get()
                .uri("/api/topics/teachers/{teacherGuid}/topics/pending", teacherGuid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения тем на проверку")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<List<PendingTopicSelectionDto>>() {});
    }

    public Mono<List<TopicCommentDto>> getCommentsForTopic(Long topicId) {
        return topicWebClient.get()
                .uri("/api/topics/{topicId}/comments", topicId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения комментариев")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<List<TopicCommentDto>>() {});
    }

    public Mono<TopicCommentDto> addComment(Long topicId, String studentGuid, AddCommentRequest request) {
        return topicWebClient.post()
                .uri("/api/topics/{topicId}/comments", topicId)
                .header("X-Student-Guid", studentGuid)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка добавления комментария")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("topic-service недоступен")))
                .bodyToMono(TopicCommentDto.class);
    }
}