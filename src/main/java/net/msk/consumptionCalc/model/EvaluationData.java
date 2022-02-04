package net.msk.consumptionCalc.model;

import java.time.LocalDateTime;
import java.util.List;

public record EvaluationData(LocalDateTime evaulationTimestamp,
                             List<EvaluationColumn> evaluationColumns,
                             List<EvaluationDataRow> evaluationDataRows) {
}
