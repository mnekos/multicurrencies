package pl.mnekos.multicurrencies.data;

public class Amount {

    private volatile double amount;

    public Amount(double amount) {
        this.amount = amount;
    }

    public double get() {
        return amount;
    }

    protected synchronized boolean set(double amount) {
        if(canSet(amount)) {
            this.amount = amount;
            return true;
        } else {
            return false;
        }
    }

    protected synchronized boolean add(double amount) {
        if(canAdd(amount)) {
            this.amount += amount;
            return true;
        } else {
            return false;
        }
    }

    protected synchronized boolean remove(double amount) {
        if(canRemove(amount)) {
            this.amount -= amount;
            return true;
        } else {
            return false;
        }
    }

    public boolean canAdd(double value) {
        return true;
    }

    public boolean canSet(double value) {
        return value >= 0;
    }

    public boolean canRemove(double value) {
        return this.amount >= value;
    }
}
