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

    public void addProject(final String name) throws DataPersistanceException {
        try {
            this.fileSystemService.addProject(name);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to create project folder.", e);
        }
    }

    public void addCounter(final String project, final String counterName) throws DataPersistanceException {
        try {
            this.fileSystemService.addCounter(project, counterName);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to create counter folder.", e);
        }
    }

    public RawCounterData getRawCounterData(final String project, final String counter, final Integer periodFrom, final Integer periodUntil) throws DataLoadingException {
        try {
            final List<Path> dataFilePaths = this.fileSystemService.getRawDataFiles(project, counter, periodFrom, periodUntil);
            return this.csvService.loadRawCounterData(dataFilePaths);
        }
        catch (final IOException e) {
            throw new DataLoadingException("Failed to load rawCounterData.", e);
        }
    }

    public void addCounterData(final String project, final String counter, final RawCounterDataRow rawCounterDataRow) throws DataPersistanceException {
        try {
            final Integer year = rawCounterDataRow.timestamp().getYear();
            final List<Path> dataFilePaths = this.fileSystemService.getRawDataFiles(project, counter, year, year);

            final RawCounterData newRawCounterData;
            if(dataFilePaths.isEmpty()) {
                newRawCounterData = new RawCounterData();
                newRawCounterData.counterData().add(rawCounterDataRow);
            }
            else {
                final RawCounterData rawCounterData = this.csvService.loadRawCounterData(dataFilePaths);
                rawCounterData.counterData().add(rawCounterDataRow);
                final List<RawCounterDataRow> sortedList = rawCounterData.counterData().stream()
                        .sorted(Comparator.comparing(RawCounterDataRow::timestamp))
                        .toList();
                newRawCounterData = new RawCounterData(sortedList);
            }

            this.csvService.persistRawCounterData(this.fileSystemService.getRawDataFilePathForYear(project, counter, year), newRawCounterData);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to addCounterData.", e);
        }
    }

    public EvaluationData evaluateSimple(final String project, final String counter, final Integer periodFrom, final Integer periodUntil, final EvaluationMode evaluationMode) throws DataLoadingException {
        final RawCounterData rawCounterData = this.getRawCounterData(project, counter, periodFrom, periodUntil);
        return this.evaluationService.evaluateSimple(project, rawCounterData, evaluationMode);
    }
}
