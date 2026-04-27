package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class QuizGame {

    private List<Question> questions = new ArrayList<>();
    private Question currentQuestion;
    private List<String> shuffledOptions;
    private boolean isQuestionActive = false;
    private Random random = new Random();

    private static class Question {
        String category;
        List<String> words;
        String correct;
        String explanation;

        Question(String category, List<String> words, String correct, String explanation) {
            this.category = category;
            this.words = words;
            this.correct = correct;
            this.explanation = explanation;
        }
    }

    public QuizGame() {
        loadQuestionsFromFile("questions.txt");
    }

    private void loadQuestionsFromFile(String filename) {
        InputStream inputStream = null;

        inputStream = getClass().getClassLoader().getResourceAsStream(filename);

        if (inputStream == null) {
            System.err.println("Файл " + filename + " не найден!");
            System.err.println("Проверьте, что файл лежит в: src/main/resources/" + filename);
            loadDefaultQuestions();
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;
            int loadedCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    Question question = parseLine(line);
                    if (question != null) {
                        questions.add(question);
                        loadedCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка в строке " + lineNumber + ": " + e.getMessage());
                }
            }

            System.out.println("Загружено " + loadedCount + " вопросов из " + filename);

            if (questions.isEmpty()) {
                System.err.println("Файл пуст. Использую вопросы по умолчанию.");
                loadDefaultQuestions();
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
            loadDefaultQuestions();
        }
    }

    private Question parseLine(String line) {
        String[] parts = line.split("\\|");

        if (parts.length < 2) {
            throw new IllegalArgumentException("Неверный формат. Ожидается: Категория:слова|ответ|[объяснение]");
        }

        String firstPart = parts[0];
        String[] categoryAndWords = firstPart.split(":", 2);

        if (categoryAndWords.length != 2) {
            throw new IllegalArgumentException("Не найдено разделение категории и слов через ':'");
        }

        String category = categoryAndWords[0].trim();
        String[] wordsArray = categoryAndWords[1].split(",");
        List<String> words = new ArrayList<>();

        for (String word : wordsArray) {
            words.add(word.trim());
        }

        String correct = parts[1].trim();
        String explanation = parts.length > 2 ? parts[2].trim() : "";

        return new Question(category, words, correct, explanation);
    }

    private void loadDefaultQuestions() {
        questions = Arrays.asList(
                new Question("Фрукты", Arrays.asList("Яблоко", "Груша", "Банан", "Морковь"), "Морковь", "Морковь - это овощ"),
                new Question("Овощи", Arrays.asList("Помидор", "Огурец", "Картофель", "Апельсин"), "Апельсин", "Апельсин - это фрукт"),
                new Question("Транспорт", Arrays.asList("Машина", "Автобус", "Самолёт", "Велосипед"), "Велосипед", "Велосипед не имеет двигателя"),
                new Question("Животные", Arrays.asList("Собака", "Кошка", "Тигр", "Стол"), "Стол", "Стол - это мебель"),
                new Question("Столицы", Arrays.asList("Москва", "Лондон", "Париж", "Нью-Йорк"), "Нью-Йорк", "Нью-Йорк не столица"),
                new Question("Языки программирования", Arrays.asList("Java", "Python", "C++", "HTML"), "HTML", "HTML - язык разметки")
        );
        System.out.println("Загружено " + questions.size() + " вопросов по умолчанию");
    }

    public String generateNewQuestion() {
        if (questions.isEmpty()) {
            return "База вопросов пуста.";
        }

        this.currentQuestion = questions.get(random.nextInt(questions.size()));
        this.shuffledOptions = new ArrayList<>(currentQuestion.words);
        Collections.shuffle(shuffledOptions);
        this.isQuestionActive = true;

        StringBuilder questionText = new StringBuilder();
        questionText.append("Категория: ").append(currentQuestion.category).append("\n\n");
        questionText.append("Найди лишнее слово:\n\n");

        for (int i = 0; i < shuffledOptions.size(); i++) {
            questionText.append(i + 1).append(". ").append(shuffledOptions.get(i)).append("\n");
        }

        questionText.append("\nОтветь номером (1-").append(shuffledOptions.size()).append(") или напиши слово");

        return questionText.toString();
    }

    public String checkAnswer(String userAnswerText) {
        if (!this.isQuestionActive || this.currentQuestion == null) {
            return "Сначала начни новую игру командой /start";
        }

        String trimmedAnswer = userAnswerText.trim();
        boolean isCorrect = false;

        try {
            int answerNumber = Integer.parseInt(trimmedAnswer);
            if (answerNumber >= 1 && answerNumber <= this.shuffledOptions.size()) {
                String selectedWord = this.shuffledOptions.get(answerNumber - 1);
                isCorrect = selectedWord.equalsIgnoreCase(this.currentQuestion.correct);
            }
        } catch (NumberFormatException e) {
            isCorrect = trimmedAnswer.equalsIgnoreCase(this.currentQuestion.correct);
        }

        this.isQuestionActive = false;

        StringBuilder resultMessage = new StringBuilder();

        if (isCorrect) {
            resultMessage.append("✅ Правильно!\n\n");
            resultMessage.append("Лишнее слово: ").append(this.currentQuestion.correct).append("\n");
            if (!this.currentQuestion.explanation.isEmpty()) {
                resultMessage.append("\n📖 ").append(this.currentQuestion.explanation);
            }
        } else {
            resultMessage.append("❌ Неправильно\n\n");
            resultMessage.append("🎯 Правильный ответ: ").append(this.currentQuestion.correct).append("\n");
            if (!this.currentQuestion.explanation.isEmpty()) {
                resultMessage.append("\n📖 ").append(this.currentQuestion.explanation);
            }
        }

        resultMessage.append("\n\n").append(generateNewQuestion());

        return resultMessage.toString();
    }
}