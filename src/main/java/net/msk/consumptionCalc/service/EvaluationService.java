package net.msk.consumptionCalc.service;

import net.msk.consumptionCalc.model.*;
import net.msk.consumptionCalc.persistence.file.CsvService;
import net.msk.consumptionCalc.persistence.file.FileSystemService;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {

    private final DataService dataService;

    public EvaluationService(final DataService dataService) {
        this.dataService = dataService;
    }

    public String evaluateSimple(final String project, final RawCounterData rawCounterData) throws DataPersistanceException {
        final List<EvaluationDataRow> dataList = new ArrayList<>();
        for(int i = 0; i < rawCounterData.counterData().size() - 1; i++) {
            final List<String> data = new ArrayList<>();
            final RawCounterDataRow last = rawCounterData.counterData().get(i);
            final RawCounterDataRow current = rawCounterData.counterData().get(i+1);

            final long days = ChronoUnit.DAYS.between(last.timestamp(), current.timestamp());

            final double consumption = current.value() - last.value();
            data.add(Double.toString(consumption));

            final double consumptionPerDay = consumption / days;
            data.add(Double.toString(consumptionPerDay));

            dataList.add(new EvaluationDataRow(last.timestamp(), current.timestamp(), data));
        }

        final List<EvaluationColumn> columns = new ArrayList<>();
        columns.add(new EvaluationColumn("Total"));
        columns.add(new EvaluationColumn("Per day"));

        final EvaluationData evaluationData = new EvaluationData(LocalDateTime.now(), columns, dataList);

        final String fileUuid = UUID.randomUUID().toString();
        this.dataService.saveEvaluationSimpleData(project, fileUuid, evaluationData);

        return fileUuid;
    }

}
