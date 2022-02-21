package net.msk.consumptionCalc.service;

import net.msk.consumptionCalc.model.*;
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

    public void addProject(final String projectName) throws DataPersistanceException {
        try {
            this.fileSystemService.addProject(projectName);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to create project folder.", e);
        }
    }

    public List<Project> getProjectList() throws DataLoadingException {
        try {
            return this.fileSystemService.getProjects();
        }
        catch (final IOException e) {
            throw new DataLoadingException("Failed to load project list.", e);
        }
    }

    public void addCounter(final String projectName, final String counterName, final Unit counterUnit) throws DataPersistanceException, DataLoadingException {
        try {
            final Counter counter = new Counter(counterName, counterUnit);
            this.fileSystemService.addCounter(projectName, counter);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to create counter.", e);
        }
    }

    public List<Counter> getCounterList(final String projectName) throws DataLoadingException {
        try {
            return this.fileSystemService.getCounterList(projectName);
        }
        catch (final IOException e) {
            throw new DataLoadingException("Failed to load counter list.", e);
        }
    }

    public Counter getCounter(final String projectName, final String counterName) throws DataLoadingException {
        try {
            return this.fileSystemService.getCounter(projectName, counterName);
        }
        catch (final IOException | ClassNotFoundException e) {
            throw new DataLoadingException("Failed to load counter.", e);
        }
    }

    public RawCounterData getRawCounterData(final String projectName, final String counterName, final Integer periodFrom, final Integer periodUntil) throws DataLoadingException {
        try {
            final List<Path> dataFilePaths = this.fileSystemService.getRawDataFiles(projectName, counterName, periodFrom, periodUntil);
            return this.csvService.loadRawCounterData(dataFilePaths);
        }
        catch (final IOException e) {
            throw new DataLoadingException("Failed to load rawCounterData.", e);
        }
    }

    public void addCounterData(final String projectName, final String counterName, final RawCounterDataRow rawCounterDataRow) throws DataPersistanceException, DataLoadingException {
        try {
            final Integer year = rawCounterDataRow.timestamp().getYear();
            final List<Path> dataFilePaths = this.fileSystemService.getRawDataFiles(projectName, counterName, year, year);

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

            this.csvService.persistRawCounterData(this.fileSystemService.getRawDataFilePathForYear(projectName, counterName, year), newRawCounterData);
        }
        catch (final IOException e) {
            throw new DataPersistanceException("Failed to addCounterData.", e);
        }
    }

    public EvaluationData getSimpleEvaluationResult(final String projectName, final String counterName, final Integer periodFrom, final Integer periodUntil, final EvaluationMode evaluationMode) throws DataLoadingException {
        final RawCounterData rawCounterData = this.getRawCounterData(projectName, counterName, periodFrom, periodUntil);
        return this.evaluationService.evaluateSimple(projectName, rawCounterData, evaluationMode);
    }
}
