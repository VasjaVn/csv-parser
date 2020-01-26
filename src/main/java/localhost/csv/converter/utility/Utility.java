package localhost.csv.converter.utility;

import localhost.csv.converter.entity.ComponentEntity;

public abstract class Utility {
    public static Long fromStrToLong(final String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static ComponentEntity.ElasticMetaInfo createComponentElasticMetaInfo(final String uid) {
        return ComponentEntity.ElasticMetaInfo.builder().uid(uid).build();
    }

}
