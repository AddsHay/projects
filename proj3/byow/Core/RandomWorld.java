package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.awt.*;
import java.io.Serializable;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;
import java.io.File;


public class RandomWorld implements Serializable {
    private final int WIDTH = 50;
    private final int HEIGHT = 50;
    public long SEED = 45927;
    private Random RANDOM = new Random(SEED);
    public final File CWD = new File(System.getProperty("user.dir"));
    public File savedstate = Utils.join(CWD, "savedstate.txt");
    public ArrayList<Integer> lights = new ArrayList();
    public ArrayList<Integer> floors = new ArrayList();
    public Pos avatarpos = new Pos(0,0);

    private class Pos {
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
        SEED = inputseed;
        RANDOM = new Random(SEED);
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
        /**
        if (!step.structure.equals("o") && !step.direction.equals("o")) {
            fixroomcollide(step);
        }
        */
    }


    private void fixroomborder(Steps step) {
        if (step.p.x < 3) {
            step.dx += step.p.x - 3;
            step.p.x = 3;
        }
        if (step.p.y < 3) {
            step.dy += step.p.y - 3;
            step.p.y = 3;
        }
        if (step.p.x + step.dx > WIDTH - 3) {
            step.dx = WIDTH - 3 - step.p.x;
        }
        if (step.p.y + step.dy > HEIGHT - 3) {
            step.dy = HEIGHT - 3 - step.p.y;
        }
        if (step.dx < 4 || step.dy < 4) {
            step.structure = "o";
            step.direction = "o";
        }
    }

