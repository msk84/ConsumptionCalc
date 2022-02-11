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

    public RawCounterData loadRawCounterData(final List<Path> dataFilePaths) throws IOException {
        final List<RawCounterDataRow> result = new ArrayList<>();
        for(final Path dataFilePath : dataFilePaths) {
            final Reader dataFileReader = new FileReader(dataFilePath.toFile());
            final CSVFormat csvFormat = this.getDefaultCsvFormat();

            final Iterable<CSVRecord> records = csvFormat.parse(dataFileReader);
            for (final CSVRecord record : records) {
                final String timestampString = record.get("timestamp");
                final String counterValueString = record.get("value");

                final LocalDateTime datetime = LocalDateTime.parse(timestampString);
                final double counterValue = Double.parseDouble(counterValueString);

                result.add(new RawCounterDataRow(datetime, counterValue));
            }
            dataFileReader.close();
        }

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
}
