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
    TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
    String seed = "";
    String cmds = "";
    String name = "";
    TETile avatartile = Tileset.AVATAR;
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        reset();
        createmenu();
        menumode();
        worldmode();
        HUD();
    }

    public void reset() {
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
        StdDraw.text(WIDTH / 2, HEIGHT * 5.5 / 10, "New Game (n / N)");
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 10, "Random Game (r / R)");
        StdDraw.text(WIDTH / 2, HEIGHT * 4.5 / 10, "Load Game (l / L)");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 10, "Quit Game (q / Q)");
        StdDraw.text(WIDTH / 2, HEIGHT * 3.5 / 10, "Switch avatar icon (p / P)");
        StdDraw.text(WIDTH / 2, HEIGHT * 3 / 10, "Set avatar name (b / B)");
    }

    public void seedscreen() {
        boolean exit = false;
        reset();
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "Please enter seed");
        while (!exit) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = (Character.toUpperCase(StdDraw.nextKeyTyped()));
                if (Character.isDigit(c)) {
                    seed += Character.toString(c);
                    reset();
                    StdDraw.setPenColor(Color.WHITE);
                    StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "Please enter seed");
                    StdDraw.text(WIDTH / 2, HEIGHT * 1 / 3, seed);
                }
                if (Character.toString(c).equals("S")) {
                    interactWithInputString("n" + seed + "s");
                    exit = true;
                }
            }
        }
    }

    public void avatarname() {
        boolean exit = false;
        reset();
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "Please enter a name");
        while (!exit) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = (StdDraw.nextKeyTyped());
                name += Character.toString(c);
                reset();
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "Please enter a name");
                StdDraw.text(WIDTH / 2, HEIGHT * 1 / 3, name);
                if (Character.toString(c).equals("0")) {
                    exit = true;
                }
            }
        }
        reset();
        createmenu();
    }

    public void avatarscreen() {
        boolean exit = false;
        reset();
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH / 2, HEIGHT * 2 / 3, "Select from the following Types for your Avatar");
        StdDraw.text(WIDTH / 2, HEIGHT * 5.5 / 10, "A: Avatar");
        StdDraw.text(WIDTH / 2, HEIGHT * 5 / 10, "B: Flower");
        StdDraw.text(WIDTH / 2, HEIGHT * 4.5 / 10, "C: Water");
        StdDraw.text(WIDTH / 2, HEIGHT * 4 / 10, "D: Mountain");
        StdDraw.text(WIDTH / 2, HEIGHT * 3.5 / 10, "E: Tree");
        while (!exit) {
            if (StdDraw.hasNextKeyTyped()) {
                String c = Character.toString(Character.toUpperCase(StdDraw.nextKeyTyped()));
                switch(c) {
                    case ("A"):
                        avatartile = Tileset.AVATAR;
                        exit = true;
                        break;
                    case ("B"):
                        avatartile = Tileset.FLOWER;
                        exit = true;
                        break;
                    case ("C"):
                        avatartile = Tileset.WATER;
                        exit = true;
                        break;
                    case ("D"):
                        avatartile = Tileset.MOUNTAIN;
                        exit = true;
                        break;
                    case ("E"):
                        avatartile = Tileset.TREE;
                        exit = true;
                        break;
                    default:
                }
            }
        }
        reset();
        createmenu();
    }


    public void menumode() {
        boolean exit = false;
        while (!exit) {
            if (StdDraw.hasNextKeyTyped()) {
                String c = Character.toString(Character.toUpperCase(StdDraw.nextKeyTyped()));
                switch (c) {
                    case("R"):
                        ter.initialize(WIDTH, HEIGHT + 2);
                        Random rand = new Random();
                        int random = rand.nextInt(1000000000);
                        String seed = "n" + random + "s";
                        interactWithInputString(seed);
                        exit = true;
                        break;
                    case("N"):
                        ter.initialize(WIDTH, HEIGHT + 2);
                        seedscreen();
                        exit = true;
                        break;
                    case("L"):
                        ter.initialize(WIDTH, HEIGHT + 2);
                        interactWithInputString("L");
                        exit = true;
                        break;
                    case("Q"):
                        interactWithInputString(":Q");
                        exit = true;
                        break;
                    case ("P"):
                        avatarscreen();
                        break;
                    case("B"):
                        avatarname();
                        break;
                    default:
                        continue;
                }
            }
        }
    }

    public void worldmode() {
        object.generatelights(finalWorldFrame);
        boolean exit = true;
        while (exit) {
            int mx = (int)StdDraw.mouseX();
            int my = (int)StdDraw.mouseY();
            if (mx > 49) {
                mx = 49;
            }
            if (mx < 1) {
                mx = 1;
            }
            if (my > 49) {
                my = 49;
            }
            if (my < 1) {
                my = 1;
            }
            TETile hovering = finalWorldFrame[mx][my];
            StdDraw.textLeft(WIDTH / 10, HEIGHT - 2, name);
            if (Tileset.WALL.equals(hovering)) {
                ter.renderFrame(finalWorldFrame);
                StdDraw.enableDoubleBuffering();
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.textLeft(WIDTH / 10, HEIGHT - 1, "Wall");
            } else if (Tileset.NOTHING.equals(hovering)) {
                ter.renderFrame(finalWorldFrame);
                StdDraw.enableDoubleBuffering();
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.textLeft(WIDTH / 10, HEIGHT - 1, "Nothing");
            } else if (avatartile.equals(hovering)) {
                ter.renderFrame(finalWorldFrame);
                StdDraw.enableDoubleBuffering();
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.textLeft(WIDTH / 10, HEIGHT - 1, "You!");
            } else if (Tileset.FLOOR.equals(hovering)) {
                ter.renderFrame(finalWorldFrame);
                StdDraw.enableDoubleBuffering();
                StdDraw.setPenColor(Color.WHITE);
                StdDraw.textLeft(WIDTH / 10, HEIGHT - 1, "Floor");
            }
            StdDraw.show();
            if (StdDraw.hasNextKeyTyped()) {
                char ch = StdDraw.nextKeyTyped();
                String c = Character.toString(Character.toUpperCase(ch));
                if (Character.toString(ch).equals(":")) {
                    while (true) {
                        if (StdDraw.hasNextKeyTyped()) {
                            if (Character.toString(Character.toUpperCase(StdDraw.nextKeyTyped())).equals("Q")) {
                                interactWithInputString(cmds + ":Q");
                                System.exit(0);
                            }
                            break;
                        }
                    }
                } else if (Character.toString(ch).equals("L")) {
                    interactWithInputString(cmds);
                } else {
                    cmds += c;
                    ter.renderFrame(finalWorldFrame);
                    StdDraw.enableDoubleBuffering();
                    interactWithInputString(c);
                    StdDraw.show();
                }
            }
        }
    }

    public void HUD() {

    }


    /**
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
                    Tileset.FLOOR, avatartile);
        } else {
            object.takeaction(finalWorldFrame, input, Tileset.WALL, Tileset.FLOOR, avatartile);
        }
        ter.renderFrame(finalWorldFrame);
        return finalWorldFrame;
    }
}
