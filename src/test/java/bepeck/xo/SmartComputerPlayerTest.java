package bepeck.xo;

import org.junit.Test;

import static org.junit.Assert.*;

public class SmartComputerPlayerTest {

    @Test
    public void nextStep() {
        Field field = new Field(3)
                .with(new Point(0, 0), Stamp.X)
                .with(new Point(1, 0), Stamp.O)
                .with(new Point(2, 0), Stamp.X)

                .with(new Point(2, 1), Stamp.O)

                .with(new Point(0, 2), Stamp.X)
                .with(new Point(2, 2), Stamp.O);

        System.out.println(field);

        Point point = new SmartComputerPlayer("O", Stamp.O).nextStep(field);

        System.out.println(field.with(point, Stamp.O));
    }
}