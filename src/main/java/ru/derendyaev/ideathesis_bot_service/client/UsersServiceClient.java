package ru.derendyaev.ideathesis_bot_service.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.models.StudentDetails;

@Service
@RequiredArgsConstructor
public class UsersServiceClient {

    private final WebClient usersWebClient;

    public Mono<StudentDetails> getStudentDetails(String guid) {
        return usersWebClient.get()
                .uri("/api/students/{guid}", guid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new RuntimeException("Ошибка получения данных")))
                .bodyToMono(StudentDetails.class);
    }
}
