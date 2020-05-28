package bepeck.xo;

import org.junit.Assert;
import org.junit.Test;

public class PointTest {

    @Test
    public void equalsTest() {
        Assert.assertEquals("x and y are the same", new Point(1, 1), new Point(1, 1));
        Assert.assertNotEquals("x is different", new Point(1, 1), new Point(2, 1));
        Assert.assertNotEquals("y is different", new Point(1, 1), new Point(1, 2));
    }

    @Test
    public void hashCodeTest() {
        Assert.assertEquals(
                "x and y are the same",
                new Point(1, 1).hashCode(),
                new Point(1, 1).hashCode()
        );
        Assert.assertNotEquals("" +
                        "x is different",
                new Point(1, 1).hashCode(),
                new Point(2, 1).hashCode()
        );
        Assert.assertNotEquals(
                "y is different",
                new Point(1, 1).hashCode(),
                new Point(1, 2).hashCode()
        );
    }
}
