package net.msk.consumptionCalc.persistence.file;

import net.msk.consumptionCalc.model.*;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CsvService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvService.class);

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

    public void persistRawCounterData(final Path file, final RawCounterData rawCounterData) throws IOException {
        final FileWriter out = new FileWriter(file.toFile());

        final List<String> headers = new ArrayList<>();
        headers.add("timestamp");
        headers.add("value");

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(String[]::new))
                .setDelimiter(";")
                .build())) {

            rawCounterData.counterData().forEach(row -> {
                final List<String> rowData = new ArrayList<>();
                rowData.add(row.timestamp().toString());
                rowData.add(Double.toString(row.value()));
                try {
                    printer.printRecord(rowData);
                }
                catch (final IOException e) {
                    LOGGER.error("Failed to write RawCounterDataRow.", e);
                }
            });
        }
    }

    public void persistEvaluationSimpleResult(final Path file, final EvaluationData evaluationData) throws IOException {
        final FileWriter out = new FileWriter(file.toFile());

        final List<String> headers = new ArrayList<>();
        headers.add("from");
        headers.add("until");
        headers.addAll(evaluationData.evaluationColumns().stream().map(EvaluationColumn::header).toList());

        try (CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(String[]::new))
                .setDelimiter(";")
                .build())) {

            evaluationData.evaluationDataRows().forEach(row -> {
                final List<String> rowData = new ArrayList<>();
                rowData.add(row.from().toString());
                rowData.add(row.until().toString());
                rowData.addAll(row.columnData());
                try {
                    printer.printRecord(rowData);
                }
                catch (IOException e) {
                    LOGGER.error("Failed to write EvaluationResultRow.", e);
                }
            });
        }
    }

    public EvaluationData loadEvaluationSimpleData(final String project, final UUID evaluationId, final Path dataFilePath) throws IOException {
        final Reader dataFileReader = new FileReader(dataFilePath.toFile());
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setDelimiter(";")
                .setAllowMissingColumnNames(true)
                .build();

        final List<EvaluationDataRow> dataRows = new ArrayList<>();
        final Iterable<CSVRecord> records = csvFormat.parse(dataFileReader);
        final Iterator<CSVRecord> recordIterator = records.iterator();
        final CSVRecord headerRecord = recordIterator.next();
        final List<EvaluationColumn> headers = headerRecord.stream()
                .filter(s -> !s.equals("from") && !s.equals("until"))
                .map(EvaluationColumn::new)
                .toList();

        recordIterator.forEachRemaining(r -> {
            final List<String> columnData = r.toList();

            final String timestampFromString = columnData.get(0);
            final String timestampUntilString = columnData.get(1);
            final LocalDateTime from = LocalDateTime.parse(timestampFromString);
            final LocalDateTime until = LocalDateTime.parse(timestampUntilString);

            dataRows.add(new EvaluationDataRow(from, until, columnData.subList(2, columnData.size())));
        });

        return new EvaluationData(evaluationId, project, LocalDateTime.now(), headers, dataRows);
    }
}
