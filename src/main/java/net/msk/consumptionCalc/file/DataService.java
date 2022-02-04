package net.msk.consumptionCalc.file;

import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.RawCounterData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;

@Service
public class DataService {

    private final CsvService csvService;
    private final FileSystemService fileSystemService;

    public DataService(final CsvService csvService, final FileSystemService fileSystemService) {
        this.csvService = csvService;
        this.fileSystemService = fileSystemService;
    }

    public RawCounterData getRawCounterData(final String project, final String counter, final String period) throws IOException {
        final Path dataFilePath = this.fileSystemService.getDataFile(project, counter, period);
        return this.csvService.loadRawCounterData(dataFilePath);
    }

    public EvaluationData getEvaluationSimpleData(final String project, final String fileId) throws IOException {
        final Path dataFilePath = this.fileSystemService.getEvaluationFolder(project).resolve(fileId + ".csv");
        return this.csvService.loadEvaluationSimpleData(dataFilePath);
    }
}
