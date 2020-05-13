package bepeck.xo;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class Run {

    public static void main(String[] args) {
//        final HumanPlayer playerX = new HumanPlayer(
//                System.in,
//                System.out,
//                "Player 1",
//                Stamp.O
//        );
//        final HumanPlayer playerO = new HumanPlayer(
//                System.in,
//                System.out,
//                "Player 2",
//                Stamp.X
//        );
        final Player playerX = new StupidComputerPlayer(
                "Player 1", Stamp.O
        );
        final Player playerO = new StupidComputerPlayer(
                "Player 2", Stamp.X
        );
        new Game(playerX, playerO, new FieldControlImpl(3), System.out).run();
    }

    private enum Stamp {
        X, O
    }

    private enum SetPointStateResult {
        BUSY,
        MISS,
        OK
    }

    interface Player {
        Point nextStep(Field field);

        String getName();

        Stamp getStamp();
    }

    interface Field {

        int getSize();

        Stamp getStamp(Point point);

        void print(PrintStream ps);

        boolean checkWin(Stamp stamp);
    }

    interface FieldControl extends Field {
        Set<Set<Point>> getWins();

        void clear();

        SetPointStateResult setStamp(Point point, Stamp stamp);
    }

    static class HumanPlayer implements Player {

        private final Scanner scanner;
        private final PrintStream out;
        private final String name;
        private final Stamp stamp;

        HumanPlayer(final InputStream in, final PrintStream out, final String name, final Stamp stamp) {
            this.scanner = new Scanner(requireNonNull(in));
            this.out = requireNonNull(out);
            this.name = requireNonNull(name);
            this.stamp = requireNonNull(stamp);
        }

        public Point nextStep(final Field field) {
            while (true) {
                try {
                    out.print("pls type column number: ");
                    final int column = scanner.nextInt();
                    out.print("pls type row number:    ");
                    final int row = scanner.nextInt();
                    return new Point(row, column);
                } catch (final InputMismatchException e) {
                    out.println("wrong input, try again");
                } finally {
                    scanner.skip(".*");
                }
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Stamp getStamp() {
            return stamp;
        }
    }

    static class StupidComputerPlayer implements Player {

        private final Random random = new Random();
        private final Stamp stamp;
        private final String name;

        StupidComputerPlayer(final String name, final Stamp stamp) {
            this.stamp = stamp;
            this.name = name;
        }

        @Override
        public Point nextStep(final Field field) {
            while (true) {
                final Point point = new Point(
                        random.nextInt(field.getSize()),
                        random.nextInt(field.getSize())
                );
                if (field.getStamp(point) == null) {
                    return point;
                }
            }
        }

        @Override
        public String getName() {
            return "stupid player - " + name;
        }

        @Override
        public Stamp getStamp() {
            return stamp;
        }
    }

    static class Point {
        final int x;
        final int y;

        Point(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final Point point = (Point) obj;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Point{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    static class Game {

        private final Player playerX;
        private final Player playerO;
        private final FieldControl field;
        private final PrintStream ps;

        Game(
                final Player playerX,
                final Player playerO,
                final FieldControl field,
                final PrintStream ps
        ) {
            this.ps = ps;
            if (Objects.equals(playerX.getName(), playerO.getName())) {
                throw new IllegalArgumentException("names are duplicated");
            }
            if (Objects.equals(playerX.getStamp(), playerO.getStamp())) {
                throw new IllegalArgumentException("stamps are duplicated");
            }
            this.playerX = playerX;
            this.playerO = playerO;
            this.field = field;

            field.clear();
        }

        void run() {
            ps.println("Let's play");

            final int steps = field.getSize() * field.getSize();

            final List<Player> playersQueue = Stream.generate(() -> Stream.of(
                    playerX,
                    playerO
            )).flatMap(identity()).limit(steps).collect(toList());

            for (final Player player : playersQueue) {
                field.print(ps);

                ps.println("------------------------------");
                ps.println(player.getName() + ": next step");

                while (true) {
                    final Point point = player.nextStep(field);
                    final SetPointStateResult result = field.setStamp(point, player.getStamp());
                    if (result == SetPointStateResult.OK) {
                        break;
                    }
                    ps.println("wrong point, try again: " + result);
                }

                if (field.checkWin(player.getStamp())) {
                    ps.println(player.getName() + " win");
                    break;
                }
            }

            field.print(ps);
        }
    }

    static class FieldControlImpl implements FieldControl {

        private final int size;
        private final Set<Set<Point>> wins;

        private Map<Point, Stamp> values = new HashMap<>();

        FieldControlImpl(final int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("size should be greater than 0");
            }
            this.size = size;
            final Map<Integer, Set<Point>> rows = new HashMap<>();
            final Map<Integer, Set<Point>> columns = new HashMap<>();
            final Set<Point> diagonal1 = new HashSet<>();
            final Set<Point> diagonal2 = new HashSet<>();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    rows.computeIfAbsent(i, ii -> new HashSet<>()).add(new Point(i, j));
                    columns.computeIfAbsent(i, ii -> new HashSet<>()).add(new Point(j, i));
                }
            }
            for (int i = 0; i < size; i++) {
                diagonal1.add(new Point(i, i));
                diagonal2.add(new Point(i, size - 1 - i));
            }
            this.wins = Stream.of(
                    singleton(diagonal1),
                    singleton(diagonal2),
                    rows.values(),
                    columns.values()
            ).flatMap(Collection::stream).map(Collections::unmodifiableSet).collect(toSet());
        }

        @Override
        public Set<Set<Point>> getWins() {
            return wins;
        }

        @Override
        public void clear() {
            values.clear();
        }

        @Override
        public int getSize() {
            return size;
        }

        @Override
        public Stamp getStamp(final Point point) {
            return values.get(point);
        }

        @Override
        public SetPointStateResult setStamp(final Point point, final Stamp stamp) {
            requireNonNull(point);
            requireNonNull(stamp);
            if (point.x < 0 || point.x >= size) {
                return SetPointStateResult.MISS;
            }
            final Stamp currentState = values.putIfAbsent(point, stamp);
            if (currentState == null) {
                return SetPointStateResult.OK;
            } else {
                return SetPointStateResult.BUSY;
            }
        }

        @Override
        public void print(final PrintStream ps) {
            for (int row = 0; row < size; row++) {
                for (int column = 0; column < size; column++) {
                    ps.print("|");
                    final Stamp stamp = values.get(new Point(row, column));
                    if (stamp == null) {
                        ps.print(" ");
                    } else {
                        ps.print(stamp.name());
                    }
                }
                ps.println("|");
            }
        }

        @Override
        public boolean checkWin(final Stamp stamp) {
            return wins.stream().anyMatch(
                    points -> points.stream().allMatch(point -> getStamp(point) == stamp)
            );
        }
    }
}
