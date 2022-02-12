package net.msk.consumptionCalc.service;

import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.EvaluationMode;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationServiceTest {

    final EvaluationService evaluationService = new EvaluationService();

    @Test
    void evaluateSimpleByMonth() {
        final List<RawCounterDataRow> counterData = new ArrayList<>();
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2021,12,31,23,59), 0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,3,31,23,59), 900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,10,23,59), 1900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,11,12,2), 2020.0, ""));

        final RawCounterData rawCounterData = new RawCounterData(counterData);
        final EvaluationData evaluationData = this.evaluationService.evaluateSimple("UnitTest", rawCounterData, EvaluationMode.Month);
        assertNotNull(evaluationData);
    }
}