    private void fixroomcollide(Steps step) {
        switch (step.direction) {
            case "up":
                int verlim = step.dy;
                int leftlim = step.pz.x - step.p.x + 1;
                int rightlim = step.p.x + step.dx - step.pz.x;
                for (int i = step.p.x; i < step.p.x + step.dx; i += 1) {
                    for (int j = 0; j < verlim; j += 1) {
                        if (step.tile[i][step.p.y + j] == step.floor) {
                            verlim = j;
                        }
                    }
                }
                for (int i = step.p.y; i < step.p.y + step.dy; i += 1) {
                    for (int j = 0; j < leftlim; j += 1) {
                        if (step.tile[i - j][j] == step.floor) {
                            leftlim = j;
                        }
                    }
                    for (int j = 0; j < rightlim; j += 1) {
                        if (step.tile[i + j][j] == step.floor) {
                            rightlim = j;
                        }
                    }
                }
                if (verlim < 4) {
                    verlim = 0;
                }
                if (leftlim + rightlim < 5) {
                    leftlim = 0;
                    rightlim = 0;
                }
                if (verlim == 0 && leftlim == 0 && rightlim == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
                if (verlim * step.dx > (leftlim + rightlim - 1) * step.dy) {
                    step.dy = verlim;
                } else {
                    step.p.x = step.pz.x - leftlim + 1;
                    step.dx = leftlim + rightlim - 1;
                }
                break;
            case "down":
                verlim = step.dy;
                leftlim = step.pz.x - step.p.x + 1;
                rightlim = step.p.x + step.dx - step.pz.x;
                for (int i = step.p.x; i < step.p.x + step.dx; i += 1) {
                    for (int j = 0; j < verlim; j += 1) {
                        if (step.tile[i][step.p.y - j] == step.floor) {
                            verlim = j;
                        }
                    }
                }
                for (int i = step.p.y; i < step.p.y + step.dy; i += 1) {
                    for (int j = 0; j < leftlim; j += 1) {
                        if (step.tile[i - j][j] == step.floor) {
                            leftlim = j;
                        }
                    }
                    for (int j = 0; j < rightlim; j += 1) {
                        if (step.tile[i + j][j] == step.floor) {
                            rightlim = j;
                        }
                    }
                }
                if (verlim < 4) {
                    verlim = 0;
                }
                if (leftlim + rightlim < 5) {
                    leftlim = 0;
                    rightlim = 0;
                }
                if (verlim == 0 && leftlim == 0 && rightlim == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
                if (verlim * step.dx > (leftlim + rightlim - 1) * step.dy) {
                    step.p.y = step.pz.y - verlim + 1;
                    step.dy = verlim;
                } else {
                    step.p.x = step.pz.x - leftlim + 1;
                    step.dx = leftlim + rightlim - 1;
                }
                break;
            case "left":
                int horlim = step.dx;
                int downlim = step.pz.y - step.p.y + 1;
                int uplim = step.p.y + step.dy - step.pz.y;
                for (int i = step.p.y; i < step.p.y + step.dy; i += 1) {
                    for (int j = 0; j < horlim; j += 1) {
                        if (step.tile[step.p.y - j][i] == step.floor) {
                            horlim = j;
                        }
                    }
                }
                for (int i = step.p.x; i > step.p.x - step.dx; i -= 1) {
                    for (int j = 0; j < downlim; j += 1) {
                        if (step.tile[i][step.pz.y - j] == step.floor) {
                            downlim = j;
                        }
                    }
                    for (int j = 0; j < uplim; j += 1) {
                        if (step.tile[i][step.pz.y + j] == step.floor) {
                            uplim = j;
                        }
                    }
                }
                if (horlim < 4) {
                    horlim = 0;
                }
                if (downlim + uplim < 5) {
                    downlim = 0;
                    uplim = 0;
                }
                if (horlim == 0 && downlim == 0 && uplim == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
                if (horlim * step.dy > (downlim + uplim - 1) * step.dx) {
                    step.p.x = step.pz.x - horlim + 1;
                    step.dx = horlim;
                } else {
                    step.p.y = step.pz.y - downlim + 1;
                    step.dy = downlim + uplim - 1;
                }
                break;
            case "right":
                horlim = step.dx;
                downlim = step.pz.y - step.p.y + 1;
                uplim = step.p.y + step.dy - step.pz.y;
                for (int i = step.p.y; i < step.p.y + step.dy; i += 1) {
                    for (int j = 0; j < horlim; j += 1) {
                        if (step.tile[step.p.y + j][i] == step.floor) {
                            horlim = j;
                        }
                    }
                }
                for (int i = step.p.x; i < step.p.x + step.dx; i += 1) {
                    for (int j = 0; j < downlim; j += 1) {
                        if (step.tile[i][step.pz.y - j] == step.floor) {
                            downlim = j;
                        }
                    }
                    for (int j = 0; j < uplim; j += 1) {
                        if (step.tile[i][step.pz.y + j] == step.floor) {
                            uplim = j;
                        }
                    }
                }
                if (horlim < 4) {
                    horlim = 0;
                }
                if (downlim + uplim < 5) {
                    downlim = 0;
                    uplim = 0;
                }
                if (horlim == 0 && downlim == 0 && uplim == 0) {
                    step.structure = "o";
                    step.direction = "o";
                }
                if (horlim * step.dy > (downlim + uplim - 1) * step.dx) {
                    step.dx = horlim;
                } else {
                    step.p.y = step.pz.y - downlim + 1;
                    step.dy = downlim + uplim - 1;
                }
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
    private class Steps {
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

    public void placeavatar(TETile[][] tiles, TETile avatartile) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    floors.add(((y - 1) * WIDTH) + x);
                }
            }
        }
        if (floors.size() == 0) {

        }
        int com = floors.get(RandomUtils.uniform(RANDOM, floors.size()));
        int x = com % WIDTH;
        int y = com / WIDTH + 1;
        tiles[x][y] = avatartile;
        avatarpos = new Pos(x, y);
    }

    public TETile[][] takeaction(TETile[][] tiles, String commands, TETile walltile, TETile floortile, TETile avatartile) {
        if (commands.length() == 0) {
            return tiles;
        }
        if (!Character.toString(commands.charAt(0)).equals("L")
                && !Character.toString(commands.charAt(0)).equals("l")) {
            placeavatar(tiles, Tileset.AVATAR);
        }
        for (int i = 0; i < commands.length(); i++) {
            if (Character.toString(commands.charAt(i)).equals("t")) {
                togglelight(tiles, new TETile('·', new Color(128, 192, 128),
                        new Color(255, 255, 255), "light"));
            }
            if (Character.toString(commands.charAt(i)).equals("W")) {
                if (tiles[avatarpos.x][avatarpos.y + 1] != walltile) {
                    tiles[avatarpos.x][avatarpos.y] = floortile;
                    avatarpos.y += 1;
                    tiles[avatarpos.x][avatarpos.y] = avatartile;
                }
            }
            if (Character.toString(commands.charAt(i)).equals("A")) {
                if (tiles[avatarpos.x - 1][avatarpos.y] != walltile) {
                    tiles[avatarpos.x][avatarpos.y] = floortile;
                    avatarpos.x += -1;
                    tiles[avatarpos.x][avatarpos.y] = avatartile;
                }
            }
            if (Character.toString(commands.charAt(i)).equals("S")) {
                if (tiles[avatarpos.x][avatarpos.y - 1] != walltile) {
                    tiles[avatarpos.x][avatarpos.y] = floortile;
                    avatarpos.y -= 1;
                    tiles[avatarpos.x][avatarpos.y] = avatartile;
                }
            }
            if (Character.toString(commands.charAt(i)).equals("D")) {
                if (tiles[avatarpos.x + 1][avatarpos.y] != walltile) {
                    tiles[avatarpos.x][avatarpos.y] = floortile;
                    avatarpos.x += 1;
                    tiles[avatarpos.x][avatarpos.y] = avatartile;
                }
            }
            if (Character.toString(commands.charAt(i)).equals(":")) {
                if (Character.toString(commands.charAt(i + 1)).equals("Q")
                        || Character.toString(commands.charAt(i + 1)).equals("q")) {
                    File savedstate = new File("savedstate.txt");
                    String inputseed = "n" + SEED + "s" + commands.substring(0, commands.length() - 2);
                    Utils.writeContents(savedstate, inputseed);
                    return tiles;
                }
            }
            if (Character.toString(commands.charAt(i)).equals("L")
                    || Character.toString(commands.charAt(i)).equals("l")) {
                String inputstring = Utils.readContentsAsString(savedstate);
                String inputseed = inputstring;
                String oldcmd = inputstring;
                if (Character.toString(inputstring.charAt(0)).equals("N")
                        || Character.toString(inputstring.charAt(0)).equals("n")) {
                    inputseed = inputstring.substring(1);
                }
                for (int x = 0; x < inputseed.length(); x++) {
                    if (Character.isLetter(inputseed.charAt(x))) {
                        oldcmd = inputseed.substring(x + 1);
                        inputseed = inputseed.substring(0, x);
                        break;
                    }
                }
                drawbuild(tiles, Tileset.WALL, Tileset.FLOOR, Long.parseLong(inputseed));
                takeaction(tiles, oldcmd, Tileset.WALL, Tileset.FLOOR, Tileset.AVATAR);
                } else {
                    continue;
                }
            }
        return tiles;
    }

    public void placelight(TETile[][] tiles, TETile lighttile) {
        Pos p = new Pos(RandomUtils.uniform(RANDOM, 3, WIDTH - 3), RandomUtils.uniform(RANDOM, 3, HEIGHT - 3));
        if (tiles[p.x][p.y] != Tileset.FLOOR) {
            placelight(tiles, lighttile);
        } else {
            tiles[p.x][p.y] = lighttile;
            lights.add(((p.y - 1) * WIDTH) + p.x);
        }
    }

    /** pass in  as lighttile:
     * TETile Light = new TETile('·', new Color(128, 192, 128), new Color(255, 255, 255),
     *             "light)
     */
    public void togglelight(TETile[][] tiles, TETile lighttile) {
        int floorcount = 0;
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                if (tiles[x][y] == Tileset.FLOOR) {
                    floorcount += 1;
                }
            }
        }
        for (int x = 0; x < floorcount / 49; x++) {
            placelight(tiles, lighttile);
        }
        for (Integer light : lights) {
            int x = light % WIDTH;
            int y = light / WIDTH + 1;
            for (int xstart = x - 4; xstart < 10; xstart += 1) {
                for (int ystart = y - 4; ystart < 10; ystart += 1) {
                    if (tiles[x][y].isBlack()) {
                        if (tiles[xstart][ystart] == Tileset.FLOOR) {
                            int dx = x - xstart;
                            int dy = y - ystart;
                            int c = (int) Math.round(102 * Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)));
                            if (c < 255) {
                                TETile.recolor(lighttile, -c, -c, 0);
                                tiles[xstart][ystart] = lighttile;
                            } else {
                                TETile.recolor(lighttile, -255, -255, 255 - c);
                                tiles[xstart][ystart] = lighttile;
                            }
                        }
                    } else if (tiles[xstart][ystart] == Tileset.FLOOR) {
                        TETile.recolor(lighttile, 0, 0, 0);
                        tiles[xstart][ystart] = lighttile;
                        tiles[x][y] = lighttile;
                    }
                }
            }
        }
    }

}