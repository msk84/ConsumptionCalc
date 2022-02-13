package net.msk.consumptionCalc.model;

public enum Unit {
    m3("m³"),
    kWh("kWh");

    Unit(final String stringValue) {
        this.stringValue = stringValue;
    }

    private final String stringValue;

    @Override
    public String toString() {
        return this.stringValue;
    }
}
