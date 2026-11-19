package br.com.sampatech.portal.config;

import br.com.sampatech.portal.exception.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortRulesTest {

    @Test
    void allowedSortPasses() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("openingDate"));

        assertDoesNotThrow(() -> SortRules.validate(pageable, SortRules.ISSUE));
    }

    @Test
    void sortByUserPasswordIsRejected() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("user.passwordHash"));

        assertThrows(BadRequestException.class, () -> SortRules.validate(pageable, SortRules.ISSUE));
    }

    @Test
    void filterDropsForbiddenSortAndUsesFallback() {
        Pageable pageable = PageRequest.of(2, 10, Sort.by("user.email"));

        Pageable safe = SortRules.filter(pageable, SortRules.ISSUE, Sort.by("openingDate"));

        assertEquals(Sort.by("openingDate"), safe.getSort());
        assertEquals(2, safe.getPageNumber());
    }
}
