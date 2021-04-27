package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.util.Objects;
import java.util.Random;

import java.awt.*;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    private final int WIDTH = 50;
    private final int HEIGHT = 50;
    RandomWorld object = new RandomWorld();
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.

    public void interactWithKeyboard() {
        reset();
        createmenu();
        menumode();
        StdDraw.enableDoubleBuffering();
        worldmode();
    }

    public void reset() {
        boolean exit = false;
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(Color.BLACK);
    }

    public void createmenu() {
        StdDraw.setPenColor(Color.WHITE);
        Font menusize = new Font("Times", Font.BOLD, 30);
        Font textsize = new Font("Times", Font.PLAIN, 15);
        StdDraw.setFont(menusize);
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "CS61B: THE GAME");
        StdDraw.setFont(textsize);
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 10, "New Game (n / N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 4.5 / 10, "Load Game (l / L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 10, "Quit Game (q / Q)");
    }

    public void menumode() {
        boolean exit = false;
        while (!exit) {
            if (StdDraw.hasNextKeyTyped()) {
                String c = Character.toString(Character.toUpperCase(StdDraw.nextKeyTyped()));
                switch (c) {
                    case("N"):
                        Random rand = new Random();
                        int random = rand.nextInt(1000000000);
                        String seed = "n" + random + "s";
                        interactWithInputString(seed);
                        exit = true;
                        break;
                    case("L"):
                        interactWithInputString("L");
                        exit = true;
                        break;
                    case("Q"):
                        interactWithInputString(":Q");
                        exit = true;
                        break;
                    default:
                        continue;
                }
            }
        }
    }

    public void worldmode() {
        boolean exit = true;
        while (exit) {
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                String c = Character.toString(Character.toUpperCase(ch));
                interactWithInputString(c);
                if (Objects.equals(ch, ":")) {
                    while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                            if (Objects.equals(StdDraw.nextKeyTyped(), "Q")) {
                                interactWithInputString(":Q");
                            }
                            break;
                        }
                    }
                }
                StdDraw.show();
            } else {
                exit = false;
            }
            break;
        }
    }



     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     * take off the na nd the s and then parse long of the remainder
     */
    public TETile[][] interactWithInputString(String input) {
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        String inputseed = null;
        String commands = null;
        String seed = null;
        if (Character.toString(input.charAt(0)).equals("n")
                || Character.toString(input.charAt(0)).equals("N")) {
            inputseed = input.substring(1);
            for (int i = 0; i < inputseed.length(); i++) {
                if (Character.isLetter(inputseed.charAt(i))) {
                    commands = inputseed.substring(i + 1);
                    seed = inputseed.substring(0, i);
                    i = inputseed.length();
                    break;
                }
            }
            object.drawbuild(finalWorldFrame, Tileset.WALL,
                    Tileset.FLOOR, Long.parseLong(seed));
            object.takeaction(finalWorldFrame, commands, Tileset.WALL,
                    Tileset.FLOOR, Tileset.AVATAR);
        } else {
            object.takeaction(finalWorldFrame, input, Tileset.WALL, Tileset.FLOOR, Tileset.AVATAR);
        }
        return finalWorldFrame;
    }
}
