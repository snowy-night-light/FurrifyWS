package ws.furrify.core.mappers;

import org.hibernate.Hibernate;
import org.hibernate.collection.spi.PersistentCollection;
import org.mapstruct.Condition;
import ws.furrify.core.entity.BaseEntity;

import java.util.Collection;

public class HibernateLazyLoaderMappingChecker {

    @Condition
    public static boolean isCollectionInitialized(Collection<?> collection) {
        if (collection == null) {
            return false;
        }
        if (collection instanceof PersistentCollection<?> pc) {
            return pc.wasInitialized();
        }
        return Hibernate.isInitialized(collection);
    }

    @Condition
    public static <ENTITY extends BaseEntity> boolean isEntityInitialized(ENTITY entity) {
        if (entity == null) {
            return false;
        }
        return Hibernate.isInitialized(entity);
    }
}