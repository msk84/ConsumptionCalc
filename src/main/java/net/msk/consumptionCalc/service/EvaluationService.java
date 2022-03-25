package net.msk.consumptionCalc.service;

import net.msk.consumptionCalc.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class EvaluationService {

    static final DateTimeFormatter MAP_KEY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    public EvaluationData evaluateSimple(final String project, final RawCounterData rawCounterData,
                                         final EvaluationMode evaluationMode) {
        if(EvaluationMode.Month.equals(evaluationMode)) {
            return this.evaluateByMonth(project, rawCounterData);
        }
        else {
            return this.evaluateTimeframe(project, rawCounterData);
        }
    }

    private EvaluationData evaluateTimeframe(final String project, final RawCounterData rawCounterData) {
        final List<EvaluationDataRow> dataList = new ArrayList<>();
        for(int i = 0; i < rawCounterData.counterData().size() - 1; i++) {
            final List<String> columnData = new ArrayList<>();
            final RawCounterDataRow lastMeasurement = rawCounterData.counterData().get(i);
            final RawCounterDataRow currentMeasurement = rawCounterData.counterData().get(i+1);

            final LocalDateTime startDateTime = lastMeasurement.timestamp();
            final LocalDateTime endDateTime = currentMeasurement.timestamp();

            if(currentMeasurement.counterExchange()) {
                columnData.add(null);
                columnData.add(null);
                columnData.add(null);
                dataList.add(new EvaluationDataRow(startDateTime, endDateTime, columnData));
                continue;
            }

            final double consumption = currentMeasurement.value() - lastMeasurement.value();
            columnData.add(Double.toString(consumption));

            if(ChronoUnit.DAYS.between(startDateTime, endDateTime) < 1) {
                columnData.add(null);
                columnData.add(null); //Todo: Why not calculating the hours?
            }
            else {
                long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime) +
                        ((startDateTime.getMinute() < 30 && endDateTime.getMinute() > 30) ? 1 : 0);

                final double consumptionPerHour = consumption / hours;
                final double consumptionPerDay = consumptionPerHour * 24;
                columnData.add(Double.toString(consumptionPerDay));
                columnData.add(Double.toString(consumptionPerHour));
            }
            dataList.add(new EvaluationDataRow(startDateTime, endDateTime, columnData));
        }

        final List<EvaluationColumn> columns = new ArrayList<>();
        columns.add(new EvaluationColumn("columnTotal"));
        columns.add(new EvaluationColumn("columnPerDay"));
        columns.add(new EvaluationColumn("columnPerHour"));

        final UUID evaluationUuid = UUID.randomUUID();
        return new EvaluationData(evaluationUuid, project, EvaluationMode.Timeframe, LocalDateTime.now(), columns, dataList);
    }

    private EvaluationData evaluateByMonth(final String project, final RawCounterData rawCounterData) {
        final UUID evaluationUuid = UUID.randomUUID();
        final List<EvaluationDataRow> dataList = new ArrayList<>();
        final List<EvaluationColumn> columns = new ArrayList<>();
        columns.add(new EvaluationColumn("columnTotal"));
        columns.add(new EvaluationColumn("columnPerDay"));
        columns.add(new EvaluationColumn("columnPerHour"));

        if(rawCounterData.counterData().size() == 0) {
            return new EvaluationData(evaluationUuid, project, EvaluationMode.Month, LocalDateTime.now(), columns, dataList);
        }

        final Map<String, Double> consumptionPerMonth = new LinkedHashMap<>();

        for(int i = 0; i < rawCounterData.counterData().size() - 1; i++) {
            final RawCounterDataRow lastMeasurement = rawCounterData.counterData().get(i);
            final RawCounterDataRow currentMeasurement = rawCounterData.counterData().get(i+1);

            if(currentMeasurement.counterExchange()) {
                continue;
            }

            final LocalDateTime startDateTime = lastMeasurement.timestamp();
            final LocalDateTime endDateTime = currentMeasurement.timestamp();

            final double consumption = currentMeasurement.value() - lastMeasurement.value();

            if(ChronoUnit.DAYS.between(startDateTime, endDateTime) < 1) {
                consumptionPerMonth.compute(startDateTime.format(MAP_KEY_FORMAT), (key, value) -> (value == null) ? consumption : Double.sum(value, consumption));
                continue;
            }

            long hours = ChronoUnit.HOURS.between(startDateTime, endDateTime) +
                    ((startDateTime.getMinute() < 30 && endDateTime.getMinute() > 30) ? 1 : 0);

            final double consumptionPerHour = consumption / hours;
            final double consumptionPerDay = consumptionPerHour * 24;

            final double consumptionStartDay = (24 - (startDateTime.getHour() + ((startDateTime.getMinute() < 30) ? 0 : 1))) * consumptionPerHour;
            final double consumptionEndDay = (endDateTime.getHour() + ((endDateTime.getMinute() < 30) ? 0 : 1)) * consumptionPerHour;
            if(consumptionStartDay > 0.0) {
                consumptionPerMonth.compute(startDateTime.format(MAP_KEY_FORMAT), (key, value) -> (value == null) ? consumptionStartDay : Double.sum(value, consumptionStartDay));
            }

            for(LocalDateTime currentDay = startDateTime.truncatedTo(ChronoUnit.DAYS).plusDays(1); currentDay.isBefore(endDateTime.truncatedTo(ChronoUnit.DAYS)); currentDay = currentDay.plusDays(1)) {
                consumptionPerMonth.compute(currentDay.format(MAP_KEY_FORMAT), (key, value) -> (value==null) ? consumptionPerDay : Double.sum(value, consumptionPerDay));
            }

            if(consumptionEndDay > 0.0) {
                consumptionPerMonth.compute(endDateTime.format(MAP_KEY_FORMAT), (key, value) -> (value == null) ? consumptionEndDay : Double.sum(value, consumptionEndDay));
            }
        }

        final LocalDateTime firstMeasurementTimestamp = this.applyHourRoundingLogic(rawCounterData.counterData().get(0).timestamp());
        final LocalDateTime lastMeasurementTimestamp = this.applyHourRoundingLogic(rawCounterData.counterData().get(rawCounterData.counterData().size() - 1).timestamp());

        int i = 0;
        for(final String currentKey : consumptionPerMonth.keySet()) {
            final LocalDate fromDate = LocalDate.parse(currentKey + "-01", DateTimeFormatter.ISO_DATE);
            final LocalDate untilDate = LocalDate.parse(currentKey + "-01", DateTimeFormatter.ISO_DATE);
            final LocalDateTime from = (i == 0) ? firstMeasurementTimestamp : LocalDateTime.from(fromDate.atStartOfDay()).withDayOfMonth(1);
            final LocalDateTime until = (i == consumptionPerMonth.size() - 1) ? lastMeasurementTimestamp : LocalDateTime.from(untilDate.atTime(23, 59)).with(TemporalAdjusters.lastDayOfMonth());

            final double totalMonthlyConsumption = consumptionPerMonth.get(currentKey);

            final long days = ChronoUnit.DAYS.between(from, until);
            final double consumptionPerDay = (days == 0) ? totalMonthlyConsumption : totalMonthlyConsumption / days;

            final long hours = ChronoUnit.HOURS.between(from, until);
            final double consumptionPerHour = (hours == 0) ? totalMonthlyConsumption : totalMonthlyConsumption / hours;

            final List<String> columnValues = new ArrayList<>();
            columnValues.add(Double.toString(totalMonthlyConsumption));
            columnValues.add(Double.toString(consumptionPerDay));
            columnValues.add(Double.toString(consumptionPerHour));

            dataList.add(new EvaluationDataRow(from, until, columnValues));
            i++;
        }

        return new EvaluationData(evaluationUuid, project, EvaluationMode.Month, LocalDateTime.now(), columns, dataList);
    }

    private LocalDateTime applyHourRoundingLogic(final LocalDateTime timestamp) {
        if(timestamp.getMinute() < 30) {
            return timestamp.truncatedTo(ChronoUnit.HOURS);
        }
        else {
            return timestamp.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        }
    }
}
