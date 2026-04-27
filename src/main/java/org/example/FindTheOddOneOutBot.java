package org.example;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class FindTheOddOneOutBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;
    private final SessionManager sessionManager;

    public FindTheOddOneOutBot(String botToken) {
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.sessionManager = new SessionManager();
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText().trim();
            String responseText = "";

            if (messageText.equalsIgnoreCase("/start")) {
                UserSession session = sessionManager.getSession(chatId);
                session.resetStats();

                responseText = "🎮 Добро пожаловать в игру 'Найди лишнее'!\n\n" +
                        session.getQuizGame().generateNewQuestion();

            } else if (messageText.equalsIgnoreCase("/stats")) {
                if (sessionManager.hasSession(chatId)) {
                    UserSession session = sessionManager.getSession(chatId);
                    responseText = String.format("📊 Ваша статистика:\n" +
                                    "Вопросов отвечено: %d\n" +
                                    "Правильных ответов: %d\n" +
                                    "Точность: %.1f%%",
                            session.getQuestionsAnswered(),
                            session.getScore(),
                            session.getAccuracy());
                } else {
                    responseText = "У вас пока нет активной сессии. Начните игру (/start)";
                }

            } else if (messageText.equalsIgnoreCase("/reset")) {
                sessionManager.removeSession(chatId);
                responseText = "Ваша сессия сброшена. Начните новую игру (/start)";

            } else {
                if (sessionManager.hasSession(chatId)) {
                    UserSession session = sessionManager.getSession(chatId);
                    String gameResponse = session.getQuizGame().checkAnswer(messageText);

                    if (gameResponse.startsWith("✅")) {
                        session.incrementScore();
                    }
                    session.incrementQuestionsAnswered();

                    responseText = gameResponse;
                } else {
                    responseText = "Вы не начали игру. Отправьте /start, чтобы начать.";
                }
            }

            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(responseText)
                    .build();

            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}