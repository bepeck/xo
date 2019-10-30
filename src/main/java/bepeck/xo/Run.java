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
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class Run {

    public static void main(String[] args) {
        final ConsolePlayer playerX = new ConsolePlayer(
                System.in,
                System.out,
                "Player 1",
                Stamp.O
        );
        final ConsolePlayer playerO = new ConsolePlayer(
                System.in,
                System.out,
                "Player 2",
                Stamp.X
        );
        new Game(playerX, playerO, new Field(3), System.out).run();
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
        Point nextStep() throws NextStepException;

        String getName();

        Stamp getStamp();
    }

    static class ConsolePlayer implements Player {

        private final Scanner scanner;
        private final PrintStream out;
        private final String name;
        private final Stamp stamp;

        ConsolePlayer(final InputStream in, final PrintStream out, final String name, final Stamp stamp) {
            this.scanner = new Scanner(requireNonNull(in));
            this.out = requireNonNull(out);
            this.name = requireNonNull(name);
            this.stamp = requireNonNull(stamp);
        }

        public Point nextStep() throws NextStepException {
            try {
                out.print("pls type column number: ");
                final int column = scanner.nextInt();
                out.print("pls type row number:    ");
                final int row = scanner.nextInt();
                return new Point(row, column);
            } catch (final InputMismatchException e) {
                throw new NextStepException();
            } finally {
                scanner.skip(".*");
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
        private final Field field;
        private final PrintStream ps;
        private final Set<Set<Point>> winLines;

        Game(
                final ConsolePlayer playerX,
                final ConsolePlayer playerO,
                final Field field,
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
            this.winLines = generateWins(field.size);

            field.clear();
        }

        private Set<Set<Point>> generateWins(int size) {
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
            return Stream.of(
                    singleton(diagonal1),
                    singleton(diagonal2),
                    rows.values(),
                    columns.values()
            ).flatMap(Collection::stream).map(Collections::unmodifiableSet).collect(Collectors.toSet());
        }

        void run() {
            ps.println("Let's play");

            final int steps = field.size * field.size;

            final List<Player> playersQueue = Stream.generate(() -> Stream.of(
                    playerX,
                    playerO
            )).flatMap(identity()).limit(steps).collect(toList());

            for (final Player player : playersQueue) {
                field.print(ps);

                ps.println("------------------------------");
                ps.println(player.getName() + ": next step");

                while (true) {
                    final Point point;
                    try {
                        point = player.nextStep();
                    } catch (final NextStepException e) {
                        ps.println("wrong input, try again");
                        continue;
                    }
                    final SetPointStateResult result = field.setStamp(point, player.getStamp());
                    if (result == SetPointStateResult.OK) {
                        break;
                    }
                    ps.println("wrong point, try again: " + result);
                }

                if (checkWin(player.getStamp())) {
                    ps.println(player.getName() + " win");
                    break;
                }
            }

            field.print(ps);
        }

        private boolean checkWin(final Stamp stamp) {
            return winLines.stream().anyMatch(
                    points -> points.stream().allMatch(point -> field.getStamp(point) == stamp)
            );
        }
    }

    static class Field {

        final int size;

        private Map<Point, Stamp> values = new HashMap<>();

        Field(final int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("size should be greater than 0");
            }
            this.size = size;
        }

        void clear() {
            values.clear();
        }

        Stamp getStamp(final Point point) {
            return values.get(point);
        }

        SetPointStateResult setStamp(final Point point, final Stamp stamp) {
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

        void print(final PrintStream ps) {
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
    }

    static class NextStepException extends Exception {
    }
}
