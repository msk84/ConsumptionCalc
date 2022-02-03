package net.msk.consumptionCalc.file;

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
        return this.csvService.getCounterData(dataFilePath);
    }
}
