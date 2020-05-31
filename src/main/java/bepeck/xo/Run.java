package bepeck.xo;

import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class Run {

    public static void main(String[] args) {
//        final HumanPlayer player1 = new HumanPlayer(
//                System.in,
//                System.out,
//                "Player 1",
//                Stamp.O
//        );
//        final HumanPlayer player2 = new HumanPlayer(
//                System.in,
//                System.out,
//                "Player 2",
//                Stamp.X
//        );

        final Player player1 = new SmartComputerPlayer(
                "Player 1", Stamp.O
        );
        final Player player2 = new SmartComputerPlayer(
                "Player 2", Stamp.X
        );
//        final Player player1 = new StupidComputerPlayer(
//                "Player 1", Stamp.O
//        );
//        final Player player2 = new StupidComputerPlayer(
//                "Player 2", Stamp.X
//        );
        new Game(player1, player2, 3, System.out).run();
    }

    static class Game {

        private final Player player1;
        private final Player player2;
        private final PrintStream ps;
        private final int fieldSize;

        Game(
                final Player player1,
                final Player player2,
                final int fieldSize,
                final PrintStream ps
        ) {
            this.ps = ps;
            if (Objects.equals(player1.getName(), player2.getName())) {
                throw new IllegalArgumentException("names are duplicated");
            }
            if (Objects.equals(player1.getStamp(), player2.getStamp())) {
                throw new IllegalArgumentException("stamps are duplicated");
            }
            this.player1 = player1;
            this.player2 = player2;
            this.fieldSize = fieldSize;
        }

        void run() {
            ps.println("Let's play");

            final int steps = fieldSize * fieldSize;

            final List<Player> playersQueue = Stream.generate(() -> Stream.of(
                    player1,
                    player2
            )).flatMap(identity()).limit(steps).collect(toList());

            Field field = new Field(fieldSize);

            for (final Player player : playersQueue) {
                field.print(ps);

                ps.println("------------------------------");
                ps.println(player.getName() + ": next step");

                while (true) {
                    final Point point = player.nextStep(field);
                    if (field.getBusyPoints().contains(point)) {
                        ps.println("busy, try again");
                    } else if (field.getPoints().contains(point)) {
                        field = field.with(point, player.getStamp());
                        break;
                    } else {
                        ps.println("out of field, try again");
                    }
                }

                if (field.checkWin(player.getStamp())) {
                    field.print(ps);
                    ps.println(player.getName() + " win");
                    return;
                }
            }
            field.print(ps);
            ps.println("nobody win");
        }
    }
}
