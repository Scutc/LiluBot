package kryuchkov.production.LilaBySokolskiyBot.gameplay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GamePlayTest {

    @Test
    void makeAMoveTest() {
        GamePlay gamePlay = new GamePlay();
        int[] results = new int[6];
        int sum =0 ;
        int x;
        while (sum != 21) {
            sum = 0;
            for (int s: results) {
                sum+=s;
            }
            x = gamePlay.makeAMove();
            switch (x) {
                case 1:
                    results[0] = 1;
                    break;
                case 2:
                    results[1] = 2;
                    break;
                case 3:
                    results[2] = 3;
                    break;
                case 4:
                    results[3] = 4;
                    break;
                case 5:
                    results[4] = 5;
                    break;
                case 6:
                    results[5] = 6;
                    break;
            }
        }
        assertEquals(21, sum);
    }
}
