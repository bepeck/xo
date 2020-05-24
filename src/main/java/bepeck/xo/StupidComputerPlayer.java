package bepeck.xo;

import java.util.Random;
import java.util.Set;

import static java.util.Objects.requireNonNull;

class StupidComputerPlayer implements Player {

    private final Stamp stamp;
    private final String name;

    StupidComputerPlayer(final String name, final Stamp stamp) {
        this.stamp = requireNonNull(stamp);
        this.name = requireNonNull(name);
    }

    @Override
    public Point nextStep(final Field field) {
        return getRandomFreePoint(field);
    }

    @Override
    public String getName() {
        return "stupid player - " + name;
    }

    @Override
    public Stamp getStamp() {
        return stamp;
    }

    public static Point getRandomFreePoint(final Field field) {
        final Set<Point> freePoints = field.getFreePoints();
        if (freePoints.isEmpty()) {
            throw new RuntimeException("no free points");
        }
        final int next = new Random().nextInt(freePoints.size());
        return freePoints.toArray(new Point[]{})[next];
    }
}
