import java.io.IOException;
import java.util.*;

public class BotTalker {
    Scanner in = new Scanner(System.in);
    String userName;

    Random random = new Random();
    List<String> uselessWords, lastTheme;
    Map<String, List<String>> allThem;
    Map<String, List<String>> answersPattern;

    public BotTalker(String userName, String themeDBName, String uselessWordDBName, String answersDBName) {
        this.userName = userName;
        lastTheme = new ArrayList<>();
        DataBaseReader uselessWordReader = new DataBaseReader(uselessWordDBName);
        DataBaseReader themeWordReader = new DataBaseReader(themeDBName);
        DataBaseReader answersPatternWordReader = new DataBaseReader(answersDBName);
        try {
            uselessWords = uselessWordReader.readAllFile(",").get("useless");
            allThem = themeWordReader.readAllFile(",");
            answersPattern = answersPatternWordReader.readAllFile("_");
        } catch (IOException e) {
            System.out.println("We cannot find database file");
        } finally {
            uselessWordReader.close();
            themeWordReader.close();
            answersPatternWordReader.close();
        }
    }

    public void startDialog() {
        do {
            String userMessage = userInput();
            if (yesNoQuestion(userMessage))  // Если на вопрос можно ответить да или нет
                continue;
            if (searchKeyWord(userMessage))  // Пытаемся найти ключевое слово
                continue;
            if (searchVerbWord(userMessage)) //Пытаемся найти глагол чтобы его как-то использовать
                continue;

            askMoreInformation(userMessage);//Просим больше информации чтобы что-то сказать в следующий раз
        } while (true);
    }

    private boolean yesNoQuestion(String userMessage) {
        return false;
    }

    private boolean searchKeyWord(String userMessage) {
        List<String> splitWords = splitAndCleanMessage(userMessage);
        List<String> themes = findThemes(splitWords);
        if (themes.size() == 0)
            return false;
        List<String> answers = findNormalAnswers(themes);
        printRandomAnswer(answers);
        return true;
    }

    private void printRandomAnswer(List<String> answers) {
        int randomIndex = random.nextInt(answers.size());
        System.out.println("bot: " + answers.get(randomIndex));
    }

    private List<String> findNormalAnswers(List<String> themes) {
        List<String> result = new ArrayList<>();
        for (String theme : themes) {
            for (String answersKey : answersPattern.keySet()) {
                if (answersPattern.get(answersKey).contains(theme))
                    result.add(putThemeToPattern(answersKey, theme));
            }
        }
        return result;
    }

    private String putThemeToPattern(String answersPattern, String theme) {
        return answersPattern.replace("*", theme);
    }

    private boolean searchVerbWord(String userMessage) {
        return false;
    }

    private void askMoreInformation(String userMessage) {
    }


    private List<String> findThemes(List<String> splitWords) {
        List<String> result = new ArrayList<>();
        for (String word : splitWords) {
            for (String key : allThem.keySet()) {
                if (allThem.get(key).contains(word))
                    result.add(key);
            }
        }
        return result;
    }

    private List<String> splitAndCleanMessage(String userMessage) {
        List<String> result = new ArrayList<>();
        String[] splitMessage = userMessage.toLowerCase().replaceAll("[^a-zA-Z0-9]", "").split(" ");
        for (String word : splitMessage) {
            if (!uselessWords.contains(word))
                result.add(word);
        }
        return result;
    }

    private String userInput() {
        System.out.print(userName + ": ");
        return in.nextLine();
    }
}
