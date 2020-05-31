package bepeck.xo;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public class ExampleForAG {
    public static void main(String[] args) {
        final Integer result = readParam(
                System.in,
                System.out,
                "Первым ходит игрок 1. Выберите его (0 - человек, 1 - компьютер): ",
                scanner -> scanner.nextInt(),
                value -> value == 0 || value == 1
        );
        System.out.println("valid input is:" + result);

        final Integer result2 = readParam(
                System.in,
                System.out,
                "Первым ходит игрок 1. Выберите его (0 - человек, 1 - компьютер): ",
                scanner -> scanner.nextInt(),
                value -> value == 0 || value == 1
        );
        System.out.println("valid input is:" + result2);
    }

    public static <T> T readParam(
            InputStream in,
            PrintStream out,
            String description,
            Function<Scanner, T> mapper,
            Predicate<T> validate
    ) {
        final Scanner scanner = new Scanner(in);
        while (true) {
            try {
                out.println(description);
                final T param = mapper.apply(scanner);
                if (validate.test(param)) {
                    return param;
                }
            } catch (final Exception e) {
                // ignore
            }
            scanner.skip(".*");
        }
    }
}
