import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Welcome to our bot talker.\n");
        String userName = readUserName();
        BotTalker mainBotTalker = new BotTalker(userName);
        mainBotTalker.startDialog();
    }

    private static String readUserName() {
        Scanner in = new Scanner(System.in);
        System.out.print("Write your name: ");
        return in.next();
    }
}
