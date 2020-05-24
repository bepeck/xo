package bepeck.xo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static bepeck.xo.StupidComputerPlayer.getRandomFreePoint;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

public class SmartComputerPlayer implements Player {

    private final Stamp stamp;
    private final String name;

    public SmartComputerPlayer(final String name, final Stamp stamp) {
        this.name = requireNonNull(name);
        this.stamp = requireNonNull(stamp);
    }

    @Override
    public Point nextStep(final Field field) {
        final Set<Set<Point>> wins = field.getWins();

        final int maxStepsToWin = getMaxStepsToWin(wins);

        final Map<Boolean, List<WinInfo>> myWinsToWinInfos = wins
                .stream()
                .flatMap(win -> toWinInfo(win, field).stream())
                .collect(groupingBy(winInfo -> winInfo.stamp == stamp));

        final Map<Integer, List<WinInfo>> stepsToWinToMyWins = ofNullable(myWinsToWinInfos.get(true))
                .orElse(emptyList())
                .stream()
                .collect(groupingBy(winInfo -> winInfo.stepsToWin));

        final Map<Integer, List<WinInfo>> stepsToWinToOpponentWins = ofNullable(myWinsToWinInfos.get(false))
                .orElse(emptyList())
                .stream()
                .collect(groupingBy(winInfo -> winInfo.stepsToWin));

        for (int stepsToWin = 1; stepsToWin <= maxStepsToWin; stepsToWin++) {
            final Point myBetterStep = getBetterStep(stepsToWinToMyWins.get(stepsToWin));
            if (myBetterStep != null) {
                return myBetterStep;
            }
            final Point opponentBetterStep = getBetterStep(stepsToWinToOpponentWins.get(stepsToWin));
            if (opponentBetterStep != null) {
                return opponentBetterStep;
            }
        }
        return getRandomFreePoint(field);
    }

    private Point getBetterStep(final List<WinInfo> winInfos) {
        if (winInfos == null) {
            return null;
        }
        Map<Point, Long> collect = winInfos.stream()
                .collect(groupingBy(winInfo -> winInfo.step, counting()));
        return collect
                .entrySet()
                .stream()
                .max(comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private int getMaxStepsToWin(final Set<Set<Point>> wins) {
        return wins.stream().mapToInt(Set::size).max().orElseThrow(() -> new RuntimeException("impossible"));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stamp getStamp() {
        return stamp;
    }

    private Set<WinInfo> toWinInfo(final Set<Point> win, final Field field) {
        Stamp stamp = null;
        final List<Point> freePoints = new ArrayList<>();
        for (final Point point : win) {
            final Stamp existingStamp = field.getStamp(point);
            if (existingStamp == null) {
                freePoints.add(point);
            } else if (stamp == null) {
                stamp = existingStamp;
            } else if (stamp != existingStamp) {
                return emptySet();
            }
        }
        final Collection<Stamp> winners;
        if (stamp == null) {
            winners = asList(Stamp.values());
        } else {
            winners = singletonList(stamp);
        }
        return winners.stream().flatMap(possibleWinnerStamp -> freePoints.stream().map(freePoint -> new WinInfo(
                freePoint,
                possibleWinnerStamp,
                freePoints.size()
        ))).collect(toSet());
    }

    private static class WinInfo {
        final Point step;
        final Stamp stamp;
        final int stepsToWin;

        WinInfo(final Point step, final Stamp stamp, final int stepsToWin) {
            this.step = requireNonNull(step);
            this.stamp = requireNonNull(stamp);
            if (stepsToWin < 0) {
                throw new IllegalArgumentException();
            }
            this.stepsToWin = stepsToWin;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final WinInfo winInfo = (WinInfo) o;
            return stepsToWin == winInfo.stepsToWin &&
                    Objects.equals(step, winInfo.step) &&
                    stamp == winInfo.stamp;
        }

        @Override
        public int hashCode() {
            return Objects.hash(step, stamp, stepsToWin);
        }

        @Override
        public String toString() {
            return "WinInfo{" +
                    "step=" + step +
                    ", stamp=" + stamp +
                    ", stepsToWin=" + stepsToWin +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SmartComputerPlayer{" +
                "stamp=" + stamp +
                ", name='" + name + '\'' +
                '}';
    }
}
