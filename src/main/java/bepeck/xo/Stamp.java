package bepeck.xo;

public enum Stamp {
    X {
        @Override
        Stamp opponent() {
            return O;
        }
    },
    O {
        @Override
        Stamp opponent() {
            return X;
        }
    };

    abstract Stamp opponent();
}
