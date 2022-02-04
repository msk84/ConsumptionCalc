package net.msk.consumptionCalc.file;

import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.EvaluationDataRow;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EvaluationService {

    private final FileSystemService fileSystemService;
    private final CsvService csvService;

    public EvaluationService(final FileSystemService fileSystemService, final CsvService csvService) {
        this.fileSystemService = fileSystemService;
        this.csvService = csvService;
    }

    public String evaluateSimple(final String project, final RawCounterData rawCounterData) throws IOException {
        final Path evaluationFolder = this.fileSystemService.getEvaluationFolder(project);
        final String fileUuid = UUID.randomUUID().toString();
        final Path filePath = evaluationFolder.resolve(fileUuid + ".csv");

        final List<String> data = new ArrayList<>();
        data.add("aaaaaJa");
        data.add("bja");
        final List<EvaluationDataRow> list = new ArrayList<>();

        for(int i=0; i < rawCounterData.counterData().size() - 1; i++) {
            final RawCounterDataRow last = rawCounterData.counterData().get(i);
            final RawCounterDataRow current = rawCounterData.counterData().get(i+1);

            new EvaluationDataRow(last.timestamp(), current.timestamp(), data);
        }

        final EvaluationData edata = new EvaluationData(list);

        this.csvService.persistEvaluationSimpleResult(filePath, edata);
        return fileUuid;
    }

}
