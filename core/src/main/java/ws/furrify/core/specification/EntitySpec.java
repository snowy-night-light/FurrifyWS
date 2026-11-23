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
package ws.furrify.core.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.core.PropertyPath;
import org.springframework.data.core.PropertyReferenceException;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.exception.BadRequestException;
import ws.furrify.core.exception.Errors;
import ws.furrify.core.model.StrategyIntf;
import ws.furrify.core.service.StrategyRegistryService;

import java.time.LocalDate;
import java.time.ZonedDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySpec {

    public static final String OR_EXPRESSION_STRING = " || ";
    public static final String AND_EXPRESSION_STRING = " && ";
    public static final String JOIN_OPEN_STRING = "(";
    public static final String JOIN_CLOSE_STRING = ")";
    public static final String EQUAL_OPERATOR = " = ";
    public static final String NOT_EQUAL_OPERATOR = " != ";
    public static final String GREATER_THAN_OPERATOR = " > ";
    public static final String GREATER_THAN_OR_EQUAL_OPERATOR = " >= ";
    public static final String LESS_THAN_OPERATOR = " < ";
    public static final String LESS_THAN_OR_EQUAL_OPERATOR = " <= ";
    public static final String EQUAL_IGNORE_CASE_OPERATOR = " =^ ";
    public static final String NOT_EQUAL_IGNORE_CASE_OPERATOR = " !=^ ";
    public static final String LIKE_OPERATOR = " like ";
    public static final String NOT_LIKE_OPERATOR = " !like ";
    public static final String LIKE_IGNORE_CASE_OPERATOR = " like^ ";
    public static final String NOT_LIKE_IGNORE_CASE_OPERATOR = " !like^ ";
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final Pattern SPEC_PATTERN = Pattern.compile("([\\w.]+)\\s+([!=><^~a-zA-Z]+)\\s+('(?:[^'\\\\\\\\]|\\\\\\\\.)*'|\\\"(?:[^\\\"\\\\\\\\]|\\\\\\\\.)*\\\"|[^)&|]+)");

    private static String formatValueForSpecString(Object value) {
        if (value == null) return "null";
        if (value instanceof String strValue) {
            return "'" + strValue.replace("'", "\\\\'") + "'";
        }
        return String.valueOf(value);
    }
    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> unrestricted() {
        return new EntitySpecResult<>("", (root, query, cb) -> cb.conjunction());
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specEquals(Object value) {
        return new InternalExpression<>(EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            if (v == null) return cb.isNull(path);
            return cb.equal(path, coerceValue(path, v));
        }, value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEquals(Object value) {
        return new InternalExpression<>(NOT_EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            if (v == null) return cb.isNotNull(path);
            return cb.notEqual(path, coerceValue(path, v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThan(Object value) {
        return new InternalExpression<>(GREATER_THAN_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.greaterThan((Expression<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThanOrEqual(Object value) {
        return new InternalExpression<>(GREATER_THAN_OR_EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.greaterThanOrEqualTo((Expression<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLessThan(Object value) {
        return new InternalExpression<>(LESS_THAN_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.lessThan((Expression<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLessThanOrEqual(Object value) {
        return new InternalExpression<>(LESS_THAN_OR_EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.lessThanOrEqualTo((Expression<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    /**
     * Expression for a (dotted) field. Besides entity paths ({@code book.id}, {@code tags.name}),
     * keys inside a JSON map attribute: {@code sources.data.url} reads key {@code url} of the
     * {@code data} JSON column as text (nested keys: {@code data.a.b}). Uses Hibernate's portable
     * {@code json_value}, which each dialect renders for its database; any operator applies to the text value.
     */
    /** SQL/JSON path of these keys: {@code $.a.b}; keys other than plain names quoted ({@code $."my key"}). */
    static String jsonPath(String[] keys) {
        StringBuilder path = new StringBuilder("$");
        for (String key : keys) {
            if (key.matches("[A-Za-z_][A-Za-z0-9_]*")) {
                path.append('.').append(key);
            } else {
                path.append(".\"").append(key.replace("\\", "\\\\").replace("\"", "\\\"")).append('"');
            }
        }
        return path.toString();
    }

    private static Expression<?> getPath(Root<?> root, CriteriaBuilder cb, String field) {
        try {
            return resolvePath(root, PropertyPath.from(field, root.getJavaType()));
        } catch (PropertyReferenceException e) {
            // Longest prefix that resolves to a Map (JSON) attribute; the remaining segments are JSON keys.
            String[] segments = field.split("\\.");
            for (int i = segments.length - 1; i >= 1; i--) {
                Path<?> mapPath;
                try {
                    mapPath = resolvePath(root, PropertyPath.from(String.join(".", Arrays.copyOfRange(segments, 0, i)), root.getJavaType()));
                } catch (PropertyReferenceException notThisPrefix) {
                    continue;
                }
                if (!Map.class.isAssignableFrom(mapPath.getJavaType())) {
                    break;
                }
                if (!(cb instanceof HibernateCriteriaBuilder hcb)) {
                    throw new IllegalStateException("JSON fields in specs need Hibernate's CriteriaBuilder");
                }
                return hcb.jsonValue(mapPath, jsonPath(Arrays.copyOfRange(segments, i, segments.length)));
            }
            throw e;
        }
    }

    private static Path<?> resolvePath(Root<?> root, PropertyPath propertyPath) {
        Path<?> path = root;
        while (propertyPath != null) {
            if (propertyPath.isCollection()) {
                path = getOrCreateJoin((From<?, ?>) path, propertyPath.getSegment(), JoinType.LEFT);
            } else {
                path = path.get(propertyPath.getSegment());
            }
            propertyPath = propertyPath.next();
        }
        return path;
    }

    private static Join<?, ?> getOrCreateJoin(From<?, ?> from, String attributeName, JoinType joinType) {
        for (Join<?, ?> join : from.getJoins()) {
            if (join.getAttribute().getName().equals(attributeName) && join.getJoinType() == joinType) {
                return join;
            }
        }
        return from.join(attributeName, joinType);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object coerceValue(Expression<?> path, Object value) {
        if (value instanceof String strValue) {
            Class<?> type = path.getJavaType();
            try {
                if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, strValue);
                // strategy columns (e.g. Source.strategy) are compared by strategy name
                if (StrategyIntf.class.isAssignableFrom(type)) return StrategyRegistryService.getInstance().deserializeStrategy(strValue);
                if (type == ZonedDateTime.class) return ZonedDateTime.parse(strValue);
                if (type == LocalDate.class) return LocalDate.parse(strValue);
                if (type == Long.class || type == long.class) return Long.valueOf(strValue);
                if (type == Integer.class || type == int.class) return Integer.valueOf(strValue);
                if (type == Boolean.class || type == boolean.class) return Boolean.valueOf(strValue);
            } catch (Exception e) {
                throw new BadRequestException("Cannot convert value '" + strValue + "' to expected type " + type.getSimpleName());
            }
        }
        return value;
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specEqualsIgnoreCase(Object value) {
        return new InternalExpression<>(EQUAL_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof String strValue) {
                return cb.equal(cb.lower((Expression<String>) path), strValue.toLowerCase());
            }
            return cb.equal(path, coerced);
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEqualsIgnoreCase(Object value) {
        return new InternalExpression<>(NOT_EQUAL_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof String strValue) {
                return cb.notEqual(cb.lower((Expression<String>) path), strValue.toLowerCase());
            }
            return cb.notEqual(path, coerced);
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLike(Object value) {
        return new InternalExpression<>(LIKE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) ->
                cb.like((Expression<String>) getPath(root, cb, f), String.valueOf(v)), value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotLike(Object value) {
        return new InternalExpression<>(NOT_LIKE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) ->
                cb.notLike((Expression<String>) getPath(root, cb, f), String.valueOf(v)), value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLikeIgnoreCase(Object value) {
        return new InternalExpression<>(LIKE_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            if (v instanceof String strValue) {
                return cb.like(cb.lower((Expression<String>) path), strValue.toLowerCase());
            }
            return cb.like((Expression<String>) path, String.valueOf(v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotLikeIgnoreCase(Object value) {
        return new InternalExpression<>(NOT_LIKE_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Expression<?> path = getPath(root, cb, f);
            if (v instanceof String strValue) {
                return cb.notLike(cb.lower((Expression<String>) path), strValue.toLowerCase());
            }
            return cb.notLike((Expression<String>) path, String.valueOf(v));
        }, value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specWhere(String field, EntitySpecExpression<ENTITY> expr) {
        String newExpr = JOIN_OPEN_STRING + field + expr.expression() + JOIN_CLOSE_STRING;
        return new InternalExpression<>(newExpr, null, null, expr.toSpecification(field));
    }

    public static <ENTITY extends BaseEntity> EntitySpecWhereStep<ENTITY> specBuilder() {
        return new EntitySpecBuilder<>();
    }

    public static <ENTITY extends BaseEntity> EntitySpecJoinStep<ENTITY> from(EntitySpecResult<ENTITY> result) {
        EntitySpecBuilder<ENTITY> builder = new EntitySpecBuilder<>();
        builder.initializeFrom(result);
        return builder;
    }

    private static class SpecParser<ENTITY extends BaseEntity> {
        private final String input;
        private int pos = 0;

        public SpecParser(String input) {
            this.input = input;
        }

        public EntitySpecResult<ENTITY> parse() {
            return parseOr();
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }

        private boolean match(String str) {
            skipWhitespace();
            if (input.startsWith(str, pos)) {
                pos += str.length();
                return true;
            }
            return false;
        }

        private EntitySpecResult<ENTITY> parseOr() {
            EntitySpecResult<ENTITY> left = parseAnd();
            if (left == null) return null;

            while (match("||")) {
                EntitySpecResult<ENTITY> right = parseAnd();
                if (right == null) throw new BadRequestException(Errors.INVALID_SPECIFICATION_FORMAT.getErrorMessage(input));
                left = EntitySpec.from(left).or(right).build();
            }
            return left;
        }

        private EntitySpecResult<ENTITY> parseAnd() {
            EntitySpecResult<ENTITY> left = parsePrimary();
            if (left == null) return null;

            while (match("&&")) {
                EntitySpecResult<ENTITY> right = parsePrimary();
                if (right == null) throw new BadRequestException(Errors.INVALID_SPECIFICATION_FORMAT.getErrorMessage(input));
                left = EntitySpec.from(left).and(right).build();
            }
            return left;
        }

        private EntitySpecResult<ENTITY> parsePrimary() {
            skipWhitespace();
            if (match("(")) {
                EntitySpecResult<ENTITY> expr = parseOr();
                if (!match(")")) {
                    throw new BadRequestException(Errors.INVALID_SPECIFICATION_FORMAT.getErrorMessage(input));
                }
                return expr;
            }

            Matcher m = SPEC_PATTERN.matcher(input);
            m.region(pos, input.length());
            if (m.lookingAt()) {
                String field = m.group(1);
                String operator = m.group(2);
                String rawValue = m.group(3).trim();
                pos = m.end();

                Object parsedValue;
                if (rawValue.startsWith("'") && rawValue.endsWith("'")) {
                    rawValue = rawValue.substring(1, rawValue.length() - 1).replace("\\\\'", "'");
                    parsedValue = rawValue;
                } else if (rawValue.startsWith("\\\"") && rawValue.endsWith("\\\"")) {
                    rawValue = rawValue.substring(1, rawValue.length() - 1).replace("\\\\\\\"", "\\\"");
                    parsedValue = rawValue;
                } else if (rawValue.equalsIgnoreCase("null")) {
                    parsedValue = null;
                } else if (rawValue.matches(UUID_REGEX)) {
                    parsedValue = UUID.fromString(rawValue);
                } else {
                    parsedValue = rawValue;
                }

                EntitySpecExpression<ENTITY> expr = switch (operator.trim()) {
                    case "=" -> EntitySpec.specEquals(parsedValue);
                    case "!=" -> EntitySpec.specNotEquals(parsedValue);
                    case ">" -> EntitySpec.specGreaterThan(parsedValue);
                    case ">=" -> EntitySpec.specGreaterThanOrEqual(parsedValue);
                    case "<" -> EntitySpec.specLessThan(parsedValue);
                    case "<=" -> EntitySpec.specLessThanOrEqual(parsedValue);
                    case "=^" -> EntitySpec.specEqualsIgnoreCase(parsedValue);
                    case "!=^" -> EntitySpec.specNotEqualsIgnoreCase(parsedValue);
                    case "like" -> EntitySpec.specLike(parsedValue);
                    case "!like" -> EntitySpec.specNotLike(parsedValue);
                    case "like^" -> EntitySpec.specLikeIgnoreCase(parsedValue);
                    case "!like^" -> EntitySpec.specNotLikeIgnoreCase(parsedValue);
                    default -> throw new BadRequestException(Errors.UNKNOWN_SPECIFICATION_OPERATOR.getErrorMessage(operator));
                };

                return EntitySpec.<ENTITY>specBuilder().where(field, expr).build();
            }
            return null;
        }
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> fromSpecString(String specString) {
        if (specString == null || specString.trim().isEmpty()) {
            return EntitySpec.unrestricted();
        }

        SpecParser<ENTITY> parser = new SpecParser<>(specString);
        EntitySpecResult<ENTITY> result = parser.parse();

        if (result == null) {
            throw new BadRequestException(Errors.INVALID_SPECIFICATION_FORMAT.getErrorMessage(specString));
        }
        
        parser.skipWhitespace();
        if (parser.pos < specString.length()) {
            throw new BadRequestException(Errors.INVALID_SPECIFICATION_FORMAT.getErrorMessage(specString));
        }

        return result;
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> specCombineAllWithAnd(Iterable<EntitySpecResult<ENTITY>> specs) {
        if (specs == null) return unrestricted();
        
        EntitySpecJoinStep<ENTITY> joinStep = null;
        for (EntitySpecResult<ENTITY> spec : specs) {
            if (joinStep == null) {
                joinStep = EntitySpec.from(spec);
            } else {
                joinStep = joinStep.and(spec);
            }
        }
        
        return joinStep != null ? joinStep.build() : unrestricted();
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> specCombineAllWithOr(Iterable<EntitySpecResult<ENTITY>> specs) {
        if (specs == null) return unrestricted();
        
        EntitySpecJoinStep<ENTITY> joinStep = null;
        for (EntitySpecResult<ENTITY> spec : specs) {
            if (joinStep == null) {
                joinStep = EntitySpec.from(spec);
            } else {
                joinStep = joinStep.or(spec);
            }
        }
        
        return joinStep != null ? joinStep.build() : unrestricted();
    }
}