/*
 * Copyright © 2026 FurrifyWS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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