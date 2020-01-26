package localhost.csv.converter.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Builder
public class ComponentEntity {
    private final Long primaryKey;
    private final String name;
    private final Long assetInsightId;

    private final ComponentGroupEntity componentGroup;

    private Set<ComponentVersionEntity> componentVersions;

    private final ElasticMetaInfo elasticMetaInfo;

    public Long getComponentGroupForeignKey() {
        return componentGroup.getPrimaryKey();
    }

    @Builder
    public static class ElasticMetaInfo {
        private final String uid;
    }
}
