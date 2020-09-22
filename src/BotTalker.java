import java.io.IOException;
import java.util.*;

public class BotTalker {
    //Names of txt files
    private static final String THEME_FILE_NAME = "keywords.txt";
    private static final String ADDITIONAL_FILE_NAME = "additionalFile.txt";
    private static final String ANSWER_FILE_NAME = "answers.txt";

    private static final int LAST_THEME_IN_QUEUE_SIZE = 7;
    private static final int RESERVE_THEME_IN_QUEUE_SIZE = 5;

    private static final Scanner in = new Scanner(System.in);
    private static final Random random = new Random();
    private List<Response> responsesList;
    private Queue<String> reserveThemeResponse;
    private Queue<String> lastPatternResponses;
    private String userName;

    //Information from DB
    private List<String> uselessWords;
    private Map<String, List<String>> allThem;
    private Map<String, List<String>> answersPattern;
    private Map<String, List<String>> additionalDB;

    public BotTalker(String userName) {
        this.userName = userName;
        lastPatternResponses = new LinkedList<>();
        responsesList = new ArrayList<>();
        reserveThemeResponse = new LinkedList<>();
        DataBaseReader uselessWordReader = new DataBaseReader(ADDITIONAL_FILE_NAME);
        DataBaseReader themeWordReader = new DataBaseReader(THEME_FILE_NAME);
        DataBaseReader answersPatternWordReader = new DataBaseReader(ANSWER_FILE_NAME);
        try {
            additionalDB = uselessWordReader.readAllFileAsMap(";");
            uselessWords = additionalDB.get("useless");
            allThem = themeWordReader.readAllFileAsMap(",");
            answersPattern = answersPatternWordReader.readAllFileAsMap(";");
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
            responsesList.clear();
            String userMessage = userInput();
            if (isQuestion(userMessage)) {
                if (isSimpleQuestion(userMessage))
                    answerYesOrNo();
                else {
                	detectQuestionType(userMessage);
                }
            } else {
                addToListResponseOnKeyword(userMessage);
                addToListResponseOnWordPattern(userMessage);
            }

            checkResponseListOnIteration();
            if (responsesList.size() == 0)
                addToListResponseReserveTheme();

            checkResponseListOnIteration();
            isGreeting(userMessage);
            if (isParting(userMessage)) {
                botResponse();
                return;
            }
            checkResponseListOnIteration();
            if (responsesList.size() == 0)
                addToListAskingMoreInformation(userMessage);
            botResponse();
        } while (true);
    }

    private void isGreeting(String userMessage) {
        String message = userMessage.toLowerCase();
        List<String> greetings = additionalDB.get("greeting");
        for (String str : greetings) {
            if (message.contains(str)) {
                responsesList.clear();
                responsesList.add(new Response(getRandomElementFromList(greetings)));
            }
        }
    }

    private boolean isParting(String userMessage) {
        String message = userMessage.toLowerCase();
        List<String> partings = additionalDB.get("parting");
        for (String str : partings) {
            if (message.contains(str)) {
                responsesList.clear();
                responsesList.add(new Response(getRandomElementFromList(partings)));
                return true;
            }
        }
        return false;
    }

    private boolean isQuestion(String userMessage) {
        return userMessage.endsWith("?");
    }
    // check if it is a simple question which starts with certain words
    private boolean isSimpleQuestion(String userMessage) {
        List<String> yesNoQuestion = additionalDB.get("yesNoQuestion");
        String firstWord = firstWord(userMessage).toLowerCase();
        for (String str : yesNoQuestion)
            if (firstWord.equals(str))
                return true;
        return false;
    }
    // detect type of the question 
    private void detectQuestionType(String userMessage) {
        String firstWord = firstWord(userMessage).toLowerCase();
        List<String> answer;
        if (firstWord.equals("where")) {
            answer = additionalDB.get("where");
        } else if (firstWord.equals("when")) {
            answer = additionalDB.get("when");
        }else {
        	answer = additionalDB.get("ques");
        }
        responsesList.add(new Response(getRandomElementFromList(answer)));
    }
    // answers randomly variations for yes and no 
    private void answerYesOrNo() {
        List<String> Yes = additionalDB.get("Yes");
        List<String> No = additionalDB.get("No");
        if (random.nextBoolean())
        	responsesList.add(new Response(getRandomElementFromList(Yes)));
        else responsesList.add(new Response(getRandomElementFromList(No)));
    }

