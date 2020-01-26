package localhost.csv.parser.record;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CompVerCsvRecord {
    private String uid;
    private String componentUid;
    private String createdByUserUserId;
    private String modifiedByUserUserId;
    private String updated;
    private String qualityGrade;
    private String componentReleaseVersion;
    private String versionValidated;
    private String versionValidatedError;
}
