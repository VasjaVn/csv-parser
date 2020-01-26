package localhost.csv.converter.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class ComponentGroupEntity {
    private final Long primaryKey;
    private final String name;
    private final PlatformEntity platform;
    private final Map<String, ComponentEntity> mapComponents;

    public Long getPlatformForeignKey() {
        return platform.getPrimaryKey();
    }
}
