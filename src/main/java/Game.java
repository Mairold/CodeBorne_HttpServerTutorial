import java.util.Random;

public class Game {
    public Integer randomNumber;
    public boolean gameStatus;
    public int gamesWonCounter = 0;
    public int gamesEndedWithoutWinCounter = 0;
    public int currentGameGuessCounter = 0;
    public int totalGuessCounter = 0;
    public int averageGuessesToWinGameCounter = 0;


    public Game() {
        this.randomNumber = new Random().nextInt(1, 101);
        ;
        this.gameStatus = true;
    }

    public void newGame() {
        this.randomNumber = new Random().nextInt(1, 101);
        ;
        this.gameStatus = true;
    }
}
