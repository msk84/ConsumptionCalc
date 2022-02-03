package net.msk.consumptionCalc.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record RawCounterDataRow(LocalDate date, LocalTime time, double value){
}
