package net.msk.consumptionCalc.model;

import java.time.LocalDateTime;

public record RawCounterDataRow(LocalDateTime timestamp, double value, String comment){
}
