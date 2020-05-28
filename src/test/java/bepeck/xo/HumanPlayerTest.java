package bepeck.xo;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class HumanPlayerTest {

    @Test
    public void testNameAndStamp() {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[]{});
        PrintStream ps = new PrintStream(new ByteArrayOutputStream());

        HumanPlayer player = new HumanPlayer(in, ps, "vasya", Stamp.X);

        Assert.assertEquals("vasya", player.getName());
        Assert.assertEquals(Stamp.X, player.getStamp());
    }

    @Test
    public void testNextStepHappyPath() {
        ByteArrayInputStream in = new ByteArrayInputStream("1\n0\n".getBytes());
        PrintStream ps = new PrintStream(new ByteArrayOutputStream());

        HumanPlayer player = new HumanPlayer(in, ps, "vasya", Stamp.X);

        Assert.assertEquals("vasya", player.getName());
        Assert.assertEquals(Stamp.X, player.getStamp());

        Point point = player.nextStep(new Field(1));

        Assert.assertEquals(new Point(1, 0), point);
    }
}
