package net.msk.consumptionCalc.file;

import net.msk.consumptionCalc.model.EvaluationData;
import net.msk.consumptionCalc.model.EvaluationDataRow;
import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CsvService {

    private CSVFormat getDefaultCsvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setDelimiter(";")
                .setAllowMissingColumnNames(true)
                .build();
    }

    public RawCounterData loadRawCounterData(final Path dataFilePath) throws IOException {
        final Reader dataFileReader = new FileReader(dataFilePath.toFile());
        final CSVFormat csvFormat = this.getDefaultCsvFormat();

        final List<RawCounterDataRow> result = new ArrayList<>();
        final Iterable<CSVRecord> records = csvFormat.parse(dataFileReader);
        for(final CSVRecord record : records) {
            final String timestampString = record.get("timestamp");
            final String counterValueString = record.get("value");

            final LocalDateTime datetime = LocalDateTime.parse(timestampString);
            final double counterValue = Double.parseDouble(counterValueString);

            result.add(new RawCounterDataRow(datetime, counterValue));
        }

        dataFileReader.close();
        return new RawCounterData(result);
    }

    public void persistEvaluationSimpleResult(final Path file, final EvaluationData evaluationData) throws IOException {
        final FileWriter out = new FileWriter(file.toFile());
        final String[] DEFAULT_HEADERS = {"from", "until", "value"};

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                .setHeader(DEFAULT_HEADERS)
                .setDelimiter(";")
                .build())) {
             printer.printRecord("2022-01-01T11:24:12", "2022-02-01T12:21:11","2323.43");
             printer.printRecord("2022-01-01T11:24:12", "2022-02-01T12:21:11","2323.43");
             printer.printRecord("2022-01-01T11:24:12", "2022-02-01T12:21:11","2323.43");
        }
    }

    public EvaluationData loadEvaluationSimpleData(Path dataFilePath) throws IOException {
        final Reader dataFileReader = new FileReader(dataFilePath.toFile());
        final CSVFormat csvFormat = this.getDefaultCsvFormat();

        final List<EvaluationDataRow> result = new ArrayList<>();
        final Iterable<CSVRecord> records = csvFormat.parse(dataFileReader);
        for(final CSVRecord record : records) {
            final String timestampFromString = record.get("from");
            final String timestampUntilString = record.get("until");
            final String counterValueString = record.get("value");

            final LocalDateTime from = LocalDateTime.parse(timestampFromString);
            final LocalDateTime until = LocalDateTime.parse(timestampUntilString);
            //final double counterValue = Double.parseDouble(counterValueString);

            result.add(new EvaluationDataRow(from, until, Collections.singletonList(counterValueString)));
        }

        return new EvaluationData(result);
    }
}
