package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

public class RandomWorld {

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

        public static void drawoutline(TETile[][] tiles, Pos p, TETile walltile, int dx, int dy) {
            for ()
            tiles[p.x + dx][p.y] = walltile;
            tiles[p.x][p.y +dy] = walltile;
            tiles[]

        }

        public static void drawrow(TETile[][] tiles, Pos p, TETile walltile, int dx) {
            if (dx > 0) {
                tiles[p.x][p.y] = walltile;
                p.x += 1;
                drawrow(tiles, p, walltile, dx - 1);
            }
        }

        public static void createbox(TETile walltile, TETile floortile, Pos p, int dx, int dy) {

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

            ter.renderFrame(randomTiles);
        }

    }

}
