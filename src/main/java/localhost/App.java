package localhost;

import localhost.csv.converter.CsvToSqlConverter;
import localhost.csv.parser.CsvParser;
import localhost.csv.parser.cnfg.CsvParserCnfg;

public class App {
    private static final String FILE_NAME_COMP_CSV = "./csv/components.csv";
    private static final String FILE_NAME_COMP_VER_CSV = "./csv/componentVersion.csv";

    public static void main( String[] args ) {
        CsvParserCnfg csvParserCnfg = CsvParserCnfg.builder()
                .delimiter(CsvParserCnfg.COMMA_DELIMITER)
                .ignoreFirstRecord(true)
                .fileNameCompCsv(FILE_NAME_COMP_CSV)
                .fileNameCompVerCsv(FILE_NAME_COMP_VER_CSV)
                .build();

        CsvParser csvParser = new CsvParser(csvParserCnfg);
        CsvToSqlConverter converter = new CsvToSqlConverter(csvParser);
        converter.convert();
    }
}
