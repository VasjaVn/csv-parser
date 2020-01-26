package localhost.csv.parser.record;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CompCsvRecord {
    private String uid;
    private String platform;
    private String component;
    private String componentGroup;
    private String createdByUserUserId;
    private String modifiedByUserUserId;
    private String updated;
    private String applicationId;
}
