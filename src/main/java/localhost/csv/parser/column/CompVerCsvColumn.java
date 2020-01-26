package localhost.csv.parser.column;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CompVerCsvColumn {
    UID(0),
    COMPONENT_UID(1),
    CREATED_BY_USER_USERID(2),
    MODIFIED_BY_USER_USERID(3),
    UPDATED(4),
    QUALITY_GRADE(5),
    COMPONENT_RELEASE_VERSION(6),
    VERSION_VALIDATED(7),
    VERSION_VALIDATION_ERROR(8);

    private final Integer order;
}
