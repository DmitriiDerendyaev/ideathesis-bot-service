package ru.derendyaev.ideathesis_bot_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.exceptions.UnauthorizedException;
import ru.derendyaev.ideathesis_bot_service.models.AuthRequest;
import ru.derendyaev.ideathesis_bot_service.models.AuthResponse;

@Service
@RequiredArgsConstructor
public class AuthServiceClient {
    private final WebClient webClient;

    public Mono<AuthResponse> authenticate(String login, String password) {
        return webClient.post()
                .uri("/bot-login")
                .bodyValue(new AuthRequest(login, password))
                .retrieve()
                .onStatus(
                        status -> status == HttpStatus.SERVICE_UNAVAILABLE,
                        response -> Mono.error(new ServiceUnavailableException("Service Unavailable"))
                )
                .onStatus(
                        status -> status == HttpStatus.UNAUTHORIZED,
                        response -> Mono.error(new UnauthorizedException("Unauthorized"))
                )
                .bodyToMono(AuthResponse.class);
    }
}