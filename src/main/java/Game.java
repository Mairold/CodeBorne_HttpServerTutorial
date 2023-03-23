import java.util.Random;

public class Game {
    private Integer randomNumber;
    private boolean gameStatus;
    private int gamesWonCounter = 0;
    private int gamesEndedWithoutWinCounter = 0;
    private int currentGameGuessCounter = 0;
    private int totalGuessCounter = 0;
    private int averageGuessesToWinGameCounter = 0;


    public Game() {
        this.randomNumber = new Random().nextInt(1, 101);;
        this.gameStatus = true;
    }
}