    private String getRandomElementFromList(List<String> list) {
        return list.get(random.nextInt(list.size() - 1));
    }
    // return the first word of the string or the whole string
    private String firstWord(String userMessage) {
        if (userMessage.contains(" "))
            return userMessage.substring(0, userMessage.indexOf(" "));
        return userMessage;
    }

    private void addToListResponseOnKeyword(String userMessage) {
        List<String> splitWords = splitAndCleanMessage(userMessage);
        List<String> themes = findThemes(splitWords);
        themes.forEach(reserveThemeResponse::remove);
        findNormalKeywordsAnswers(themes);
    }

    private void addToListResponseOnWordPattern(String userMessage) {
        List<String> splitWords = splitAndCleanMessage(userMessage);
        List<String> pronounsAll = findPronouns(splitWords);
        if (pronounsAll.contains(splitWords.get(0))) {
            String localUserMessage = userMessage.replaceAll("I am","you are").replaceAll("I","you");
            responsesList.add(new Response(getRandomElementFromList(additionalDB.get("answersOnVerbs")), localUserMessage));
        }
        pronounsAll.forEach(reserveThemeResponse::remove);
    }

    private void addToListResponseReserveTheme() {
        checkResponseListOnIteration();
        findNormalKeywordsAnswers(new ArrayList<>(reserveThemeResponse));
    }

    private void botResponse() {
        checkResponseListOnIteration();
        Response answer = responsesList.get(random.nextInt(responsesList.size()));
        botOutput(answer);
        controlLastAndReserveResponses(answer);
    }

    private void checkResponseListOnIteration() {
        List<Response> suitableResponse = new ArrayList<>();
        responsesList.forEach(response -> {
            if (!lastPatternResponses.contains(response.getPattern()))
                suitableResponse.add(response);
        });
        responsesList = suitableResponse;
    }

    private void controlLastAndReserveResponses(Response answer) {
        lastPatternResponses.add(answer.getPattern());
        if (lastPatternResponses.size() == LAST_THEME_IN_QUEUE_SIZE)
            lastPatternResponses.remove();

        reserveThemeResponse.remove(answer.getAddInfo());
        if (reserveThemeResponse.size() == RESERVE_THEME_IN_QUEUE_SIZE)
            reserveThemeResponse.remove();
    }

    private void findNormalKeywordsAnswers(List<String> themes) {
        for (String theme : themes) {
            for (String answersKey : answersPattern.keySet()) {
                if (answersPattern.get(answersKey).contains(theme)) {
                    responsesList.add(new Response(answersKey, theme));
                }
            }
            reserveThemeResponse.add(theme);
        }
    }

    private void addToListAskingMoreInformation(String userMessage) {
        if (userMessage.length() < 8)
            addToResponseList(additionalDB.get("short message"));
        addToResponseList(additionalDB.get("ask more"));
    }

    private void addToResponseList(List<String> additionalList) {
        additionalList.forEach(pattern ->
                responsesList.add(new Response(pattern)));
    }

    private List<String> findPronouns(List<String> splitWords) {
        List<String> result = new ArrayList<>();
        List<String> pronouns = additionalDB.get("pronouns");
        for (String word : splitWords) {

            if (pronouns.contains(word))
                result.add(word);

        }
        return result;
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
        String[] splitMessage = userMessage.toLowerCase().replaceAll("[^a-z 0-9]", "").split(" ");
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

    private void botOutput(Response str) {
        System.out.println("Bot: " + str);
    }
}
