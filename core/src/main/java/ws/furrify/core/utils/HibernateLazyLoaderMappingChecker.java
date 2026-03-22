package ws.furrify.core.utils;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;
import org.mapstruct.Condition;

public class HibernateLazyLoaderMappingChecker {

    @Condition
    public static boolean isInitialized(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof PersistentCollection<?> pc) {
            return pc.wasInitialized();
        }

        return Hibernate.isInitialized(value);
    }
}