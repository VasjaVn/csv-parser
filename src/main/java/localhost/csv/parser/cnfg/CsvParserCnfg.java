package localhost.csv.parser.cnfg;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CsvParserCnfg {
    public static final String COMMA_DELIMITER = ",";

    private final String delimiter;
    private final Boolean ignoreFirstRecord;

    private final String fileNameCompCsv;
    private final String fileNameCompVerCsv;

    public Boolean isIgnoreFirstRecord() {
        return ignoreFirstRecord;
    }
}
