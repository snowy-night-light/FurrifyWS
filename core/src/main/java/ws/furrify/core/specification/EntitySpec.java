package ws.furrify.core.specification;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;
import org.springframework.data.core.PropertyPath;
import ws.furrify.core.entity.BaseEntity;
import ws.furrify.core.exception.BadRequestException;
import ws.furrify.core.exception.Errors;

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
    private static final Pattern SPEC_PATTERN = Pattern.compile("\\(?([\\w.]+)\\s+([!=><^~a-zA-Z]+)\\s+('(?:[^'\\\\\\\\]|\\\\\\\\.)*'|\\\"(?:[^\\\"\\\\\\\\]|\\\\\\\\.)*\\\"|[^)&|]+)\\)?");

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
            Path<?> path = getPath(root, f);
            if (v == null) return cb.isNull(path);
            return cb.equal(path, coerceValue(path, v));
        }, value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEquals(Object value) {
        return new InternalExpression<>(NOT_EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            if (v == null) return cb.isNotNull(path);
            return cb.notEqual(path, coerceValue(path, v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThan(Object value) {
        return new InternalExpression<>(GREATER_THAN_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.greaterThan((Path<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThanOrEqual(Object value) {
        return new InternalExpression<>(GREATER_THAN_OR_EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.greaterThanOrEqualTo((Path<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLessThan(Object value) {
        return new InternalExpression<>(LESS_THAN_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.lessThan((Path<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLessThanOrEqual(Object value) {
        return new InternalExpression<>(LESS_THAN_OR_EQUAL_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof Comparable comparable) {
                return cb.lessThanOrEqualTo((Path<Comparable>) path, comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, coerced));
        }, value, null);
    }

    private static Path<?> getPath(Root<?> root, String field) {
        PropertyPath propertyPath = PropertyPath.from(field, root.getJavaType());
        Path<?> path = root;
        while (propertyPath != null) {
            if (propertyPath.isCollection()) {
                path = ((jakarta.persistence.criteria.From<?, ?>) path).join(propertyPath.getSegment());
            } else {
                path = path.get(propertyPath.getSegment());
            }
            propertyPath = propertyPath.next();
        }
        return path;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object coerceValue(Path<?> path, Object value) {
        if (value instanceof String strValue) {
            Class<?> type = path.getJavaType();
            try {
                if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, strValue);
                if (type == java.time.ZonedDateTime.class) return java.time.ZonedDateTime.parse(strValue);
                if (type == java.time.LocalDate.class) return java.time.LocalDate.parse(strValue);
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
            Path<?> path = getPath(root, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof String strValue) {
                return cb.equal(cb.lower((Path<String>) path), strValue.toLowerCase());
            }
            return cb.equal(path, coerced);
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEqualsIgnoreCase(Object value) {
        return new InternalExpression<>(NOT_EQUAL_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            Object coerced = coerceValue(path, v);
            if (coerced instanceof String strValue) {
                return cb.notEqual(cb.lower((Path<String>) path), strValue.toLowerCase());
            }
            return cb.notEqual(path, coerced);
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLike(Object value) {
        return new InternalExpression<>(LIKE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) ->
                cb.like((Path<String>) getPath(root, f), String.valueOf(v)), value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotLike(Object value) {
        return new InternalExpression<>(NOT_LIKE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) ->
                cb.notLike((Path<String>) getPath(root, f), String.valueOf(v)), value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLikeIgnoreCase(Object value) {
        return new InternalExpression<>(LIKE_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            if (v instanceof String strValue) {
                return cb.like(cb.lower((Path<String>) path), strValue.toLowerCase());
            }
            return cb.like((Path<String>) path, String.valueOf(v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotLikeIgnoreCase(Object value) {
        return new InternalExpression<>(NOT_LIKE_IGNORE_CASE_OPERATOR + formatValueForSpecString(value), (f, v) -> (root, query, cb) -> {
            Path<?> path = getPath(root, f);
            if (v instanceof String strValue) {
                return cb.notLike(cb.lower((Path<String>) path), strValue.toLowerCase());
            }
            return cb.notLike((Path<String>) path, String.valueOf(v));
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

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> fromSpecString(String specString) {
        if (specString == null) {
            return EntitySpec.unrestricted();
        }

        EntitySpecJoinStep<ENTITY> joinStep = null;
        Matcher matcher = SPEC_PATTERN.matcher(specString);

        int lastEnd = 0;
        while (matcher.find()) {
            String field = matcher.group(1);
            String operator = matcher.group(2);
            String rawValue = matcher.group(3).trim();

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

            if (joinStep == null) {
                joinStep = EntitySpec.<ENTITY>specBuilder().where(field, expr);
            } else {
                String separator = specString.substring(lastEnd, matcher.start());
                if (separator.contains("||")) {
                    joinStep = joinStep.or().where(field, expr);
                } else {
                    joinStep = joinStep.and().where(field, expr);
                }
            }
            lastEnd = matcher.end();
        }

        if (joinStep == null && !specString.trim().isEmpty()) {
            throw new BadRequestException(Errors.INVALID_SPECIFICATION_FORMAT.getErrorMessage(specString));
        }

        return (joinStep != null) ? joinStep.build() : unrestricted();
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