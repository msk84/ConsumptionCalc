package net.msk.consumptionCalc.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record EvaluationData(UUID evaluationId,
                             String project,
                             LocalDateTime evaluationTimestamp,
                             List<EvaluationColumn> evaluationColumns,
                             List<EvaluationDataRow> evaluationDataRows) {
}
