package org.example;

public class UserSession {

    private final long chatId;
    private final QuizGame quizGame;
    private int score;
    private int questionsAnswered;

    public UserSession(long chatId) {
        this.chatId = chatId;
        this.quizGame = new QuizGame(); // Создаём новую игру для этого пользователя
        this.score = 0;
        this.questionsAnswered = 0;
    }

    public long getChatId() {
        return chatId;
    }

    public QuizGame getQuizGame() {
        return quizGame;
    }

    public void incrementScore() {
        this.score++;
    }

    public void incrementQuestionsAnswered() {
        this.questionsAnswered++;
    }

    public int getScore() {
        return score;
    }

    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    public double getAccuracy() {
        if (questionsAnswered == 0) return 0.0;
        return (double) score / questionsAnswered * 100;
    }

    public void resetStats() {
        this.score = 0;
        this.questionsAnswered = 0;
    }
}