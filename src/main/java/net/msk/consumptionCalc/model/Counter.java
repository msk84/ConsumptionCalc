package net.msk.consumptionCalc.model;

import java.io.Serializable;

public record Counter(String counterName, Unit unit) implements Serializable {
}
