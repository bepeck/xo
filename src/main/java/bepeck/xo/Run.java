package bepeck.xo;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static bepeck.xo.Field.generateWins;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

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
        new Game(playerX, playerO, 3, System.out).run();
    }

    static class Game {

        private final Player playerX;
        private final Player playerO;
        private final PrintStream ps;
        private final int fieldSize;

        Game(
                final Player playerX,
                final Player playerO,
                final int fieldSize,
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
            this.fieldSize = fieldSize;
        }

        void run() {
            ps.println("Let's play");

            final int steps = fieldSize * fieldSize;

            final Set<Set<Point>> wins = generateWins(fieldSize);

            final List<Player> playersQueue = Stream.generate(() -> Stream.of(
                    playerX,
                    playerO
            )).flatMap(identity()).limit(steps).collect(toList());

            Field field = new Field(fieldSize);

            for (final Player player : playersQueue) {
                print(field);

                ps.println("------------------------------");
                ps.println(player.getName() + ": next step");

                while (true) {
                    final Point point = player.nextStep(field);
                    try {
                        field = field.with(point, player.getStamp());
                        break;
                    } catch (Exception e) {
                        ps.println("wrong point, try again: " + e.getMessage());
                    }
                }

                if (checkWin(field, player.getStamp(), wins)) {
                    ps.println(player.getName() + " win");
                    break;
                }
            }

            print(field);
        }

        private boolean checkWin(final Field field, final Stamp stamp, final Set<Set<Point>> wins) {
            return wins.stream().anyMatch(win -> field.checkWin(win, stamp));
        }

        private void print(final Field field) {
            for (int row = 0; row < fieldSize; row++) {
                for (int column = 0; column < fieldSize; column++) {
                    ps.print("|");
                    final Stamp stamp = field.getStamp(new Point(row, column));
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
}
