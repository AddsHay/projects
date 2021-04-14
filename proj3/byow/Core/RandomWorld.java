package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.lang.Math;
import java.util.Random;

public class RandomWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static final long SEED = 2873124;
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

    public static void createroom(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dx, int dy) {
        drawrow(tiles, p.x, p.y, walltile, dx);
        drawrow(tiles, p.x, p.y + dy - 1, walltile, dx);
        drawcolumn(tiles, p.x, p.y, walltile, dy);
        drawcolumn(tiles, p.x + dx - 1, p.y, walltile, dy);
        fillroom(tiles, p.x + 1, p.y + 1, floortile, dx - 2, dy - 2);
    }

    public static void createhallvert(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dy) {
        drawcolumn(tiles, p.x - 1, p.y, walltile, dy);
        drawcolumn(tiles, p.x, p.y, floortile, dy);
        drawcolumn(tiles, p.x + 1, p.y, walltile, dy);
    }

    public static void createhallhor(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dx) {
        drawrow(tiles, p.x, p.y - 1, walltile, dx);
        drawrow(tiles, p.x, p.y, floortile, dx);
        drawrow(tiles, p.x, p.y + 1, walltile, dx);
    }

    private static void drawrow(TETile[][] tiles, int x, int y, TETile tile, int dx) {
        if (dx > 0) {
            if (tiles[x][y] != Tileset.FLOOR) {
                tiles[x][y] = tile;
            }
            drawrow(tiles, x + 1, y, tile, dx - 1);
        }
        if (dx < 0) {
            if (tiles[x][y] != Tileset.FLOOR) {
                tiles[x][y] = tile;
            }
            drawrow(tiles, x - 1, y, tile, dx + 1);
        }
    }

    private static void drawcolumn(TETile[][] tiles, int x, int y, TETile tile, int dy) {
        if (dy > 0) {
            if (tiles[x][y] != Tileset.FLOOR) {
                tiles[x][y] = tile;
            }
            drawcolumn(tiles, x, y + 1, tile, dy - 1);
        }
        if (dy < 0) {
            if (tiles[x][y] != Tileset.FLOOR) {
                tiles[x][y] = tile;
            }
            drawcolumn(tiles, x, y - 1, tile, dy + 1);
        }
    }

    private static void fillroom(TETile[][] tiles, int x, int y, TETile floortile, int dx, int dy) {
        if (dy > 0) {
            drawrow(tiles, x, y, floortile, dx);
            fillroom(tiles, x, y + 1, floortile, dx, dy - 1);
        }
    }

    public static void cap(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    if (tiles[x + 1][y] == Tileset.NOTHING || tiles[x][y - 1] == Tileset.NOTHING
                            || tiles[x - 1][y] == Tileset.NOTHING || tiles[x][y + 1] == Tileset.NOTHING
                            || tiles[x + 1][y - 1] == Tileset.NOTHING || tiles[x - 1][y - 1] == Tileset.NOTHING
                            || tiles[x - 1][y + 1] == Tileset.NOTHING || tiles[x + 1][y + 1] == Tileset.NOTHING) {
                        tiles[x][y] = Tileset.WALL;
                    }
                }
            }
        }
    }

    public static void randomexit(TETile[][] tiles, Pos p, int dx, int dy) {
        int check = RandomUtils.uniform(RANDOM, -Math.abs(dx) - Math.abs(dy) + 4, Math.abs(dx) + Math.abs(dy) - 4);
        if (check < -dx + 2) {
            if (p.x - 3 > 0) {
                p.y += check + dy - 2;
            } else {
                check = 0;
            }
        } else if (check < 0) {
            if (p.y - 3 > 0) {
                p.x += check;
            } else {
                check = 0;
            }
        } else if (check > dx - 2) {
            if (p.x + dx + 3 < WIDTH) {
                p.y += check - dy + 2;
                p.x += dx - 1;
            } else {
                check = 0;
            }
        } else if (check > 0) {
            if (p.y + dy + 3 < HEIGHT) {
                p.x += check;
                p.y += dy - 1;
            } else {
                check = 0;
            }
        }
        if (check == 0) {
            randomexit(tiles, p, dx, dy);
        } else {
            tiles[p.x][p.y] = Tileset.FLOOR;
        }
    }

    public static void randombuilder(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dx, int dy) {
        int type = 0;
        if (tiles[p.x - 1][p.y] == Tileset.WALL && tiles[p.x + 1][p.y] == Tileset.WALL) {
            if (tiles[p.x][p.y - 1] == Tileset.FLOOR) {
                // Vertically up
                type = 1;
            } else if (tiles[p.x][p.y + 1] == Tileset.FLOOR) {
                // Vertically down
                type = -1;
            }
        } else if (tiles[p.x][p.y - 1] == Tileset.WALL && tiles[p.x][p.y + 1] == Tileset.WALL) {
            if (tiles[p.x - 1][p.y] == Tileset.FLOOR) {
                // Horizontally right
                type = 2;
            } else if (tiles[p.x + 1][p.y] == Tileset.FLOOR) {
                // Horizontally left
                type = -2;
            }
        }
        int check = RandomUtils.uniform(RANDOM, 0, 2);
        if (check == 0) {
            // room
            if (Math.abs(type) == 1) {
                // upward/downward room
                createroom(tiles, walltile, floortile, p, dx * type, dy * type);
            } else if (Math.abs(type) == 2) {
                // rightward/leftward room
                createroom(tiles, walltile, floortile, p, dx * type / 2, dy * type / 2);
            }
        } else {
            // hall
            if (Math.abs(type) == 1) {
                // upward/downward hall
                createhallvert(tiles, walltile, floortile, p, dy * type);
            } else if (Math.abs(type) == 2) {
                // rightward/leftward hall
                createhallhor(tiles, walltile, floortile, p, dx * type / 2);
            }
        }
    }

    public static void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Return the maximum dimension without going over */
    public static int createdimension(int p, int w) {
        int d = RandomUtils.uniform(RANDOM, 3, 10);
        if (p + d > w - 1) {
            return w - p - 2;
        } else if (p + d < 0) {
            return - p;
        } else {
            return d;
        }
    }


    public static void drawworld(TETile[][] tiles, TETile walltile, TETile floortile) {
        fillWithNothing(tiles);
        int opcount = RandomUtils.uniform(RANDOM, 10, 50);
        Pos p = new Pos(RandomUtils.uniform(RANDOM, 10, WIDTH - 10), RandomUtils.uniform(RANDOM, 10, HEIGHT - 10));
        int dx = createdimension(p.x, WIDTH);
        int dy = createdimension(p.y, HEIGHT);
        createroom(tiles, walltile, floortile, p, dx, dy);
        for (int z = 0; z < opcount; z++) {
            randomexit(tiles, p, dx, dy);
            dx = createdimension(p.x, WIDTH);
            dy = createdimension(p.y, HEIGHT);
            randombuilder(tiles, walltile, floortile, p, dx, dy);
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);
        TETile[][] tiles = new TETile[WIDTH][HEIGHT];
        /**
        fillWithNothing(tiles);
        Pos p = new Pos(10, 10);
        createroom(tiles, Tileset.WALL, Tileset.FLOOR, p, 15, 15);
        p = new Pos(30, 20);
        createhallvert(tiles, Tileset.WALL, Tileset.FLOOR, p, 9);
        createhallhor(tiles, Tileset.WALL, Tileset.FLOOR, p, 9);
        createhallvert(tiles, Tileset.WALL, Tileset.FLOOR, p, -9);
        createhallhor(tiles, Tileset.WALL, Tileset.FLOOR, p, -9);
        cap(tiles);
         */
        drawworld(tiles, Tileset.WALL, Tileset.FLOOR);

        ter.renderFrame(tiles);
    }
}
