import java.io.IOException;
import java.util.*;

public class BotTalker {
    Scanner in = new Scanner(System.in);
    String userName;

    List<String> uselessWords, lastTheme;
    Map<String, List<String>> allThem;
    public BotTalker(String userName, String themeDBName, String uselessWordDBName) {
        this.userName = userName;
        lastTheme = new ArrayList<>();
        DataBaseReader uselessWordReader = new DataBaseReader(uselessWordDBName);
        DataBaseReader themeWordReader = new DataBaseReader(themeDBName);
        try {
            uselessWords = uselessWordReader.readAllFile().get("useless");
            allThem = themeWordReader.readAllFile();
        } catch (IOException e) {
            System.out.println("We cannot find uselessWord database file");
        }

    }

    public void startDialog() {
        do {
            String userMessage = userInput();
            if(processingMessage(userMessage))
                continue;
            List<String> splitWords = splitAndCleanMessage(userMessage);
            List<String> theme = findThemes(splitWords);
        } while (true);
    }

    private List<String> findThemes(List<String> splitWords) {
        List<String> result = new ArrayList<>();
        for(String word: splitWords){
            for(String key: allThem.keySet()){
               if(allThem.get(key).contains(word))
                   result.add(key);
            }
        }
        return result;
    }

    private boolean processingMessage(String userMessage) { //Если это вопрос да\нет или я уже не помню
        return false;
    }

    private List<String> splitAndCleanMessage(String userMessage) {
        List<String> result = new ArrayList<>();
        String[] splitMessage = userMessage.toLowerCase().split(" ");
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
