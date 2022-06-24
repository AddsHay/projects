package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    private static class Pos {
        int x;
        int y;
        Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public Pos change(int oldx, int oldy) {
            return new Pos(oldx + this.x, oldy + this.y);
        }
    }

    public static void drawrow(TETile[][] tiles, Pos p, TETile tile, int length) {
        for (int track = 0; track < length; track++) {
            tiles[p.x + track][p.y] = tile;
        }
    }

    public static void drawworld(TETile[][] tiles) {
        fillWithNothing(tiles);
        Pos p = new Pos(3, 40);
        createhex(tiles, p, Tileset.AVATAR, 15);
    }

    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public static void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }


    public static void createhex(TETile[][] tiles, Pos p, TETile tile, int size) {
        if (size < 2) {
            return;
        }
        createhexhelper(tiles, p, tile, size - 1, size);

    }

    public static void createhexhelper(TETile[][] tiles, Pos p, TETile tile, int blank, int t) {
        Pos startofrow = p.change(blank, 0);
        drawrow(tiles, startofrow, tile, t);
        if (blank > 0) {
            Pos next = p.change(0, -1);
            createhexhelper(tiles, next, tile, blank - 1, t + 2);
        }
        Pos startofreflection = startofrow.change(0, - (2*blank + 1));
        drawrow(tiles, startofreflection, tile, t);

    }

    public static void main(String[] args) {
        TERenderer terenderer = new TERenderer();
        terenderer.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        drawworld(world);

        terenderer.renderFrame(world);
    }
}
