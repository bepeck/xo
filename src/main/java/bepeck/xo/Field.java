package bepeck.xo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;

public class Field {

    private final Map<Point, Stamp> value;

    private Field(final Map<Point, Stamp> value) {
        this.value = Collections.unmodifiableMap(value);
    }

    public Field(final int size) {
        this(prepareInitialFieldValue(size));
    }

    public Stamp getStamp(final Point point) {
        checkPoint(point);
        return value.get(point);
    }

    public boolean checkWin(final Set<Point> win, final Stamp stamp) {
        return win.stream().allMatch(point -> value.get(point) == stamp);
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

    public Field with(final Point point, final Stamp player) {
        requireNonNull(player);
        checkPoint(point);
        final Stamp existingStamp = value.get(point);
        if (existingStamp == null) {
            final Map<Point, Stamp> value = new HashMap<>(this.value);
            value.put(point, player);
            return new Field(value);
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

    public static Set<Set<Point>> generateWins(final int size) {
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
}