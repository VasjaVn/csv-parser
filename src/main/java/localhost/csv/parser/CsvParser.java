package localhost.csv.parser;

import com.google.common.primitives.Ints;
import localhost.csv.parser.cnfg.CsvParserCnfg;
import localhost.csv.parser.column.CompCsvColumn;
import localhost.csv.parser.column.CompVerCsvColumn;
import localhost.csv.parser.record.CompCsvRecord;
import localhost.csv.parser.record.CompVerCsvRecord;
import localhost.csv.parser.result.CsvParserResult;
import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CsvParser {
    private static final Integer COUNT_THREADS = 2;
    private final ExecutorService executorService;

    private final CsvParserCnfg csvParserCnfg;
    private final File fileCompCsv;
    private final File fileCompVerCsv;

    public CsvParser(final CsvParserCnfg csvParserCnfg) {
        this.csvParserCnfg = csvParserCnfg;
        this.fileCompCsv = new File(csvParserCnfg.getFileNameCompCsv());
        this.fileCompVerCsv = new File(csvParserCnfg.getFileNameCompVerCsv());
        this.executorService = Executors.newFixedThreadPool(COUNT_THREADS);
    }

    public Optional<CsvParserResult> parse() {
        try (BufferedReader fileCompCsvReader = new BufferedReader(new FileReader(fileCompCsv));
             BufferedReader fileCompVerCsvReader = new BufferedReader(new FileReader(fileCompVerCsv))) {

            try {
                Future<List<CompCsvRecord>> compCsvRecordsFuture = executorService.submit(
                        new CompCsvParserThread(csvParserCnfg, fileCompCsvReader, Ints.checkedCast(Files.lines(fileCompCsv.toPath()).count())));
                Future<List<CompVerCsvRecord>> compVerCsvRecordsFuture = executorService.submit(
                        new CompVerCsvParserThread(csvParserCnfg, fileCompVerCsvReader, Ints.checkedCast(Files.lines(fileCompVerCsv.toPath()).count())));

                List<CompCsvRecord> compCsvRecords = compCsvRecordsFuture.get();
                List<CompVerCsvRecord> compVerCsvRecords = compVerCsvRecordsFuture.get();

                return Optional.of(new CsvParserResult(compCsvRecords, compVerCsvRecords));
            } catch (IllegalArgumentException ex) {
                System.out.println(ex);
            } catch (InterruptedException | ExecutionException ex) {
                System.out.println(ex.getMessage());
            } finally {
                executorService.shutdown();
            }
        } catch (FileNotFoundException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return Optional.ofNullable(null);
    }

    @AllArgsConstructor
    private static class CompCsvParserThread implements Callable<List<CompCsvRecord>> {
        private final CsvParserCnfg csvParserCnfg;
        private final BufferedReader fileCompCsvReader;
        private Integer countCompCsvRecords;

        @Override
        public List<CompCsvRecord> call() throws Exception {
            if (csvParserCnfg.isIgnoreFirstRecord()) {
                fileCompCsvReader.readLine();
                countCompCsvRecords--;
            }

            System.out.println(String.format("countCompCsvRecords=%s", countCompCsvRecords));
            List<CompCsvRecord> compCsvRecords = new ArrayList<>(countCompCsvRecords);

            String line = "";
            while ((line = fileCompCsvReader.readLine()) != null) {
                String[] csvRecordComp = line.split(csvParserCnfg.getDelimiter());

                String uid = csvRecordComp[CompCsvColumn.UID.getOrder()].trim();
                String platformName = csvRecordComp[CompCsvColumn.PLATFORM.getOrder()].trim();
                String componentName = csvRecordComp[CompCsvColumn.COMPONENT.getOrder()].trim();
                String componentGroupName = csvRecordComp[CompCsvColumn.COMPONENT_GROUP.getOrder()].trim();
                String createdBy = csvRecordComp[CompCsvColumn.CREATED_BY_USER_USERID.getOrder()].trim();
                String modifiedBy = csvRecordComp[CompCsvColumn.MODIFIED_BY_USER_USERID.getOrder()].trim();
                String updated = csvRecordComp[CompCsvColumn.UPDATED.getOrder()].trim();
                String assetInsightId = csvRecordComp[CompCsvColumn.APPLICATION_ID.getOrder()].trim();

                compCsvRecords.add(CompCsvRecord.builder()
                        .uid(uid)
                        .platform(platformName)
                        .component(componentName)
                        .componentGroup(componentGroupName)
                        .createdByUserUserId(createdBy)
                        .modifiedByUserUserId(modifiedBy)
                        .updated(updated)
                        .applicationId(assetInsightId)
                        .build());
            }

            return compCsvRecords;
        }
    }

    @AllArgsConstructor
    private static class CompVerCsvParserThread implements Callable<List<CompVerCsvRecord>> {
        private final CsvParserCnfg csvParserCnfg;
        private final BufferedReader fileCompVerCsvReader;
        private Integer countCompVerCsvRecords;

        @Override
        public List<CompVerCsvRecord> call() throws Exception {
            if (csvParserCnfg.isIgnoreFirstRecord()) {
                fileCompVerCsvReader.readLine();
                countCompVerCsvRecords--;
            }

            System.out.println(String.format("countCompVerCsvRecords=%s", countCompVerCsvRecords));
            List<CompVerCsvRecord> csvCompVerRecords = new ArrayList<>(countCompVerCsvRecords);

            String line = "";
            while ((line = fileCompVerCsvReader.readLine()) != null) {
                String[] csvRecord = line.split(csvParserCnfg.getDelimiter());

                String uid = csvRecord[CompVerCsvColumn.UID.getOrder()].trim();
                String componentUid = csvRecord[CompVerCsvColumn.COMPONENT_UID.getOrder()].trim();
                String createdBy = csvRecord[CompVerCsvColumn.CREATED_BY_USER_USERID.getOrder()].trim();
                String modifiedBy = csvRecord[CompVerCsvColumn.MODIFIED_BY_USER_USERID.getOrder()].trim();
                String updated = csvRecord[CompVerCsvColumn.UPDATED.getOrder()].trim();
                String qualityGrade = csvRecord[CompVerCsvColumn.QUALITY_GRADE.getOrder()].trim();
                String componentReleaseVersion = csvRecord[CompVerCsvColumn.COMPONENT_RELEASE_VERSION.getOrder()].trim();
                String versionValidated = csvRecord[CompVerCsvColumn.VERSION_VALIDATED.getOrder()].trim();
                String versionValidationError = csvRecord[CompVerCsvColumn.VERSION_VALIDATION_ERROR.getOrder()].trim().replace('\'', '\"');

                csvCompVerRecords.add(CompVerCsvRecord.builder()
                        .uid(uid)
                        .componentUid(componentUid)
                        .createdByUserUserId(createdBy)
                        .modifiedByUserUserId(modifiedBy)
                        .updated(updated)
                        .qualityGrade(qualityGrade)
                        .componentReleaseVersion(componentReleaseVersion)
                        .versionValidated(versionValidated)
                        .versionValidatedError(versionValidationError)
                        .build());
            }

            return csvCompVerRecords;
        }
    }
}
