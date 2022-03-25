package net.msk.consumptionCalc.model;

import java.time.LocalDateTime;

public record RawCounterDataRow(LocalDateTime timestamp, boolean counterExchange, double value, String comment){
}
