package net.msk.consumptionCalc.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import java.util.UUID;

@Service
public class DataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataService.class);

    private final CsvService csvService;
    private final FileSystemService fileSystemService;
    private final EvaluationService evaluationService;
    private final Cache<UUID, EvaluationData> evaluationDataCache;

    public DataService(final CsvService csvService, final FileSystemService fileSystemService, final EvaluationService evaluationService) {
        this.csvService = csvService;
        this.fileSystemService = fileSystemService;
        this.evaluationService = evaluationService;
        this.evaluationDataCache = Caffeine.newBuilder()
                .maximumSize(100)
                .build();
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

    public UUID evaluateSimple(final String project, final String counter, final String period, final EvaluationMode evaluationMode) throws DataLoadingException {
        final RawCounterData rawCounterData = this.getRawCounterData(project, counter, period);
        final EvaluationData evaluationData = this.evaluationService.evaluateSimple(project, rawCounterData, evaluationMode);
        this.evaluationDataCache.put(evaluationData.evaluationId(), evaluationData);

        return evaluationData.evaluationId();
    }

    public void persistEvaluationData(final UUID evaluationId) throws DataPersistanceException {
        final EvaluationData evaluationData = this.evaluationDataCache.getIfPresent(evaluationId);
        if(evaluationData != null) {
            try {
                final Path evaluationFolder = this.fileSystemService.getEvaluationFolder(evaluationData.project());
                final Path filePath = evaluationFolder.resolve(evaluationData.evaluationId() + ".csv");
                this.csvService.persistEvaluationSimpleResult(filePath, evaluationData);
            }
            catch (final IOException e) {
                throw new DataPersistanceException("Failed to persist evaluationSimpleData.", e);
            }
        }
        else {
            LOGGER.error("Failed finding evaluation data in cache. EvaluationId: {}", evaluationId);
        }
    }

    public EvaluationData getEvaluationData(final String project, final UUID evaluationId) throws DataLoadingException {
        LOGGER.info("Current cache size: " + this.evaluationDataCache.estimatedSize());
        final EvaluationData evaluationData = this.evaluationDataCache.getIfPresent(evaluationId);
        if(evaluationData != null) {
            return evaluationData;
        }
        else {
            try {
                final Path dataFilePath = this.fileSystemService.getEvaluationFolder(project).resolve(evaluationId + ".csv");
                final EvaluationData evaluationDataFromFile = this.csvService.loadEvaluationSimpleData(project, evaluationId, dataFilePath);
                this.evaluationDataCache.put(evaluationId, evaluationDataFromFile);
                return evaluationDataFromFile;
            }
            catch (final IOException e) {
                throw new DataLoadingException("Failed to load evaluationData.", e);
            }
        }
    }
}
