package localhost.csv.parser.result;

import localhost.csv.parser.record.CompCsvRecord;
import localhost.csv.parser.record.CompVerCsvRecord;
import lombok.Data;

import java.util.List;

@Data
public class CsvParserResult {
    private final List<CompCsvRecord> compCsvRecords;
    private final List<CompVerCsvRecord> compVerCsvRecords;
}
