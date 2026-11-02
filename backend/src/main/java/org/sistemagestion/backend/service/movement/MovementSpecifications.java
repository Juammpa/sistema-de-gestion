package org.sistemagestion.backend.service.movement;

import jakarta.persistence.criteria.*;
import org.sistemagestion.backend.model.*;
import org.sistemagestion.backend.model.enums.MovementType;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MovementSpecifications {

    private MovementSpecifications() {
    }

    public static Specification<Movement> inAccount(Long currentAccountId) {
        return (root, query, cb) -> cb.equal(root.get("currentAccount").get("id"), currentAccountId);
    }

    public static Specification<Movement> dateBetween(LocalDate from, LocalDate to) {
        return (root, query, cb) -> cb.between(root.<LocalDate>get("date"), from, to);
    }

    // Sin tipo: no filtra
    public static Specification<Movement> ofType(MovementType type) {
        if (type == null) return (root, query, cb) -> cb.conjunction();

        Class<? extends Movement> entity = switch (type) {
            case SALE -> Sale.class;
            case PURCHASE -> Purchase.class;
            case PAYMENT -> Payment.class;
        };
        return (root, query, cb) -> cb.equal(root.type(), entity);
    }

    // Busca por texto (corte, tipo de compra, metodo de pago propio) y por precio exacto
    // (total o precio por kilo). Sin texto: no filtra
    public static Specification<Movement> matching(String search) {
        String text = search == null ? "" : search.trim();
        if (text.isEmpty()) return (root, query, cb) -> cb.conjunction();

        List<String> words = Arrays.asList(text.toLowerCase().split("\\s+"));
        BigDecimal price = parsePrice(text);

        return (root, query, cb) -> {
            List<Predicate> options = new ArrayList<>();

            // Texto: todas las palabras tienen que estar en el nombre
            options.add(existsIn(Sale.class, query, cb, root,
                    sale -> allWordsIn(cb, sale.get("cutMeatName"), words)));
            options.add(existsIn(Purchase.class, query, cb, root,
                    purchase -> allWordsIn(cb, purchase.get("purchaseType").get("name"), words)));
            options.add(existsIn(Payment.class, query, cb, root,
                    payment -> allWordsIn(cb, payment.get("customPaymentMethod").get("name"), words)));

            // Precio exacto: total del movimiento o precio por kilo
            if (price != null) {
                options.add(cb.equal(root.get("amount"), price));
                options.add(existsIn(Sale.class, query, cb, root,
                        sale -> cb.equal(sale.get("pricePerKilo"), price)));
                options.add(existsIn(Purchase.class, query, cb, root,
                        purchase -> cb.equal(purchase.get("pricePerKilo"), price)));
            }

            return cb.or(options.toArray(Predicate[]::new));
        };
    }

    // "exists" sobre un subtipo, correlacionado por id
    private static <T extends Movement> Predicate existsIn(Class<T> entity, CriteriaQuery<?> query,
                                                           CriteriaBuilder cb, Root<Movement> root,
                                                           Function<Root<T>, Predicate> condition) {
        Subquery<Integer> sub = query.subquery(Integer.class);
        Root<T> subRoot = sub.from(entity);
        sub.select(cb.literal(1))
                .where(cb.equal(subRoot.get("id"), root.get("id")), condition.apply(subRoot));
        return cb.exists(sub);
    }

    private static Predicate allWordsIn(CriteriaBuilder cb, Expression<String> field, List<String> words) {
        Predicate[] likes = words.stream()
                .map(word -> cb.like(cb.lower(field), "%" + escapeLike(word) + "%", '\\'))
                .toArray(Predicate[]::new);
        return cb.and(likes);
    }

    // Que % y _ escritos por el usuario se busquen como texto literal
    private static String escapeLike(String word) {
        return word.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // Acepta formato argentino: "1.500,50", "1500,5", "$ 1.500", "1500.5". Si no es numero: null
    private static BigDecimal parsePrice(String text) {
        String s = text.replace("$", "").replace(" ", "");
        if (!s.matches("[\\d.,]+")) return null;

        if (s.contains(",")) {
            s = s.replace(".", "").replace(",", ".");
        } else if (s.matches("\\d{1,3}(\\.\\d{3})+")) {
            s = s.replace(".", "");
        }

        try {
            return new BigDecimal(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}