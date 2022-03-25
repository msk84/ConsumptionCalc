package net.msk.consumptionCalc.service;

import net.msk.consumptionCalc.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvaluationServiceTest {

    final EvaluationService evaluationService = new EvaluationService();

    @Test
    void evaluateSimpleByTimeframe() {
        final List<RawCounterDataRow> counterData = new ArrayList<>();
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2021,12,31,23,59), false, 0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,3,31,23,59), false, 900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,10,23,59), false, 1900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,11,12,2), false, 2020.0, ""));

        final RawCounterData rawCounterData = new RawCounterData(counterData);
        final EvaluationData evaluationData = this.evaluationService.evaluateSimple("UnitTest", rawCounterData, EvaluationMode.Timeframe);
        assertNotNull(evaluationData);

        final List<EvaluationColumn> evaluationColumns = evaluationData.evaluationColumns();
        assertEquals("columnTotal", evaluationColumns.get(0).headerQualifier());
        assertEquals("columnPerDay", evaluationColumns.get(1).headerQualifier());
        assertEquals("columnPerHour", evaluationColumns.get(2).headerQualifier());

        final List<EvaluationDataRow> evaluationDataRows = evaluationData.evaluationDataRows();
        // Totals
        assertEquals(900.0, Double.parseDouble(evaluationDataRows.get(0).columnData().get(0)), 0.01);
        assertEquals(1000.0, Double.parseDouble(evaluationDataRows.get(1).columnData().get(0)), 0.01);
        assertEquals(120.0, Double.parseDouble(evaluationDataRows.get(2).columnData().get(0)), 0.01);

        //PerDay
        assertEquals(10.0, Double.parseDouble(evaluationDataRows.get(0).columnData().get(1)), 0.01);
        assertEquals(100.0, Double.parseDouble(evaluationDataRows.get(1).columnData().get(1)), 0.01);
        assertNull(evaluationDataRows.get(2).columnData().get(1)); // It's not even a day

        //PerHour
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(0).columnData().get(2)), 0.01);
        assertEquals(4.16, Double.parseDouble(evaluationDataRows.get(1).columnData().get(2)), 0.01);
        // assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(2).columnData().get(2)), 0.01);
    }

    @Test
    void evaluateCounterExchangeByTimeframe() {
        final List<RawCounterDataRow> counterData = new ArrayList<>();
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2021,12,31,23,59), false, 0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,3,31,23,59), false, 900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,1,0,1), true, 0.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,6,30,23,59), false, 900.0, ""));

        final RawCounterData rawCounterData = new RawCounterData(counterData);
        final EvaluationData evaluationData = this.evaluationService.evaluateSimple("UnitTest", rawCounterData, EvaluationMode.Timeframe);
        assertNotNull(evaluationData);

        final List<EvaluationColumn> evaluationColumns = evaluationData.evaluationColumns();
        assertEquals("columnTotal", evaluationColumns.get(0).headerQualifier());
        assertEquals("columnPerDay", evaluationColumns.get(1).headerQualifier());
        assertEquals("columnPerHour", evaluationColumns.get(2).headerQualifier());

        final List<EvaluationDataRow> evaluationDataRows = evaluationData.evaluationDataRows();
        // Totals
        assertEquals(900.0, Double.parseDouble(evaluationDataRows.get(0).columnData().get(0)), 0.01);
        assertNull(evaluationDataRows.get(1).columnData().get(0));
        assertEquals(900.0, Double.parseDouble(evaluationDataRows.get(2).columnData().get(0)), 0.01);

        //PerDay
        assertEquals(10.0, Double.parseDouble(evaluationDataRows.get(0).columnData().get(1)), 0.01);
        assertNull(evaluationDataRows.get(1).columnData().get(1));
        assertEquals(9.89, Double.parseDouble(evaluationDataRows.get(2).columnData().get(1)), 0.01);

        //PerHour
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(0).columnData().get(2)), 0.01);
        assertNull(evaluationDataRows.get(1).columnData().get(2));
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(2).columnData().get(2)), 0.01);
    }

    @Test
    void evaluateSimpleByMonth() {
        final List<RawCounterDataRow> counterData = new ArrayList<>();
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2021,12,31,23,59), false, 0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,3,31,23,59), false, 900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,10,23,59), false, 1900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,11,12,2), false, 2020.0, ""));

        final RawCounterData rawCounterData = new RawCounterData(counterData);
        final EvaluationData evaluationData = this.evaluationService.evaluateSimple("UnitTest", rawCounterData, EvaluationMode.Month);
        assertNotNull(evaluationData);

        final List<EvaluationColumn> evaluationColumns = evaluationData.evaluationColumns();
        assertEquals("columnTotal", evaluationColumns.get(0).headerQualifier());
        assertEquals("columnPerDay", evaluationColumns.get(1).headerQualifier());
        assertEquals("columnPerHour", evaluationColumns.get(2).headerQualifier());

        final List<EvaluationDataRow> evaluationDataRows = evaluationData.evaluationDataRows();
        // Totals
        assertEquals(310.0, Double.parseDouble(evaluationDataRows.get(0).columnData().get(0)), 0.01);
        assertEquals(280.0, Double.parseDouble(evaluationDataRows.get(1).columnData().get(0)), 0.01);
        assertEquals(310.0, Double.parseDouble(evaluationDataRows.get(2).columnData().get(0)), 0.01);
        assertEquals(1120.0, Double.parseDouble(evaluationDataRows.get(3).columnData().get(0)), 0.01);

        //PerDay
        assertEquals(10.33, Double.parseDouble(evaluationDataRows.get(0).columnData().get(1)), 0.01);
        assertEquals(10.37, Double.parseDouble(evaluationDataRows.get(1).columnData().get(1)), 0.01);
        assertEquals(10.33, Double.parseDouble(evaluationDataRows.get(2).columnData().get(1)), 0.01);
        assertEquals(112.0, Double.parseDouble(evaluationDataRows.get(3).columnData().get(1)), 0.01);

        //PerHour
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(0).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(1).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(2).columnData().get(2)), 0.01);
        assertEquals(4.44, Double.parseDouble(evaluationDataRows.get(3).columnData().get(2)), 0.01);
    }

    @Test
    void evaluateCounterExchangeByMonth() {
        final List<RawCounterDataRow> counterData = new ArrayList<>();
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2021,12,31,23,59), false, 0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,3,31,23,59), false, 900.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,4,1,0,1), true, 0.0, ""));
        counterData.add(new RawCounterDataRow(LocalDateTime.of(2022,6,30,23,59), false, 900.0, ""));

        final RawCounterData rawCounterData = new RawCounterData(counterData);
        final EvaluationData evaluationData = this.evaluationService.evaluateSimple("UnitTest", rawCounterData, EvaluationMode.Month);
        assertNotNull(evaluationData);

        final List<EvaluationColumn> evaluationColumns = evaluationData.evaluationColumns();
        assertEquals("columnTotal", evaluationColumns.get(0).headerQualifier());
        assertEquals("columnPerDay", evaluationColumns.get(1).headerQualifier());
        assertEquals("columnPerHour", evaluationColumns.get(2).headerQualifier());

        final List<EvaluationDataRow> evaluationDataRows = evaluationData.evaluationDataRows();
        // Totals
        assertEquals(310.0, Double.parseDouble(evaluationDataRows.get(0).columnData().get(0)), 0.01);
        assertEquals(280.0, Double.parseDouble(evaluationDataRows.get(1).columnData().get(0)), 0.01);
        assertEquals(310.0, Double.parseDouble(evaluationDataRows.get(2).columnData().get(0)), 0.01);
        assertEquals(296.70, Double.parseDouble(evaluationDataRows.get(3).columnData().get(0)), 0.01);
        assertEquals(306.59, Double.parseDouble(evaluationDataRows.get(4).columnData().get(0)), 0.01);
        assertEquals(296.70, Double.parseDouble(evaluationDataRows.get(5).columnData().get(0)), 0.01);

        //PerDay
        assertEquals(10.33, Double.parseDouble(evaluationDataRows.get(0).columnData().get(1)), 0.01);
        assertEquals(10.37, Double.parseDouble(evaluationDataRows.get(1).columnData().get(1)), 0.01);
        assertEquals(10.33, Double.parseDouble(evaluationDataRows.get(2).columnData().get(1)), 0.01);
        assertEquals(10.23, Double.parseDouble(evaluationDataRows.get(3).columnData().get(1)), 0.01);
        assertEquals(10.21, Double.parseDouble(evaluationDataRows.get(4).columnData().get(1)), 0.01);
        assertEquals(9.89, Double.parseDouble(evaluationDataRows.get(5).columnData().get(1)), 0.01);

        //PerHour
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(0).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(1).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(2).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(3).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(4).columnData().get(2)), 0.01);
        assertEquals(0.41, Double.parseDouble(evaluationDataRows.get(5).columnData().get(2)), 0.01);
    }
}