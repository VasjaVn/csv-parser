package localhost.csv.converter.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ComponentVersionEntity {
    private final Long primaryKey;
    private final String version;
    private final String packageUrl;
    private final String format;
    private final String qualityGrade;
    private final Boolean validated;
    private final String versionAvoid;
    private final String validationError;

    private ComponentEntity component;

    public Long getComponentForeignKey() {
        return component.getPrimaryKey();
    }

    @Builder
    public static class ElasticMetaInfo {
        private String uid;
        private String componentUid;
    }
}
