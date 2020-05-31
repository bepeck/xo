package bepeck.xo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static bepeck.xo.StupidComputerPlayer.getRandomFreePoint;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SmartComputerPlayer implements Player {

    private final Stamp stamp;
    private final String name;

    public SmartComputerPlayer(final String name, final Stamp stamp) {
        this.name = requireNonNull(name);
        this.stamp = requireNonNull(stamp);
    }

    @Override
    public Point nextStep(final Field field) {
        final Set<List<Step>> games = new HashSet<>();

        simulateGameOptions(field, stamp, emptyList(), games::add, 5);

        final Comparator<List<Step>> comparing = comparing((Function<List<Step>, Integer>) List::size).thenComparing(steps -> {
            final Step step = steps.get(steps.size() - 1);
            if (step.win) {
                if (step.stamp == stamp) {
                    return 0;
                } else {
                    return 1;
                }
            } else {
                return 2;
            }
        });

        final List<List<Step>> sortedGames = games.stream().sorted(comparing).collect(toList());
        if (sortedGames.size() > 0) {
            final List<Step> steps = sortedGames.get(0);
            final Step lastStep = steps.get(steps.size() - 1);
            if(lastStep.win && lastStep.stamp != stamp) {
                return lastStep.point;
            } else {
                return steps.get(0).point;
            }
        }

        return getRandomFreePoint(field);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stamp getStamp() {
        return stamp;
    }

    @Override
    public String toString() {
        return "SmartComputerPlayer{" +
                "stamp=" + stamp +
                ", name='" + name + '\'' +
                '}';
    }

    private static void simulateGameOptions(
            final Field field,
            final Stamp player,
            final List<Step> steps,
            final Consumer<List<Step>> handleGameTerminated,
            final int depht
    ) {
        if (depht == 0) {
            return;
        }
        for (final Point point : field.getFreePoints()) {
            final Field nextField = field.with(point, player);
            final boolean win = nextField.checkWin(player);
            final List<Step> nextSteps = addStep(steps, new Step(player, point, win));
            if (win || nextField.getFreePoints().isEmpty()) {
                handleGameTerminated.accept(nextSteps);
                return;
            }
            simulateGameOptions(nextField, player.opponent(), nextSteps, handleGameTerminated, depht - 1);
        }
    }

    private static List<Step> addStep(final List<Step> path, final Step step) {
        final ArrayList<Step> result = new ArrayList<>(path);
        result.add(step);
        return result;
    }

    private static class Step {
        final Stamp stamp;
        final Point point;
        final boolean win;

        Step(final Stamp stamp, final Point point, final boolean win) {
            this.stamp = stamp;
            this.point = point;
            this.win = win;
        }

        @Override
        public String toString() {
            return "Step{" +
                    "stamp=" + stamp +
                    ", point=" + point +
                    ", win=" + win +
                    '}';
        }
    }
}
