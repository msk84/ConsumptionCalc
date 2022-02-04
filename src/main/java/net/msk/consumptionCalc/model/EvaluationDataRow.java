package net.msk.consumptionCalc.model;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluationDataRow(LocalDateTime from, LocalDateTime until, List<String> columnData) {
}
