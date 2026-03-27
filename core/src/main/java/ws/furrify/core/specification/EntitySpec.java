package ws.furrify.core.specification;

import ws.furrify.core.entity.BaseEntity;

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
    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    private static final Pattern SPEC_PATTERN = Pattern.compile("\\((\\w+)\\s+([!=><]+)\\s+([^)]+)\\)");

    public static <ENTITY extends BaseEntity> EntitySpecResult<ENTITY> unrestricted() {
        return new EntitySpecResult<>("", (root, query, cb) -> cb.conjunction());
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
        EntitySpecJoinStep<ENTITY> joinStep = null;
        Matcher matcher = SPEC_PATTERN.matcher(specString);

        while (matcher.find()) {
            String field = matcher.group(1);
            String operator = matcher.group(2);
            String rawValue = matcher.group(3);

            Object parsedValue = rawValue.matches(UUID_REGEX) ? UUID.fromString(rawValue) : rawValue;

            EntitySpecExpression<ENTITY> expr = switch (operator.trim()) {
                case "=" -> EntitySpec.specEquals(parsedValue);
                case "!=" -> EntitySpec.specNotEquals(parsedValue);
                case ">" -> EntitySpec.specGreaterThan(parsedValue);
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            };

            joinStep = (joinStep == null) ? EntitySpec.<ENTITY>specBuilder().where(field, expr) : joinStep.and().where(field, expr);
        }

        return (joinStep != null) ? joinStep.build() : unrestricted();
    }
}