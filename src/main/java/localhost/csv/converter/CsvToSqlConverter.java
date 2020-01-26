package localhost.csv.converter;

import localhost.csv.converter.entity.ComponentEntity;
import localhost.csv.converter.entity.ComponentGroupEntity;
import localhost.csv.converter.entity.ComponentVersionEntity;
import localhost.csv.converter.entity.PlatformEntity;
import localhost.csv.converter.utility.Utility;
import localhost.csv.parser.CsvParser;
import localhost.csv.parser.record.CompCsvRecord;
import localhost.csv.parser.record.CompVerCsvRecord;
import localhost.csv.parser.result.CsvParserResult;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CsvToSqlConverter {
    private static final String FILE_NAME_PLATFORM_SQL = "./result/sql/1_platform.sql";
    private static final String FILE_NAME_COMPONENT_GROUP_SQL = "./result/sql/2_componentGroup.sql";
    private static final String FILE_NAME_COMPONENT_SQL = "./result/sql/3_component.sql";
    private static final String FILE_NAME_COMPONENT_VERSION_SQL = "./result/sql/4_component_version.sql";

    private static final String FILE_NAME_NEW_COMPONENT_VERSION_CSV = "./result/newComponentVersion.csv";

    private final CsvParser csvParser;
    private final Map<String, PlatformEntity> mapRootEntity;

    public CsvToSqlConverter(final CsvParser csvParser) {
        this.csvParser = csvParser;
        this.mapRootEntity = new LinkedHashMap<>();
    }

    public void convert() {
        CsvParserResult csvParserResult = csvParser.parse().orElseThrow(() -> new RuntimeException("can not parse csv file"));
        newCsvCompVerFile(csvParserResult.getCompVerCsvRecords());
        prepareMapRootEntityFromCsvParserResult(csvParserResult);
        generateSqlFromMapRootEntityAndWriteToFile();
    }

    private void generateSqlFromMapRootEntityAndWriteToFile() {
        StringBuilder sqlInsertPlatform = new StringBuilder();
        StringBuilder sqlInsertComponentGroup = new StringBuilder();
        StringBuilder sqlInsertComponent = new StringBuilder();
        StringBuilder sqlInsertComponentVersion = new StringBuilder();

        for (PlatformEntity platformEntity: mapRootEntity.values()) {
            //System.out.println(String.format("INSERT INTO platform(name) values('%s');", platformEntity.getName()));
            sqlInsertPlatform.append(String.format("INSERT INTO platform(name) values('%s');\n", platformEntity.getName()));

            for (ComponentGroupEntity componentGroupEntity: platformEntity.getMapComponentGroups().values()) {
                //System.out.println(String.format("INSERT INTO component_group(name, platform_fk) values('%s', %s);", componentGroupEntity.getName(), componentGroupEntity.getPlatformForeignKey()));
                sqlInsertComponentGroup.append(String.format("INSERT INTO component_group (platform_id, name) values(%s, '%s');\n", componentGroupEntity.getPlatformForeignKey(), componentGroupEntity.getName()));

                for (ComponentEntity componentEntity: componentGroupEntity.getMapComponents().values()) {
                    //System.out.println(String.format("INSERT INTO component(name, asset_insight_id, component_group_fk) values('%s', %s, %s);", componentEntity.getName(), componentEntity.getAssetInsightId(), componentEntity.getComponentGroupForeignKey()));
                    sqlInsertComponent.append(String.format("INSERT INTO component (component_group_id, name, asset_insight_id) values(%s, '%s', %s);\n", componentEntity.getComponentGroupForeignKey(), componentEntity.getName(), componentEntity.getAssetInsightId()));

                    for (ComponentVersionEntity componentVersionEntity: componentEntity.getComponentVersions()) {
                        //System.out.println(String.format("INSERT INTO component_version(version, quality_grade, validation_error, component_fk) values('%s', %s, '%s', %s);", componentVersionEntity.getVersion(), componentVersionEntity.getQualityGrade(), componentVersionEntity.getValidationError(), componentVersionEntity.getComponentForeignKey()));
                        sqlInsertComponentVersion.append(String.format("INSERT INTO component_version (component_id, version, quality_grade, validation_error) values(%s, '%s', '%s', %s);\n", componentVersionEntity.getComponentForeignKey(), componentVersionEntity.getVersion(), componentVersionEntity.getQualityGrade(), componentVersionEntity.getValidationError()));
                    }
                }
            }
        }

        try (PrintWriter sqlPlatform = new PrintWriter(FILE_NAME_PLATFORM_SQL, "UTF-8");
             PrintWriter sqlComponentGroup = new PrintWriter(FILE_NAME_COMPONENT_GROUP_SQL, "UTF-8");
             PrintWriter sqlComponent = new PrintWriter(FILE_NAME_COMPONENT_SQL, "UTF-8");
             PrintWriter sqlComponentVersion = new PrintWriter(FILE_NAME_COMPONENT_VERSION_SQL, "UTF-8")) {
            
            sqlPlatform.write(sqlInsertPlatform.toString());
            sqlComponentGroup.write(sqlInsertComponentGroup.toString());
            sqlComponent.write(sqlInsertComponent.toString());
            sqlComponentVersion.write(sqlInsertComponentVersion.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void newCsvCompVerFile(List<CompVerCsvRecord> compVerCsvRecords) {
        try (PrintWriter csvNewFile = new PrintWriter(FILE_NAME_NEW_COMPONENT_VERSION_CSV, "UTF-8")) {
            csvNewFile.write("uid,componentUid,createdBy,modifiedBy,updated,qualitygrade,componentreleaseversion,versionvalidated,versionvalidationerror\n");
            for (CompVerCsvRecord compVerCsvRecord: compVerCsvRecords) {
                String record = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        compVerCsvRecord.getUid(),
                        compVerCsvRecord.getComponentUid(),
                        compVerCsvRecord.getCreatedByUserUserId(),
                        compVerCsvRecord.getModifiedByUserUserId(),
                        compVerCsvRecord.getUpdated(),
                        compVerCsvRecord.getQualityGrade(),
                        compVerCsvRecord.getComponentReleaseVersion(),
                        compVerCsvRecord.getVersionValidated(),
                        compVerCsvRecord.getVersionValidatedError());
                csvNewFile.write(record);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void prepareMapRootEntityFromCsvParserResult(final CsvParserResult csvParserResult) {
        final List<CompCsvRecord> compCsvRecords = csvParserResult.getCompCsvRecords();
        final List<CompVerCsvRecord> compVerCsvRecords = csvParserResult.getCompVerCsvRecords();

        final Map<String, Set<ComponentVersionEntity>> mapCompUidToCompVersions = prepareMapCompUidToCompVersions(compVerCsvRecords);

        long platformPrimaryKey = 0;
        long componentGroupPrimaryKey = 0;
        long componentPrimaryKey = 0;

        for (CompCsvRecord compCsvRecord: compCsvRecords) {
            //System.out.println(String.format("%s\t - [platform=\"%s\",\t componentGroup=\"%s\",\t component=\"%s\", assetInsightId=\"%s\"]", ++lineNumber, compCsvRecord.getPlatform(), compCsvRecord.getComponentGroup(), compCsvRecord.getComponent(), compCsvRecord.getApplicationId()));

            PlatformEntity platformEntity =
                    mapRootEntity.get(compCsvRecord.getPlatform());
            if (platformEntity == null) {
                PlatformEntity newPlatformEntity = PlatformEntity.builder()
                        .primaryKey(++platformPrimaryKey)
                        .name(compCsvRecord.getPlatform())
                        .mapComponentGroups(new LinkedHashMap<>())
                        .build();
                mapRootEntity.put(compCsvRecord.getPlatform(), newPlatformEntity);
            } else {
                ComponentGroupEntity componentGroupEntity =
                        platformEntity.getMapComponentGroups().get(compCsvRecord.getComponentGroup());
                if (componentGroupEntity == null) {
                    ComponentGroupEntity newComponentGroupEntity = ComponentGroupEntity.builder()
                            .primaryKey(++componentGroupPrimaryKey)
                            .name(compCsvRecord.getComponentGroup())
                            .platform(platformEntity)
                            .mapComponents(new LinkedHashMap<>())
                            .build();
                    platformEntity.getMapComponentGroups().put(compCsvRecord.getComponentGroup(), newComponentGroupEntity);
                } else {
                    ComponentEntity componentEntity =
                            componentGroupEntity.getMapComponents().get(compCsvRecord.getComponent());
                    if (componentEntity == null) {
                        ComponentEntity newComponentEntity = ComponentEntity.builder()
                                .primaryKey(++componentPrimaryKey)
                                .name(compCsvRecord.getComponent())
                                .componentGroup(componentGroupEntity)
                                .componentVersions(new HashSet<>())
                                .assetInsightId(Utility.fromStrToLong(compCsvRecord.getApplicationId()))
                                .elasticMetaInfo(Utility.createComponentElasticMetaInfo(compCsvRecord.getUid()))
                                .build();

                        Set<ComponentVersionEntity> componentVersionEntities = mapCompUidToCompVersions.get(compCsvRecord.getUid());
                        if (componentVersionEntities != null) {
                            componentVersionEntities.stream().forEach(cve -> cve.setComponent(newComponentEntity));
                            newComponentEntity.setComponentVersions(componentVersionEntities);
                        } else {
                            System.out.println(String.format("WARN: LIST COMPONENT VERSIONS IS EMPTY - [platform=\"%s\", componentGroup=\"%s\", component=\"%s\"]", compCsvRecord.getPlatform(), compCsvRecord.getComponentGroup(), compCsvRecord.getComponent()));
                        }
                        componentGroupEntity.getMapComponents().put(compCsvRecord.getComponent(), newComponentEntity);
                    } else {
                        //System.out.println(String.format("WARN - [platform=\"%s\", componentGroup=\"%s\", component=\"%s\"]", compCsvRecord.getPlatform(), compCsvRecord.getComponentGroup(), compCsvRecord.getComponent()));
                    }
                }
            }
        }
    }

    private Map<String, Set<ComponentVersionEntity>> prepareMapCompUidToCompVersions(List<CompVerCsvRecord> compVerCsvRecords) {
        Map<String, Set<ComponentVersionEntity>> map = new LinkedHashMap<>();

        long componentVersionPrimaryKey = 0;
        for (CompVerCsvRecord compVerCsvRecord: compVerCsvRecords) {
            if (compVerCsvRecord.getComponentUid() == null || compVerCsvRecord.getComponentUid().isEmpty()) {
                continue;
            }

            Set<ComponentVersionEntity> componentVersionEntities = map.get(compVerCsvRecord.getComponentUid());

            ComponentVersionEntity componentVersionEntity = ComponentVersionEntity.builder()
                    .primaryKey(++componentVersionPrimaryKey)
                    .version(compVerCsvRecord.getComponentReleaseVersion())
                    .qualityGrade(compVerCsvRecord.getQualityGrade())
                    .validationError(compVerCsvRecord.getVersionValidatedError())
                    .build();

            if (componentVersionEntities == null) {
                Set<ComponentVersionEntity> newComponentVersionEntities = new LinkedHashSet<>();
                newComponentVersionEntities.add(componentVersionEntity);
                map.put(compVerCsvRecord.getComponentUid(), newComponentVersionEntities);
            } else {
                componentVersionEntities.add(componentVersionEntity);
            }
        }

        return map;
    }
}
