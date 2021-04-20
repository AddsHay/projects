package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import java.lang.Math;
import java.util.Random;


public class RandomWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    public long SEED = 45927;
    private Random RANDOM = new Random(SEED);

    private static class Pos {
        int x;
        int y;

        Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public void createroom(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dx, int dy) {
        drawrow(tiles, p.x, p.y, walltile, dx);
        drawrow(tiles, p.x, p.y + dy - 1, walltile, dx);
        drawcolumn(tiles, p.x, p.y, walltile, dy);
        drawcolumn(tiles, p.x + dx - 1, p.y, walltile, dy);
        fillroom(tiles, p.x + 1, p.y + 1, floortile, dx - 2, dy - 2);
    }

    public void createhallvert(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dy) {
        drawcolumn(tiles, p.x - 1, p.y, walltile, dy);
        drawcolumn(tiles, p.x, p.y, floortile, dy);
        drawcolumn(tiles, p.x + 1, p.y, walltile, dy);
    }

    public void createhallhor(TETile[][] tiles, TETile walltile, TETile floortile, Pos p, int dx) {
        drawrow(tiles, p.x, p.y - 1, walltile, dx);
        drawrow(tiles, p.x, p.y, floortile, dx);
        drawrow(tiles, p.x, p.y + 1, walltile, dx);
    }

    private void drawrow(TETile[][] tiles, int x, int y, TETile tile, int dx) {
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

    private void drawcolumn(TETile[][] tiles, int x, int y, TETile tile, int dy) {
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

    private void fillroom(TETile[][] tiles, int x, int y, TETile floortile, int dx, int dy) {
        if (dy > 0) {
            drawrow(tiles, x, y, floortile, dx);
            fillroom(tiles, x, y + 1, floortile, dx, dy - 1);
        }
    }

    public void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Return the maximum dimension/length of a construct without going over the bounds of WIDTH or HEIGHT */
    public int createdimension(int p, int w) {
        int d = RandomUtils.uniform(RANDOM, 3, 10);
        if (p + d > w - 1) {
            return w - p - 2;
        } else if (p - d < 0) {
            return p;
        } else {
            return d;
        }
    }

    public void drawbuild(TETile[][] tiles, TETile walltile, TETile floortile, Long inputseed) {
        RANDOM = new Random(inputseed);
        fillWithNothing(tiles);
        Pos p = new Pos(RandomUtils.uniform(RANDOM, 10, WIDTH - 10), RandomUtils.uniform(RANDOM, 10, HEIGHT - 10));
        Pos pz = new Pos(p.x + 1, p.y + 1);
        int dx = createdimension(p.x, WIDTH);
        int dy = createdimension(p.y, HEIGHT);
        Steps end = new Steps(null, null, null, null, null,
                null, null, 0, 0, 0, "x", "x");
        Steps base = new Steps(end, end, tiles, walltile, floortile,
                p, pz, dx, dy, 0, "room", "x");
        end.next = base;
        end.last = base;
        for (int i = RandomUtils.uniform(RANDOM, 1, 4); i > 0; i--) {
            Steps next = stepmaker(base);
            base.last.last.next = next;
            base.last.last = next;
            base.last.last.last = base.last.last.last.last;
            base.last.last.next = base.last;
            base.next.last = base.last;
        }
        bloom(base);
    }

    public void bloom(Steps base) {
        // Build structure (r,h,v) with wall/floor on tiles at p dimensions dx/dy
        //     Use checker to test viability, build only if possible
        // Add exit location data to the end of a list
        // Build new structure with next list item with bloom(next stuff)
        //
        // Fix the given structure
        fix(base);
        // Make the given structure
        // (Note that this makes P the bottom-left corner)
        switch (base.structure) {
            case "room":
                createroom(base.tile, base.wall, base.floor, base.p, base.dx, base.dy);
                base.tile[base.pz.x][base.pz.y] = Tileset.FLOOR;
                break;
            case "hall":
                switch (base.direction) {
                    case "up":
                        createhallvert(base.tile, base.wall, base.floor, base.p, base.dy);
                        drawrow(base.tile, base.p.x - 1, base.p.y + base.dy, base.wall, 3);
                        break;
                    case "down":
                        createhallvert(base.tile, base.wall, base.floor, base.p, -base.dy);
                        drawrow(base.tile, base.p.x - 1, base.p.y - base.dy, base.wall, 3);
                        break;
                    case "left":
                        createhallhor(base.tile, base.wall, base.floor, base.p, -base.dx);
                        drawcolumn(base.tile, base.p.x - base.dx, base.p.y - 1, base.wall, 3);
                        break;
                    case "right":
                        createhallhor(base.tile, base.wall, base.floor, base.p, base.dx);
                        drawcolumn(base.tile, base.p.x + base.dx, base.p.y - 1, base.wall, 3);
                        break;
                    default:
                        break;
                }
                break;
            default:
                base.zero = 1;
                break;
        }
        // Add new Steps
        if (RandomUtils.uniform(RANDOM) > base.zero) {
            for (int i = RandomUtils.uniform(RANDOM, 0, 5); i > 0; i--) {
                Steps next = stepmaker(base);
                Steps now = new Steps(next.next, next.last, next.tile, next.wall, next.floor, next.p, next.pz,
                        next.dx, next.dy, next.zero, next.structure, next.direction);
                base.last.last.next = now;
                base.last.last = now;
                base.last.last.last = base.last.last.last.last;
                base.last.last.next = base.last;
                base.next.last = base.last;
            }
        }
        // Run next thing
        if (!base.structure.equals("x")) {
            bloom(base.next);
        }
    }

    private void fix(Steps step) {
        if (step.structure.equals("room")) {
            fixroom(step);
        } else if (step.structure.equals("hall")) {
            fixhall(step);
        }
    }

    private void fixroom(Steps step) {
        fixroomborder(step);
    }

    private void fixroomborder(Steps step) {
        if (step.p.x < 3) {
            step.p.x = 3;
            step.dx -= 3;
        }
        if (step.p.y < 3) {
            step.p.y = 3;
            step.dy -= 3;
        }
        if (step.p.x + step.dx > WIDTH - 3) {
            step.dx = WIDTH - 3 - step.p.x;
        }
        if (step.p.y + step.dy > HEIGHT - 3) {
            step.dy = HEIGHT - 3 - step.p.y;
        }
        if (step.dx < 3 || step.dy < 3) {
            step.structure = "o";
            step.direction = "o";
        }
    }

    private void fixhall(Steps step) {
        if (step.direction.equals("up")) {
            if (step.p.y + step.dy > HEIGHT - 3) {
                step.dy = Math.max(HEIGHT - 3 - step.p.y, 0);
                if (step.dy == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
            }
            for (int i = 1; i < step.dy; i += 1) {
                if (step.tile[step.p.x][step.p.y + i] == step.wall) {
                    if (step.tile[step.p.x][step.p.y + i + 1] == step.floor) {
                        step.dy = i + 1;
                        break;
                    }
                }
                if (step.tile[step.p.x][step.p.y + i] == step.floor ||
                        step.tile[step.p.x - 1][step.p.y + i] == step.floor ||
                        step.tile[step.p.x + 1][step.p.y + i] == step.floor) {
                    step.structure = "o";
                    step.direction = "o";
                    break;
                }
            }
        }
        if (step.direction.equals("down")) {
            if (step.p.y - step.dy < 3) {
                step.dy = Math.max(step.p.y - 3, 0);
                if (step.dy == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
            }
            for (int i = 1; i < step.dy; i += 1) {
                if (step.tile[step.p.x][step.p.y - i] == step.wall) {
                    if (step.tile[step.p.x][step.p.y - i - 1] == step.floor) {
                        step.dy = i + 1;
                        break;
                    }
                }
                if (step.tile[step.p.x][step.p.y - i] == step.floor  ||
                        step.tile[step.p.x - 1][step.p.y - i] == step.floor ||
                        step.tile[step.p.x + 1][step.p.y - i] == step.floor) {
                    step.structure = "o";
                    step.direction = "o";
                    break;
                }
            }
        }
        if (step.direction.equals("right")) {
            if (step.p.x + step.dx > WIDTH - 3) {
                step.dx = Math.max(WIDTH - 3 - step.p.x, 0);
                if (step.dx == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
            }
            for (int i = 1; i < step.dx; i += 1) {
                if (step.tile[step.p.x + i][step.p.y] == step.wall) {
                    if (step.tile[step.p.x + i + 1][step.p.y] == step.floor) {
                        step.dx = i + 1;
                        break;
                    }
                }
                if (step.tile[step.p.x + i][step.p.y] == step.floor  ||
                        step.tile[step.p.x + i][step.p.y - 1] == step.floor ||
                        step.tile[step.p.x + i][step.p.y + 1] == step.floor) {
                    step.structure = "o";
                    step.direction = "o";
                    break;
                }
            }
        }
        if (step.direction.equals("left")) {
            if (step.p.x - step.dx < 3) {
                step.dx = Math.max(step.p.x - 3, 0);
                if (step.dx == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
            }
            for (int i = 1; i < step.dx; i += 1) {
                if (step.tile[step.p.x - i][step.p.y] == step.wall) {
                    if (step.tile[step.p.x - i - 1][step.p.y] == step.floor) {
                        step.dx = i + 1;
                        break;
                    }
                }
                if (step.tile[step.p.x - i][step.p.y] == step.floor   ||
                        step.tile[step.p.x - i][step.p.y - 1] == step.floor ||
                        step.tile[step.p.x - i][step.p.y + 1] == step.floor) {
                    step.structure = "o";
                    step.direction = "o";
                    break;
                }
            }
        }
    }

    /** Storage unit for the upcoming steps */
    private static class Steps {
        private Steps next;
        private Steps last;
        private final TETile[][] tile;
        private final TETile wall;
        private final TETile floor;
        // p = Constructing position
        // pz = Entrance position
        private Pos p;
        private Pos pz;
        private int dx;
        private int dy;
        private double zero;
        // structure: room, hall
        // direction: up, down, left, right, o (do nothing), x (end)
        private String structure;
        private String direction;
        Steps(Steps nx, Steps ls, TETile[][] tls, TETile wltl, TETile fltl,
              Pos ps, Pos psz, int drx, int dry, double zro, String str, String dir) {
            next = nx;
            last = ls;
            tile = tls;
            wall = wltl;
            floor = fltl;
            p = ps;
            pz = psz;
            dx = drx;
            dy = dry;
            zero = zro;
            structure = str;
            direction = dir;
        }
    }

    private Steps stepmaker(Steps base) {
        Steps a = new Steps(base.last, base.last.last, base.tile, base.wall, base.floor,
                null, null, 0, 0, 0, "x", "x");
        if (base.structure.equals("room")) {
            int y = RandomUtils.uniform(RANDOM, 2);
            int r = RandomUtils.uniform(RANDOM, -base.dx + 2, base.dy - 2);
            if (r < 0) {
                a.pz = new Pos(r * -1 + base.p.x, base.p.y + ((base.dy - 1) * y));
            } else {
                a.pz = new Pos(base.p.x + ((base.dx - 1) * y), r + 1 + base.p.y);
            }
            if (r < 0 && y == 0) {
                a.direction = "down";
            } else if (r >= 0 && y == 0) {
                a.direction = "left";
            } else if (r < 0 && y == 1) {
                a.direction = "up";
            } else if (r >= 0 && y == 1) {
                a.direction = "right";
            } else {
                a.direction = "o";
            }
        } else if (base.structure.equals("hall")) {
            switch (base.direction) {
                case "left" -> a.pz = new Pos(base.p.x - base.dx + 1, base.p.y);
                case "right" -> a.pz = new Pos(base.p.x + base.dx - 1, base.p.y);
                case "up" -> a.pz = new Pos(base.p.x, base.p.y + base.dy - 1);
                case "down" -> a.pz = new Pos(base.p.x, base.p.y - base.dy + 1);
            }
            switch (RandomUtils.uniform(RANDOM, 3)) {
                case 0:
                    // left (+down)
                    if (base.direction.equals("right")) {
                        a.direction = "down";
                        a.pz.y -= 1;
                    } else {
                        a.direction = "left";
                        a.pz.x -= 1;
                    }
                    break;
                case 1:
                    // up (+down)
                    if (base.direction.equals("down")) {
                        a.direction = "down";
                        a.pz.y -= 1;
                    } else {
                        a.direction = "up";
                        a.pz.y += 1;
                    }
                    break;
                case 2:
                    // right (+down)
                    if (base.direction.equals("left")) {
                        a.direction = "down";
                        a.pz.y -= 1;
                    } else {
                        a.direction = "right";
                        a.pz.x += 1;
                    }
                    break;
                default: a.direction = "o";
            }
        } else {
            a.structure = "o";
            a.direction = "o";
        }
        // Choose structure
        if (RandomUtils.uniform(RANDOM, 5) < 1) {
            // room
            a.structure = "room";
            a.dx = RandomUtils.uniform(RANDOM, 3, 10);
            a.dy = RandomUtils.uniform(RANDOM, 3, 10);
            a.p = new Pos(a.pz.x, a.pz.y);
            switch (a.direction) {
                case "left" -> {
                    a.p.x -= a.dx - 1;
                    a.p.y -= RandomUtils.uniform(RANDOM, 1, a.dy - 1);
                }
                case "right" -> a.p.y -= RandomUtils.uniform(RANDOM, 1, a.dy - 1);
                case "up" -> a.p.x -= RandomUtils.uniform(RANDOM, 1, a.dx - 1);
                case "down" -> {
                    a.p.x -= RandomUtils.uniform(RANDOM, 1, a.dx - 1);
                    a.p.y -= a.dy - 1;
                }
                default -> a.structure = "o";
            }
        } else {
            // hall
            a.structure = "hall";
            a.p = new Pos(a.pz.x, a.pz.y);
            a.dx = RandomUtils.uniform(RANDOM, 3, 10);
            a.dy = RandomUtils.uniform(RANDOM, 3, 10);
            switch (a.direction) {
                case "left" -> a.pz.x -= a.dx - 1;
                case "right" -> a.pz.x += a.dx - 1;
                case "up" -> a.pz.y += a.dx - 1;
                case "down" -> a.pz.y -= a.dy - 1;
                default -> {
                    a.structure = "o";
                    a.direction = "o";
                }
            }
        }
        return a;
    }
}