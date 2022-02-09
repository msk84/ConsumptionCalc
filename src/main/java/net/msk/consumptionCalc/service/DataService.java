package net.msk.consumptionCalc.service;

import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.EvaluationMode;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import net.msk.consumptionCalc.persistence.file.CsvService;
import net.msk.consumptionCalc.persistence.file.FileSystemService;
import net.msk.consumptionCalc.service.exception.DataLoadingException;
import net.msk.consumptionCalc.service.exception.DataPersistanceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    private final CsvService csvService;
    private final FileSystemService fileSystemService;
    private final EvaluationService evaluationService;

    public DataService(final CsvService csvService, final FileSystemService fileSystemService, final EvaluationService evaluationService) {
        this.csvService = csvService;
        this.fileSystemService = fileSystemService;
        this.evaluationService = evaluationService;
    }

    public RawCounterData getRawCounterData(final String project, final String counter, final String period) throws DataLoadingException {
        try {
            final Path dataFilePath = this.fileSystemService.getRawDataFile(project, counter, period);
            return this.csvService.loadRawCounterData(dataFilePath);
        } catch (final IOException e) {
            throw new DataLoadingException("Failed to load rawCounterData.", e);
        }
    }

    public void addCounterData(final String project, final String counter, final String period, final RawCounterDataRow rawCounterDataRow) throws DataPersistanceException {
        try {
            final Path dataFilePath = this.fileSystemService.getRawDataFile(project, counter, period);
            final RawCounterData rawCounterData = this.csvService.loadRawCounterData(dataFilePath);
            rawCounterData.counterData().add(rawCounterDataRow);
            final List<RawCounterDataRow> sortedList = rawCounterData.counterData().stream()
                    .sorted(Comparator.comparing(RawCounterDataRow::timestamp))
                    .toList();

            final RawCounterData newRawCounterData = new RawCounterData(sortedList);
            this.csvService.persistRawCounterData(dataFilePath, newRawCounterData);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to addCounterData.", e);
        }
    }
    public EvaluationData evaluateSimple(final String project, final String counter, final String period, final EvaluationMode evaluationMode) throws DataLoadingException {
        final RawCounterData rawCounterData = this.getRawCounterData(project, counter, period);
        return this.evaluationService.evaluateSimple(project, rawCounterData, evaluationMode);
    }
}
