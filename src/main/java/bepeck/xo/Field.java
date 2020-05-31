package bepeck.xo;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class Field {

    private final Map<Point, Stamp> value;
    private final Set<Set<Point>> wins;
    private final int size;

    private Field(final int size, final Map<Point, Stamp> value, final Set<Set<Point>> wins) {
        this.size = size;
        this.value = unmodifiableMap(value);
        this.wins = unmodifiableSet(wins);
    }

    public Field(final int size) {
        this(size, prepareInitialFieldValue(size), generateWins(size));
    }

    public Stamp getStamp(final Point point) {
        checkPoint(point);
        return value.get(point);
    }

    boolean checkWin(final Stamp stamp) {
        return wins.stream().anyMatch(win -> win.stream().allMatch(point -> value.get(point) == stamp));
    }

    public Set<Point> getPoints() {
        return value.keySet();
    }

    public Set<Point> getFreePoints() {
        return value.entrySet()
                .stream()
                .filter(entry -> entry.getValue() == null)
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    public Set<Point> getBusyPoints() {
        return value.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey)
                .collect(toSet());
    }

    public Field with(final Point point, final Stamp player) {
        requireNonNull(player);
        checkPoint(point);
        final Stamp existingStamp = value.get(point);
        if (existingStamp == null) {
            final Map<Point, Stamp> value = new HashMap<>(this.value);
            value.put(point, player);
            return new Field(size, value, wins);
        } else {
            throw new RuntimeException("point " + point + " is busy by " + existingStamp);
        }
    }

    private void checkPoint(final Point point) {
        requireNonNull(point);
        if (!value.containsKey(point)) {
            throw new RuntimeException("point " + point + " is invalid");
        }
    }

    private static void checkSize(final int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size should be > 0");
        }
    }

    private static Map<Point, Stamp> prepareInitialFieldValue(final int size) {
        checkSize(size);
        final Map<Point, Stamp> value = new HashMap<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                value.put(new Point(i, j), null);
            }
        }
        return value;
    }

    private static Set<Set<Point>> generateWins(final int size) {
        checkSize(size);
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
        ).flatMap(Collection::stream).map(Collections::unmodifiableSet).collect(toSet());
    }

    public void print(final PrintStream ps) {
        for (int column = 0; column < size; column++) {
            for (int row = 0; row < size; row++) {
                ps.print("|");
                final Stamp stamp = getStamp(new Point(row, column));
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
    public String toString() {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        print(new PrintStream(baos));
        return new String(baos.toByteArray());
    }
}