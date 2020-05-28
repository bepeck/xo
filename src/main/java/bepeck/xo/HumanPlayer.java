package bepeck.xo;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.util.Objects.requireNonNull;

public class HumanPlayer implements Player {

    private final Scanner scanner;
    private final PrintStream out;
    private final String name;
    private final Stamp stamp;

    public HumanPlayer(final InputStream in, final PrintStream out, final String name, final Stamp stamp) {
        this.scanner = new Scanner(requireNonNull(in));
        this.out = requireNonNull(out);
        this.name = requireNonNull(name);
        this.stamp = requireNonNull(stamp);
    }

    public Point nextStep(final Field field) {
        while (true) {
            try {
                out.print("pls type column number: ");
                final int column = scanner.nextInt();
                out.print("pls type row number:    ");
                final int row = scanner.nextInt();
                return new Point(column, row);
            } catch (final InputMismatchException e) {
                out.println("wrong input, try again");
            } finally {
                scanner.skip(".*");
            }
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Stamp getStamp() {
        return stamp;
    }
}
