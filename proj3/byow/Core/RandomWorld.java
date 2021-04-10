package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

public class RandomWorld {
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
    public static void createbox(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dx, int dy) {
        drawrow(tiles, p.x, p.y, walltile, dx);
        drawrow(tiles, p.x, p.y + dy - 1, walltile, dx);
        drawcolumn(tiles, p.x, p.y, walltile, dy);
        drawcolumn(tiles, p.x + dx - 1, p.y, walltile, dy);
        fillroom(tiles, p.x + 1, p.y + 1, floortile, dx - 2, dy - 2);
    }
    public static void drawrow(TETile[][] tiles, int x, int y, TETile walltile, int dx) {
        if (dx > 0) {
            tiles[x][y] = walltile;
            drawrow(tiles, x + 1, y, walltile, dx - 1);
        }
    }
    public static void drawcolumn(TETile[][] tiles, int x, int y, TETile walltile, int dy) {
        if (dy > 0) {
            tiles[x][y] = walltile;
            drawcolumn(tiles, x, y + 1, walltile, dy - 1);
        }
    }
    public static void fillroom(TETile[][] tiles, int x, int y, TETile floortile, int dx, int dy) {
        if (dy > 0) {
            drawrow(tiles, x, y, floortile, dx);
            fillroom(tiles, x, y + 1, floortile, dy - 1);
        }
    }
    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.NOTHING;
            default: return Tileset.NOTHING;
        }
    }
    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] randomTiles = new TETile[WIDTH][HEIGHT];
        fillWithRandomTiles(randomTiles);
        Pos p = new Pos(10, 10);
        createbox(randomTiles, Tileset.WALL, Tileset.FLOOR, p, 30, 30);

        ter.renderFrame(randomTiles);
    }

}
