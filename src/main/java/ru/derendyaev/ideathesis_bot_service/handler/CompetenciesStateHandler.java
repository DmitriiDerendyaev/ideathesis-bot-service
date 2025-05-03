//package ru.derendyaev.ideathesis_bot_service.handler;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.telegram.telegrambots.meta.api.objects.Message;
//import ru.derendyaev.ideathesis_bot_service.models.BotState;
//import ru.derendyaev.ideathesis_bot_service.services.AuthBot;
//import ru.derendyaev.ideathesis_bot_service.services.UserSessionData;
//import ru.derendyaev.ideathesis_bot_service.services.UserStateService;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class CompetenciesStateHandler implements StateHandler {
//    private final AuthBot bot;
//
//    @Override
//    public void handle(Message message, UserStateService stateService) {
//        Long chatId = message.getChatId();
//        UserSessionData sessionData = stateService.getSessionData(chatId);
//
//        List<String> competencies = Arrays.stream(message.getText().split(","))
//                .map(String::trim)
//                .filter(s -> !s.isEmpty())
//                .collect(Collectors.toList());
//
//        sessionData.setCompetencies(competencies);
//        stateService.setState(chatId, BotState.AWAITING_DOMAIN);
//        bot.sendDomainRequest(chatId);
//    }
//}