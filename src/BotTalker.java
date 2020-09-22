import java.io.IOException;
import java.util.*;

public class BotTalker {
    private static final String THEME_FILE_NAME = "keywords.txt";
    private static final String ADDITIONAL_FILE_NAME = "additionalFile.txt";
    private static final String ANSWER_FILE_NAME = "answers.txt";
    private static final String PRONOUNS_FILE_NAME = "pronouns.txt";

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
    private Map<String, List<String>> pronouns;

    public BotTalker(String userName) {
        this.userName = userName;
        lastPatternResponses = new LinkedList<>();
        responsesList = new ArrayList<>();
        reserveThemeResponse = new LinkedList<>();
        DataBaseReader uselessWordReader = new DataBaseReader(ADDITIONAL_FILE_NAME);
        DataBaseReader themeWordReader = new DataBaseReader(THEME_FILE_NAME);
        DataBaseReader answersPatternWordReader = new DataBaseReader(ANSWER_FILE_NAME);
        DataBaseReader pronounsWordReader = new DataBaseReader(PRONOUNS_FILE_NAME);
        try {
            additionalDB = uselessWordReader.readAllFileAsMap(";");
            uselessWords = additionalDB.get("useless");
            allThem = themeWordReader.readAllFileAsMap(",");
            answersPattern = answersPatternWordReader.readAllFileAsMap(";");
            pronouns = pronounsWordReader.readAllFileAsMap(",");
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
                }
            } else {
                addToListResponseOnKeyword(userMessage);
                addToListResponseOnWordPattern(userMessage);
            }

            checkResponseListOnIteration();
            if (responsesList.size() == 0)
                addToListResponseReserveTheme();

            checkResponseListOnIteration();
            if (responsesList.size() == 0)
                addToListAskingMoreInformation(userMessage);

            botResponse();
        } while (true);
    }

    private boolean isQuestion(String userMessage) {
        return userMessage.endsWith("?");
    }

    private boolean isSimpleQuestion(String userMessage) {
        List<String> yesNoQuestion = additionalDB.get("yesNoQuestion");
        String[] simple = {"is", "are", "am", "does", "do", "did", "will", "shall", "would"};
        String firstWord = firstWord(userMessage).toLowerCase();
        for (String str : yesNoQuestion)
            if (firstWord.equals(str))
                return true;
        return false;
    }

    private String detectQuestionType(String userMessage) {
        String firstWord = firstWord(userMessage).toLowerCase();
        List<String> answer = null;
    	if(firstWord.equals("where")) {
    		answer = additionalDB.get("where");
    		return answer.get(random.nextInt(answer.size())); 
    	}
    	else if(firstWord.equals("when")) {
    		answer = additionalDB.get("when");
    		return answer.get(random.nextInt(answer.size())); 
    	}
    	answer = additionalDB.get("ques");
    	return answer.get(random.nextInt(answer.size())); 
    }

    private String answerYesOrNo() {
        String[] Yes = {"Yes", "Definitely", "That's right", "Sure", "Of course"};
        String[] No = {"No", "Not really", "I don't think so", "I am afraid not"};
        boolean ans = random.nextBoolean();
        if (ans) return Yes[random.nextInt(Yes.length)];
        return No[random.nextInt(No.length)];
    }

    private String firstWord(String userMessage) {
        if (userMessage.contains(" "))
            return userMessage.substring(0, userMessage.indexOf(" "));
        return userMessage;
    }

    private void addToListResponseOnQuestion(String userMessage) {
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
            if (pronounsAll.get(0).equals("i"))
                responsesList.add(new Response("Why do you think you *", userMessage.replace("I ", "")));
            else
                responsesList.add(new Response("Why do you think *", userMessage));
        }
        pronounsAll.forEach(reserveThemeResponse::remove);
//        List<String> themes = findThemes(splitWords);
//        themes.forEach(reserveThemeResponse::remove);
//        findNormalKeywordsAnswers(themes);


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
        for (String word : splitWords) {
            for (String key : pronouns.keySet()) {
                if (pronouns.get(key).contains(word))
                    result.add(word);
            }
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
