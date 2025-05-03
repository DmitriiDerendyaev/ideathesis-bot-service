//package ru.derendyaev.ideathesis_bot_service.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import ru.derendyaev.ideathesis_bot_service.handler.*;
//import ru.derendyaev.ideathesis_bot_service.models.BotState;
//
//import java.util.List;
//import java.util.Map;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Configuration
//public class BotConfig {
//    @Bean
//    public Map<BotState, StateHandler> stateHandlersMap(List<StateHandler> handlers) {
//        return handlers.stream().collect(Collectors.toMap(
//                handler -> getStateForHandler(handler.getClass()),
//                Function.identity()
//        ));
//    }
//
//    private BotState getStateForHandler(Class<?> handlerClass) {
//        if (handlerClass.equals(StartStateHandler.class)) return BotState.START;
//        if (handlerClass.equals(CredentialsStateHandler.class)) return BotState.AWAITING_CREDENTIALS;
//        if (handlerClass.equals(CompetenciesStateHandler.class)) return BotState.AWAITING_COMPETENCIES;
//        if (handlerClass.equals(DomainStateHandler.class)) return BotState.AWAITING_DOMAIN;
//        return BotState.START;
//    }
//}