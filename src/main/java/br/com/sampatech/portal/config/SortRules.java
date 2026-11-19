package br.com.sampatech.portal.config;

import br.com.sampatech.portal.exception.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public final class SortRules {

    public static final Set<String> ISSUE = Set.of("id", "title", "status", "severity", "openingDate", "resolutionDate");
    public static final Set<String> USER = Set.of("id", "name", "email", "userType", "registrationDate", "active");
    public static final Set<String> ISSUE_TYPE = Set.of("id", "name", "category", "active");
    public static final Set<String> AGENCY = Set.of("id", "name", "city", "state", "subprefecture", "active");
    public static final Set<String> CHAT = Set.of("id", "createdAt");

    private SortRules() {
    }

    public static Pageable validate(Pageable pageable, Set<String> allowed) {
        for (Sort.Order order : pageable.getSort()) {
            if (!allowed.contains(order.getProperty())) {
                throw new BadRequestException("Ordenação por '" + order.getProperty() + "' não permitida. Use: "
                        + String.join(", ", new TreeSet<>(allowed)));
            }
        }
        return pageable;
    }

    public static Pageable filter(Pageable pageable, Set<String> allowed, Sort fallback) {
        List<Sort.Order> orders = pageable.getSort().stream()
                .filter(order -> allowed.contains(order.getProperty()))
                .toList();
        Sort sort = orders.isEmpty() ? fallback : Sort.by(orders);
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }
}
