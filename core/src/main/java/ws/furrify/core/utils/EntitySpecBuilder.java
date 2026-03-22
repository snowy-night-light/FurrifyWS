package ws.furrify.core.utils;

import org.springframework.data.jpa.domain.Specification;
import ws.furrify.core.entity.BaseEntity;

import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EntitySpecBuilder {

    public interface EntitySpecExpression<ENTITY extends BaseEntity> {
        String expression();
        Specification<ENTITY> toSpecification(String field);
    }

    private record InternalExpression<ENTITY extends BaseEntity>(
            String expression,
            BiFunction<String, String, Specification<ENTITY>> specProvider,
            String value,
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

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specEquals(String value) {
        return new InternalExpression<>(" = " + value, (f, v) -> (root, query, cb) -> cb.equal(root.get(f), v), value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specNotEquals(String value) {
        return new InternalExpression<>(" != " + value, (f, v) -> (root, query, cb) -> cb.notEqual(root.get(f), v), value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specGreaterThan(String value) {
        return new InternalExpression<>(" > " + value, (f, v) -> (root, query, cb) -> cb.greaterThan(root.get(f), v), value, null);
    }

    public static <ENTITY extends BaseEntity> EntitySpecExpression<ENTITY> specWhere(String field, EntitySpecExpression<ENTITY> expr) {
        String newExpr = "(" + field + expr.expression() + ")";
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

        private static final String OR_EXPRESSION_STRING = " || ";
        private static final String AND_EXPRESSION_STRING = " && ";
        private static final String JOIN_OPEN_STRING = "(";
        private static final String JOIN_CLOSE_STRING = ")";

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

        Pattern pattern = Pattern.compile("\\((\\w+)\\s+([!=><]+)\\s+([^)]+)\\)");
        Matcher matcher = pattern.matcher(specString);

        while (matcher.find()) {
            String field = matcher.group(1);
            String operator = matcher.group(2);
            String value = matcher.group(3);

            EntitySpecExpression<ENTITY> expr = switch (operator) {
                case "=" -> specEquals(value);
                case "!=" -> specNotEquals(value);
                case ">" -> specGreaterThan(value);
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