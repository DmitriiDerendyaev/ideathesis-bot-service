package ru.derendyaev.ideathesis_bot_service.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.derendyaev.ideathesis_bot_service.dto.employee.EmployeeAllDto;
import ru.derendyaev.ideathesis_bot_service.exceptions.BadRequestException;
import ru.derendyaev.ideathesis_bot_service.exceptions.ServiceUnavailableException;
import ru.derendyaev.ideathesis_bot_service.models.student.StudentDetails;

import java.util.List;

@Service
public class UsersServiceClient {

    private final WebClient usersWebClient;

    // Явный конструктор с @Qualifier
    public UsersServiceClient(@Qualifier("usersWebClient") WebClient usersWebClient) {
        this.usersWebClient = usersWebClient;
    }

    public Mono<StudentDetails> getStudentDetails(String guid) {
        return usersWebClient.get()
                .uri("/api/students/{guid}", guid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения данных, something went wrong")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("user-service is unavailable")))
                .bodyToMono(StudentDetails.class);
    }

    public Mono<List<EmployeeAllDto>> getAllEmployees() {
        return usersWebClient.get()
                .uri("/api/employees")
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка получения списка сотрудников")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("user-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<List<EmployeeAllDto>>() {});
    }

    public Mono<EmployeeAllDto> getEmployeeById(String guid) {
        return usersWebClient.get()
                .uri("/api/employees/{guid}", guid)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Сотрудник не найден")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("user-service недоступен")))
                .bodyToMono(EmployeeAllDto.class).log();
    }

    public Mono<List<EmployeeAllDto>> searchEmployeesByFullName(String fullName) {
        return usersWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/employees/search")
                        .queryParam("fullName", fullName)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        response -> Mono.error(new BadRequestException("Ошибка поиска сотрудников")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ServiceUnavailableException("user-service недоступен")))
                .bodyToMono(new ParameterizedTypeReference<List<EmployeeAllDto>>() {});
    }
}
