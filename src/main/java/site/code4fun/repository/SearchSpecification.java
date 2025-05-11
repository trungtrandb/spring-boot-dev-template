package site.code4fun.repository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import site.code4fun.model.dto.SearchCriteria;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static site.code4fun.constant.AppConstants.COMMA;
import static site.code4fun.constant.AppConstants.TIME_ZONE;
import static site.code4fun.constant.SearchOperator.*;
import static site.code4fun.util.UrlParserUtils.isCollectionField;

@AllArgsConstructor
@Slf4j
public class SearchSpecification<T> implements Specification<T> {
    private transient SearchCriteria criteria;

    @Override
    public Predicate toPredicate(@NonNull Root<T> root, CriteriaQuery<?> query, @NonNull CriteriaBuilder builder) {
        Object value = criteria.getValue();
        String key = criteria.getKey();


        return switch (criteria.getOperation()) {
            case GREATER_THAN -> builder.greaterThanOrEqualTo(root.get(key), value.toString());
            case LESS_THAN -> builder.lessThanOrEqualTo(root.get(key), value.toString());

            case IN -> builder.in(root.join(key).get("id")).value(parseValues(value)); // "1,2,3" or List<>(1,2,3)
            case NOT_IN -> builder.not(root.get(key).in(parseValues(value)));

            case EQUAL -> handleEqual(builder, root, key, value);
            case NOT_EQUAL -> handleNotEqual(builder, root, key, value);

            default -> {
                log.warn(criteria.toString());
                yield null;
            }
        };
    }

    private List<?> parseValues(Object value) {
        try {
            return Arrays.stream(value.toString().split(COMMA))
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException e) {
            return Arrays.stream(value.toString().split(COMMA)).toList();
        }
    }

    private Predicate handleNotEqual(CriteriaBuilder builder, Root<?> root, String key, Object value) {
        Class<?> keyType = getKeyType(root, key);
        if (keyType == String.class) {
            return builder.notLike(root.get(key), "%" + value + "%");
        }
        return builder.notEqual(root.get(key), value);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Predicate handleEqual(CriteriaBuilder builder, Root<?> root, String key, Object value) {
        Class<?> keyType = getKeyType(root, key);

        if (keyType == String.class) {
            return builder.like(root.get(key), "%" + value + "%");
        } else if (keyType == Boolean.TYPE) {
            return builder.equal(root.get(key), Boolean.parseBoolean(value.toString()));
        }else if (keyType.isEnum()) {
            Enum<?> enumValue = Enum.valueOf((Class<Enum>) keyType, value.toString());
            return builder.equal(root.get(key), enumValue);
        } else if (isCollectionField(keyType)) {
            Predicate idLike = builder.equal(root.get(key).get("id"),  value);
            Predicate nameLike = builder.like(root.get(key).get("name"), "%" + value + "%");
            return builder.or(idLike, nameLike);
        } else if (keyType == LocalDateTime.class || keyType == Date.class) {
            ZonedDateTime parsedDate = ZonedDateTime.parse(value.toString());
            ZonedDateTime zonedDateTimeInDesiredZone = parsedDate.withZoneSameInstant(ZoneId.of(TIME_ZONE));
            LocalDate localDate = zonedDateTimeInDesiredZone.toLocalDate();

            Predicate startDay = builder.greaterThanOrEqualTo(root.get(key), localDate.atStartOfDay());
            Predicate endDay = builder.lessThanOrEqualTo(root.get(key), localDate.atTime(23, 59, 59));
            return builder.and(startDay, endDay);
        }

        return  builder.equal(root.get(key),  value);
    }

    private Class<?> getKeyType(Root<?> root, String key) {
        Class<?> keyType = null;
        try {
            keyType = root.getJavaType().getDeclaredField(key).getType();
        } catch (Exception e) {
            try {
                keyType = root.getJavaType().getSuperclass().getDeclaredField(key).getType();
            } catch (NoSuchFieldException ex) {
                throw new RuntimeException(ex);
            }
            log.error(e.getMessage());
        }
        return keyType;
    }
}