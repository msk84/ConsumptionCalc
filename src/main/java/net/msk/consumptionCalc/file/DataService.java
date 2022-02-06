package net.msk.consumptionCalc.file;

import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

@Service
public class DataService {

    private final CsvService csvService;
    private final FileSystemService fileSystemService;

    public DataService(final CsvService csvService, final FileSystemService fileSystemService) {
        this.csvService = csvService;
        this.fileSystemService = fileSystemService;
    }

    public RawCounterData getRawCounterData(final String project, final String counter, final String period) throws IOException {
        final Path dataFilePath = this.fileSystemService.getRawDataFile(project, counter, period);
        return this.csvService.loadRawCounterData(dataFilePath);
    }

    public EvaluationData getEvaluationSimpleData(final String project, final String fileId) throws IOException {
        final Path dataFilePath = this.fileSystemService.getEvaluationFolder(project).resolve(fileId + ".csv");
        return this.csvService.loadEvaluationSimpleData(dataFilePath);
    }

    public void addCounterData(final String project, final String counter, final String period, final RawCounterDataRow rawCounterDataRow) throws IOException {
        final Path dataFilePath = this.fileSystemService.getRawDataFile(project, counter, period);
        final RawCounterData rawCounterData = this.csvService.loadRawCounterData(dataFilePath);
        rawCounterData.counterData().add(rawCounterDataRow);
        final List<RawCounterDataRow> sortedList = rawCounterData.counterData().stream()
                .sorted(Comparator.comparing(RawCounterDataRow::timestamp))
                .toList();

        final RawCounterData newRawCounterData = new RawCounterData(sortedList);
        this.csvService.persistRawCounterData(dataFilePath, newRawCounterData);
    }
}
