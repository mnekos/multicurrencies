package pl.mnekos.multicurrencies.api;

public class ResponseAmount {

    private final double amount;

    public ResponseAmount(double amount) {
        this.amount = amount;
    }

    public double get() {
        return amount;
    }
}
