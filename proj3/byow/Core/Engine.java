package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.lang.String;

public class Engine {
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 50;
    public static final int HEIGHT = 50;
    RandomWorld object = new RandomWorld();
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
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
        for (int i = 0; i < input.length(); i++) {
            if (Character.isLetter(input.charAt(i))) {
                inputseed = input.substring(0, i) + input.substring(i + 1);
                for (int x = 0; x < inputseed.length(); x++) {
                    if (Character.isLetter(inputseed.charAt(x))) {
                        inputseed = inputseed.substring(0, x) + inputseed.substring(x + 1);
                    }
                }
            }
        }
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        object.drawbuild(finalWorldFrame, Tileset.WALL, Tileset.FLOOR, Long.parseLong(inputseed));
        return finalWorldFrame;
    }
}
