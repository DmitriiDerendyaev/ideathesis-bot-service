package ru.derendyaev.ideathesis_bot_service.handler.messageHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ru.derendyaev.ideathesis_bot_service.client.TopicServiceClient;
import ru.derendyaev.ideathesis_bot_service.dto.topic.GenerateTopicRequest;
import ru.derendyaev.ideathesis_bot_service.handler.MessageHandler;
import ru.derendyaev.ideathesis_bot_service.models.user.UserSessionData;
import ru.derendyaev.ideathesis_bot_service.services.BotServiceDelegate;
import org.telegram.telegrambots.meta.api.objects.Message;
import ru.derendyaev.ideathesis_bot_service.models.ParseMode;

@Slf4j
@Component
public class DomainMessageHandler implements MessageHandler {

    private final BotServiceDelegate botServiceDelegate;
    private final TopicServiceClient topicServiceClient;

    @Autowired
    public DomainMessageHandler(@Lazy BotServiceDelegate botServiceDelegate, TopicServiceClient topicServiceClient) {
        this.botServiceDelegate = botServiceDelegate;
        this.topicServiceClient = topicServiceClient;
    }

    @Override
    public void handle(Message msg) {
        long chatId = msg.getChatId();
        String text = msg.getText().trim();

        UserSessionData session = botServiceDelegate.getUserStateService().getSessionData(chatId);
        session.setDomains(botServiceDelegate.splitList(text));

        String studentGuid = session.getAuth().getUser().getGuid();
        String competencies = String.join(",", session.getCompetencies());
        String areaOfStudy = String.join(",", session.getDomains()); // Предполагаем, что берём первую область как основную
        String educationLevel = "BACHELOR"; // Установим значение по умолчанию, можно уточнить позже

        GenerateTopicRequest request = new GenerateTopicRequest();
        request.setCompetencies(competencies);
        request.setAreaOfStudy(areaOfStudy);
        request.setEducationLevel(educationLevel);

        botServiceDelegate.sendMessage(chatId,
                String.format("Компетенции: %s\nОбласти: %s\nСпасибо! Подбираю темы...",
                        String.join(", ", session.getCompetencies()),
                        String.join(", ", session.getDomains())), ParseMode.HTML);

        topicServiceClient.generateTopics(studentGuid, request)
                .doOnNext(response -> {
                    StringBuilder topicsMessage = new StringBuilder("Сгенерированные темы:\n");
                    response.getTopics().forEach(topic -> {
                        topicsMessage.append(String.format(
                                "📌 *%s*\nОписание: %s\nАктуальность: %s\nПроблемы: %s\nРекомендуемые навыки: %s\n\n",
                                topic.getTitle(),
                                topic.getDescription(),
                                topic.getActuality(),
                                topic.getProblems(),
                                String.join(", ", topic.getRecommendedSkills())
                        ));
                    });
                    botServiceDelegate.sendMessage(chatId, topicsMessage.toString(), ParseMode.MARKDOWN);
                })
                .doOnError(ex -> {
                    log.error("Ошибка при генерации тем: {}", ex.getMessage());
                    botServiceDelegate.sendMessage(chatId, "Произошла ошибка при генерации тем. Попробуйте позже.", ParseMode.NONE);
                })
                .doOnSuccess(response -> botServiceDelegate.getUserStateService().clear(chatId))
                .subscribe();
    }
}