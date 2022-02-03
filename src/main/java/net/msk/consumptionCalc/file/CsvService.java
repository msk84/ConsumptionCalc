package net.msk.consumptionCalc.file;

import net.msk.consumptionCalc.model.RawCounterData;
import net.msk.consumptionCalc.model.RawCounterDataRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvService {

    public RawCounterData getCounterData(final Path dataFilePath) throws IOException {
        final Reader dataFileReader = new FileReader(dataFilePath.toFile());
        final CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setDelimiter(";")
                .setAllowMissingColumnNames(true)
                .build();

        final List<RawCounterDataRow> result = new ArrayList<>();
        final Iterable<CSVRecord> records = csvFormat.parse(dataFileReader);
        for(final CSVRecord record : records) {
            final String dateString = record.get("date");
            final String timeString = record.get("time");
            final String countervalueString = record.get("value");

            final LocalDate date = LocalDate.parse(dateString);
            final LocalTime time = LocalTime.parse(timeString);
            final double countervalue = Double.parseDouble(countervalueString);

            result.add(new RawCounterDataRow(date, time, countervalue));
        }

        dataFileReader.close();
        return new RawCounterData(result);
    }
}
