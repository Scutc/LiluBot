package kryuchkov.production.LilaBySokolskiyBot.service;

import kryuchkov.production.LilaBySokolskiyBot.config.BotConfig;
import kryuchkov.production.LilaBySokolskiyBot.model.User;
import kryuchkov.production.LilaBySokolskiyBot.model.UserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import kryuchkov.production.LilaBySokolskiyBot.gameplay.*;

import java.util.*;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;


    private Map<Long, ArrayList<Integer>> usersCurrentSteps;

    final BotConfig config;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Начать игру"));
        listOfCommands.add(new BotCommand("/play", "Бросить кубик"));
        listOfCommands.add(new BotCommand("/history", "Посмотреть историю игры"));
        listOfCommands.add(new BotCommand("/end", "Завершить игру"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка создани меню бота: " + e.getMessage());
        }
        usersCurrentSteps = new HashMap<>();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandRecieved(chatId, update.getMessage().getChat().getFirstName());
                    log.info("Выполнена команда start пользователем: " + chatId);
                    break;
                case "/play":
                    makeAMove(chatId);
                    break;
                default:
                    sendMessage(chatId, "Неизвестная команда");
                    log.info("Запрошена неизвестная команда пользователем: " + chatId);
            }
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {

            var chatId = message.getChatId();
            var chat = message.getChat();

            User user = new User();

            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setUserName(chat.getUserName());

            userRepository.save(user);
            usersCurrentSteps.put(chatId, new ArrayList<>());
            log.info("Сохранен пользователь: " + user);
        } else {
            Long chatId = message.getChatId();
            usersCurrentSteps.put(chatId, new ArrayList<>());
        }
    }

    private void startCommandRecieved(long chatId, String firstName) {
        String answer = "Привет " + firstName + ", добро пожаловать в игру Лилу!";
        sendMessage(chatId, answer);
        log.info("Отправлен ответ пользователя " + firstName);

    }

    private void makeAMove (long chatId) {
        int x = GamePlay.makeAMove();

        String answer;
        if (x == 6) {
            usersCurrentSteps.get(chatId).add(x);
            answer = "Выпала цифра 6. Вам нужно сделать еще один бросок. Текущая серия бросков: "
                    + usersCurrentSteps.get(chatId) ;
        } else {
            usersCurrentSteps.get(chatId).add(x);
            answer = "Выпала цифра: " + x + ". Вы делаете следующий ход: " + usersCurrentSteps.get(chatId);
            usersCurrentSteps.put(chatId, new ArrayList<>());
        }
        sendMessage(chatId, answer);
        log.info("Пользователь " + chatId + "сделал бросок. Выпала цифра: " + x);
    }


    @SneakyThrows
    private void sendMessage (long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        try {
            execute(message);
            log.info("Пользователь " + chatId + " получил сообщение: " + message);
        } catch (TelegramApiException e) {
            log.error("Произошла ошибка: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
