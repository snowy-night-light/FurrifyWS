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
    private static final Pattern SPEC_PATTERN = Pattern.compile("\\(?([\\w.]+)\\s+([!=><^~a-zA-Z]+)\\s+([^)&|]+)\\)?");

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> unrestricted() {
        return new EntitySpecResult<>("", (root, query, cb) -> cb.conjunction());
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specEquals(Object value) {
        return new InternalExpression<>(EQUAL_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v == null) return cb.isNull(getPath(root, f));
            return cb.equal(getPath(root, f), v);
        }, value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEquals(Object value) {
        return new InternalExpression<>(NOT_EQUAL_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v == null) return cb.isNotNull(getPath(root, f));
            return cb.notEqual(getPath(root, f), v);
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThan(Object value) {
        return new InternalExpression<>(GREATER_THAN_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof Comparable comparable) {
                return cb.greaterThan((Path<Comparable>) getPath(root, f), comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThanOrEqual(Object value) {
        return new InternalExpression<>(GREATER_THAN_OR_EQUAL_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof Comparable comparable) {
                return cb.greaterThanOrEqualTo((Path<Comparable>) getPath(root, f), comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLessThan(Object value) {
        return new InternalExpression<>(LESS_THAN_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof Comparable comparable) {
                return cb.lessThan((Path<Comparable>) getPath(root, f), comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLessThanOrEqual(Object value) {
        return new InternalExpression<>(LESS_THAN_OR_EQUAL_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof Comparable comparable) {
                return cb.lessThanOrEqualTo((Path<Comparable>) getPath(root, f), comparable);
            }
            throw new BadRequestException(Errors.SPECIFICATION_FIELD_NOT_COMPARABLE_TO_VALUE.getErrorMessage(f, v));
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specEqualsIgnoreCase(Object value) {
        return new InternalExpression<>(EQUAL_IGNORE_CASE_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof String strValue) {
                return cb.equal(cb.lower((Path<String>) getPath(root, f)), strValue.toLowerCase());
            }
            return cb.equal(getPath(root, f), v);
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEqualsIgnoreCase(Object value) {
        return new InternalExpression<>(NOT_EQUAL_IGNORE_CASE_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof String strValue) {
                return cb.notEqual(cb.lower((Path<String>) getPath(root, f)), strValue.toLowerCase());
            }
            return cb.notEqual(getPath(root, f), v);
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

    
    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLike(Object value) {
        return new InternalExpression<>(LIKE_OPERATOR + value, (f, v) -> (root, query, cb) -> cb.like((Path<String>) getPath(root, f), (String) v), value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotLike(Object value) {
        return new InternalExpression<>(NOT_LIKE_OPERATOR + value, (f, v) -> (root, query, cb) -> cb.notLike((Path<String>) getPath(root, f), (String) v), value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specLikeIgnoreCase(Object value) {
        return new InternalExpression<>(LIKE_IGNORE_CASE_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof String strValue) {
                return cb.like(cb.lower((Path<String>) getPath(root, f)), strValue.toLowerCase());
            }
            return cb.like((Path<String>) getPath(root, f), (String) v);
        }, value, null);
    }

    @SuppressWarnings({"unchecked"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotLikeIgnoreCase(Object value) {
        return new InternalExpression<>(NOT_LIKE_IGNORE_CASE_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof String strValue) {
                return cb.notLike(cb.lower((Path<String>) getPath(root, f)), strValue.toLowerCase());
            }
            return cb.notLike((Path<String>) getPath(root, f), (String) v);
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
            if (rawValue.equalsIgnoreCase("null")) {
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