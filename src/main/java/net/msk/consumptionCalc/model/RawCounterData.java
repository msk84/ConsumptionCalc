package net.msk.consumptionCalc.model;

import java.util.ArrayList;
import java.util.List;

public record RawCounterData(List<RawCounterDataRow> counterData) {
    public RawCounterData() {
        this(new ArrayList<>());
    }
}
