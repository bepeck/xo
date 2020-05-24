package bepeck.xo;

interface Player {
    Point nextStep(Field field);

    String getName();

    Stamp getStamp();
}
