package ws.furrify.core.utils;

import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySpecBuilder {

    public static final String OR_EXPRESSION_STRING = " || ";
    public static final String AND_EXPRESSION_STRING = " && ";
    public static final String JOIN_OPEN_STRING = "(";
    public static final String JOIN_CLOSE_STRING = ")";
    public static final String EQUAL_OPERATOR = " = ";
    public static final String NOT_EQUAL_OPERATOR = " != ";
    public static final String GREATER_THAN_OPERATOR = " > ";
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final Pattern SPEC_PATTERN = Pattern.compile("\\((\\w+)\\s+([!=><]+)\\s+([^)]+)\\)");

    public interface EntitySpecExpression<ENTITY extends BaseEntity> {
        String expression();
        Specification<ENTITY> toSpecification(String field);
    }

    private record InternalExpression<ENTITY extends BaseEntity>(
            String expression,
            BiFunction<String, Object, Specification<ENTITY>> specProvider,
            Object value,
            Specification<ENTITY> nestedSpec
    ) implements EntitySpecExpression<ENTITY> {
        @Override
        public Specification<ENTITY> toSpecification(String field) {
            return (nestedSpec != null) ? nestedSpec : specProvider.apply(field, value);
        }
    }

    public record EntitySpecResult<ENTITY extends BaseEntity>(String specString, Specification<ENTITY> specification) {
    }

    public interface EntitySpecWhereStep<ENTITY extends BaseEntity> {
        EntitySpecJoinStep<ENTITY> where(String field, EntitySpecExpression<ENTITY> expression);
    }

    public interface EntitySpecAndOrStep<ENTITY extends BaseEntity> {
        EntitySpecJoinStep<ENTITY> where(String field, EntitySpecExpression<ENTITY> expression);
    }

    public interface EntitySpecJoinStep<ENTITY extends BaseEntity> {
        EntitySpecAndOrStep<ENTITY> and();
        EntitySpecAndOrStep<ENTITY> or();
        EntitySpecJoinStep<ENTITY> and(EntitySpecExpression<ENTITY> nested);
        EntitySpecJoinStep<ENTITY> or(EntitySpecExpression<ENTITY> nested);
        EntitySpecResult<ENTITY> build();
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specEquals(Object value) {
        return new InternalExpression<>(EQUAL_OPERATOR + value, (f, v) -> (root, query, cb) -> cb.equal(root.get(f), v), value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEquals(Object value) {
        return new InternalExpression<>(NOT_EQUAL_OPERATOR + value, (f, v) -> (root, query, cb) -> cb.notEqual(root.get(f), v), value, null);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThan(Object value) {
        return new InternalExpression<>(GREATER_THAN_OPERATOR + value, (f, v) -> (root, query, cb) -> {
            if (v instanceof Comparable comparable) {
                return cb.greaterThan(root.get(f), comparable);
            }
            throw new IllegalArgumentException("Field " + f + " is not comparable with value " + v);
        }, value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specWhere(String field, EntitySpecExpression<ENTITY> expr) {
        String newExpr = JOIN_OPEN_STRING + field + expr.expression() + JOIN_CLOSE_STRING;
        Specification<ENTITY> newSpec = expr.toSpecification(field);
        return new InternalExpression<>(newExpr, null, null, newSpec);
    }

    public static <ENTITY extends BaseEntity> EntitySpecWhereStep<ENTITY> specBuilder() {
        return new Builder<>();
    }

    private static class Builder<ENTITY extends BaseEntity> implements EntitySpecWhereStep<ENTITY>, EntitySpecJoinStep<ENTITY>, EntitySpecAndOrStep<ENTITY> {
        private final StringBuilder queryStr = new StringBuilder();
        private Specification<ENTITY> combinedSpec = null;
        private boolean useOrLogic = false;

        @Override
        public EntitySpecJoinStep<ENTITY> where(String field, EntitySpecExpression<ENTITY> expr) {
            if (!queryStr.isEmpty() && !queryStr.toString().endsWith(AND_EXPRESSION_STRING) && !queryStr.toString().endsWith(OR_EXPRESSION_STRING)) {
                queryStr.append(AND_EXPRESSION_STRING);
            }
            queryStr.append(JOIN_OPEN_STRING).append(field).append(expr.expression()).append(JOIN_CLOSE_STRING);
            updateSpec(expr.toSpecification(field));
            return this;
        }

        @Override
        public EntitySpecAndOrStep<ENTITY> or() {
            queryStr.append(OR_EXPRESSION_STRING);
            useOrLogic = true;
            return this;
        }

        @Override
        public EntitySpecJoinStep<ENTITY> or(EntitySpecExpression<ENTITY> nested) {
            if (!queryStr.isEmpty() && !queryStr.toString().endsWith(OR_EXPRESSION_STRING)) {
                queryStr.append(OR_EXPRESSION_STRING);
            }
            queryStr.append(nested.expression());
            useOrLogic = true;
            updateSpec(nested.toSpecification(null));
            return this;
        }

        @Override
        public EntitySpecAndOrStep<ENTITY> and() {
            queryStr.append(AND_EXPRESSION_STRING);
            useOrLogic = false;
            return this;
        }

        @Override
        public EntitySpecJoinStep<ENTITY> and(EntitySpecExpression<ENTITY> nested) {
            if (!queryStr.isEmpty() && !queryStr.toString().endsWith(AND_EXPRESSION_STRING)) {
                queryStr.append(AND_EXPRESSION_STRING);
            }
            queryStr.append(nested.expression());
            useOrLogic = false;
            updateSpec(nested.toSpecification(null));
            return this;
        }

        private void updateSpec(Specification<ENTITY> newSpec) {
            if (combinedSpec == null) {
                combinedSpec = Specification.where(newSpec);
            } else {
                combinedSpec = useOrLogic ? combinedSpec.or(newSpec) : combinedSpec.and(newSpec);
            }
            useOrLogic = false;
        }

        @Override
        public EntitySpecResult<ENTITY> build() {
            Specification<ENTITY> finalSpec = (combinedSpec == null) ? (root, query, cb) -> cb.conjunction() : combinedSpec;
            return new EntitySpecResult<>(queryStr.toString(), finalSpec);
        }
    }

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> fromSpecString(String specString) {
        EntitySpecJoinStep<ENTITY> joinStep = null;
        Matcher matcher = SPEC_PATTERN.matcher(specString);

        while (matcher.find()) {
            String field = matcher.group(1);
            String operator = matcher.group(2);
            String rawValue = matcher.group(3);

            Object parsedValue = rawValue;
            if (rawValue.matches(UUID_REGEX)) {
                parsedValue = UUID.fromString(rawValue);
            }

            Object finalValue = parsedValue;
            EntitySpecExpression<ENTITY> expr = switch (operator.trim()) {
                case "=" -> specEquals(finalValue);
                case "!=" -> specNotEquals(finalValue);
                case ">" -> specGreaterThan(finalValue);
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            };

            if (joinStep == null) {
                joinStep = EntitySpecBuilder.<ENTITY>specBuilder().where(field, expr);
            } else {
                joinStep = joinStep.and().where(field, expr);
            }
        }

        return (joinStep != null) ? joinStep.build() : new EntitySpecResult<>("", (root, query, cb) -> cb.conjunction());
    }
}