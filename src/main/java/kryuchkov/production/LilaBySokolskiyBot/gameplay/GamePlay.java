package kryuchkov.production.LilaBySokolskiyBot.gameplay;

public class GamePlay {

    public static int makeAMove () {
        return (int)(Math.random() * ((6 -1) + 1)) + 1;
    }
}
