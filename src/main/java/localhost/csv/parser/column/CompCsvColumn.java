package localhost.csv.parser.column;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompCsvColumn {
    UID(0),
    PLATFORM(1),
    COMPONENT(2),
    COMPONENT_GROUP(3),
    CREATED_BY_USER_USERID(4),
    MODIFIED_BY_USER_USERID(5),
    UPDATED(6),
    APPLICATION_ID(7);

    private final Integer order;
}
