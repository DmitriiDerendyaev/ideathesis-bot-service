package ru.derendyaev.ideathesis_bot_service.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;
import ru.derendyaev.ideathesis_bot_service.models.AuthRequest;
import ru.derendyaev.ideathesis_bot_service.dto.AuthResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceClient {

    private final WebClient webClient;

    public Mono<AuthResponse> authenticate(String login, String password) {
        return webClient.post()
                .uri("/api/v1/bot-login")
                .bodyValue(new AuthRequest(login, password))
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("Service Unavailable")))
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new UnauthorizedException("Unauthorized")))
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("Raw auth response: {}", body))
                .map(json -> {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        return mapper.readValue(json, AuthResponse.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}