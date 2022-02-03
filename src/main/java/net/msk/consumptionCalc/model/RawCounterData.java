package net.msk.consumptionCalc.model;

import java.util.List;

public record RawCounterData(List<RawCounterDataRow> counterData) {
}
