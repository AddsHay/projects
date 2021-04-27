package byow.Core;

import byow.InputDemo.InputSource;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    private final int WIDTH = 50;
    private final int HEIGHT = 50;
    RandomWorld object = new RandomWorld();
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        boolean exit = false;
        while (!exit) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                // If in game mode,
                //object.takeaction(tiles, c, walltile, floortile, avatartile);
                // Else if in menu mode,
                //set of whatever you do in the menu
            }
        }
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
        }
        if (Character.toString(input.charAt(0)).equals("L")
                || Character.toString(input.charAt(0)).equals("l")) {
            object.takeaction(finalWorldFrame, input, Tileset.WALL, Tileset.FLOOR, Tileset.AVATAR);
        }
        return finalWorldFrame;
    }
}
