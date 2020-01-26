package localhost.csv.converter.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class PlatformEntity {
    private final Long primaryKey;
    private final String name;
    private final Map<String, ComponentGroupEntity> mapComponentGroups;
}
